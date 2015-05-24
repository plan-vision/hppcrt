package com.carrotsearch.hppcrt.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.exception.ParseErrorException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class InlinedMethodDef
{
    private static final Pattern JAVA_IDENTIFIER_PATTERN = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*",
            Pattern.MULTILINE | Pattern.DOTALL);

    private static final Pattern TEMPLATESPECS_PATTERN = Pattern.compile("<(?<generic>[\\w\\s,\\[\\]\\*]+\\s*)>\\s*=\\s*=\\s*>\\s*(?<specialization>.+)");

    /**
     * Can be empty, 'this' (option), or a class name for static method calls
     * ex: for 'Intrinsics.<KType>bar(a,b,c)' is  Intrinsics.
     */
    private String qualifiedClassName = "";

    /**
     * Can be empty, or an explicit list of parameters
     * ex: for 'Intrinsics.<U, V>equals(a,b,c)' is  {U,V}
     */
    private final ArrayList<String> genericParameters = new ArrayList<String>();

    /**
     * Cannot be empty, is a method name.
     * ex: for 'Intrinsics.<KType>equals(a,b,c)' is  equals
     */
    private String methodName = "";

    /**
     * String regex pattern to recognize the InlinedMethodDef method call in template source code
     */
    private String methodNamePattern;

    /**
     * Compiled Pattern to recognized the InlinedMethodDef method call.
     */
    private Pattern methodNameCompiledPattern;

    /**
     * Can be empty, or a list of arguments
     * ex: for 'Intrinsics<KType>equals(a,b,c)' is  {a,b,c}
     */
    private final ArrayList<String> arguments = new ArrayList<String>();

    /**
     * Represents a unique template specialization applicable to
     * concrete tampletized parameters, ex:
     * <int, Object> ==> [specialization]
     * @author Vincent
     *
     */
    private class TemplateSpecialization
    {
        public ArrayList<String> specializedGenericParameters = new ArrayList<String>();

        /**
         * Raw method body
         */
        public String methodBody = "";

        /**
         * Body pattern matching #methodBody, expressed by replacing InlinedMethodDef.this.arguments
         * by positional arguments %1%s, %2%s... in order to be consumed by String.format(...)
         * args
         */
        public String methodBodyPattern = "";

        public TemplateSpecialization() {
            //nothing
        }

        public boolean equalsSpecialization(final ArrayList<String> other) {

            if (this.specializedGenericParameters.size() != other.size()) {
                return false;
            }

            for (int i = 0; i < this.specializedGenericParameters.size(); i++) {

                //a wild card is always OK.
                if (this.specializedGenericParameters.get(i).equals("*")) {
                    continue;
                }

                if (!this.specializedGenericParameters.get(i).equals(other.get(i))) {

                    return false;
                }
            } //end for

            return true;
        }

        @Override
        public String toString() {

            return String.format("{TemplateSpecialization(sp args='%s', body='%s', pattern='%s')}",
                    this.specializedGenericParameters.toString(),
                    this.methodBody, this.methodBodyPattern);
        }
    }

    /**
     * The different applicable variants
     */
    private final ArrayList<TemplateSpecialization> specializations = new ArrayList<TemplateSpecialization>();

    /**
     * build from a method def
     * 
     * @param callName a litteral call name as it appears in the template source,
     * ex: 'Intrinsics.<KType>equals(a)'
     */
    public InlinedMethodDef(final String callName) {

        // Parse the call name
        parseCallName(callName);
    }

    @Override
    public int hashCode() {

        return this.methodNamePattern.hashCode();
    }

    @Override
    public boolean equals(final Object other) {

        if (other.getClass() == this.getClass()) {

            final InlinedMethodDef otherSameClass = (InlinedMethodDef) other;

            return this.methodNamePattern.equals(otherSameClass.methodNamePattern);
        }

        return false;
    }

    /**
     * Declare and add a specialization string:
     * ex : <int, float> ==> a * b
     * @param specializationString
     */
    public void addSpecialization(final String specializationString) {

        final TemplateSpecialization currentSpec = parseSpecializationString(specializationString);

        if (currentSpec != null) {
            this.specializations.add(currentSpec);
        }
    }

    /**
     * Compute the finally inlined form, for a matching call m, with pre-extracted arguments
     * @param options
     * @param optionalGenerics
     * @param m
     * @param extractedArguments
     * @return
     */
    public String computeInlinedForm(final TemplateOptions options, final String[] optionalGenerics, final List<String> extractedArguments) {

        //The number of arguments MUST match the ones of the definition (not permitted overrides, those are indeed simple macros)
        if (extractedArguments.size() != this.arguments.size()) {

            throw new ParseErrorException(String.format(
                    "[ERROR] : incorrect number of arguments (%d=>'%s' instead of %d=>'%s') provided  for this call: '%s'...",
                    extractedArguments.size(),
                    ImmutableList.of(extractedArguments).toString(),
                    this.arguments.size(),
                    this.arguments.toString(),
                    toString()));
        }

        String result = "";

        //A) search for the form to match
        final ArrayList<String> expectedGenericArgs = new ArrayList<String>();

        //if the inlined form has no generics, those are class-wise implicit
        if (this.genericParameters.isEmpty()) {

            if (options.hasKType()) {
                expectedGenericArgs.add(options.ktype.getType());
            }

            if (options.hasVType()) {
                expectedGenericArgs.add(options.vtype.getType());
            }
        } else {

            //make use of the optional generics
            for (String singleGeneric : optionalGenerics) {

                singleGeneric = singleGeneric.trim();

                if (options.hasKType() && singleGeneric.contains("KType")) {

                    singleGeneric = singleGeneric.replace("KType", options.ktype.getType());
                }

                if (options.hasVType() && singleGeneric.contains("VType")) {

                    singleGeneric = singleGeneric.replace("VType", options.vtype.getType());
                }

                expectedGenericArgs.add(singleGeneric);
            } //end for
        }

        //B) The matching form is the one TemplateSpecialization.specializedGenericParameters equal to expectedGenericArgs in this.specializations.
        options.log(Level.FINE, "computeInlinedForm(): Search specialization matching generics parameters '"
                + expectedGenericArgs.toString()
                + "'...");

        for (final TemplateSpecialization currentSpecialized : this.specializations) {

            options.log(Level.FINE, "computeInlinedForm(): Try specialization '"
                    + currentSpecialized.toString()
                    + "'...");

            try {
                if (currentSpecialized.equalsSpecialization(expectedGenericArgs)) {

                    options.log(Level.FINE, "computeInlinedForm(): Found compatible specialization, formatting with '"
                            + currentSpecialized.methodBodyPattern
                            + "' with args " + ImmutableList.of(extractedArguments).toString());

                    result = "(" + String.format(Locale.ROOT, currentSpecialized.methodBodyPattern, extractedArguments.toArray()) + ")";

                    //Post-process the remaining generics in result

                    final String resultPostProcess = InlinedMethodDef.rewriteGenericsInInlinedForm(result,
                            this.genericParameters,
                            Lists.newArrayList(optionalGenerics));

                    if (!resultPostProcess.equals(result)) {
                        options.log(Level.FINE, "computeInlinedForm(): rewriting remaining generics '" + result + "' ==> '" + resultPostProcess + "'");
                    }

                    result = resultPostProcess;

                    //match found, format and stop at the first match !
                    break;
                }
            } catch (final IllegalFormatException e) {
                throw new ParseErrorException("[ERROR] : Not able to format the inlined form for this specialization: '" + currentSpecialized + "'");
            }
        } //end for

        if (result.isEmpty()) {

            throw new ParseErrorException("[ERROR] : Not able to find a matching specialization among: '" + this.specializations + "'");
        }

        return result;
    }

    @Override
    public String toString() {

        return String.format(Locale.ROOT, "{InlinedMethodDef(class='%s', generics='%s', method='%s', args='%s', specializations='%s')}",
                this.qualifiedClassName, this.genericParameters, this.methodName,
                this.arguments, this.specializations);
    }

    ////////////////////////////////////////////////////////////////
    //// Utility methods
    ///////////////////////////////////////////////////////////////

    /**
     * Parse the call name and build the InlinedMethodDef core fields
     * @param callName
     */
    private void parseCallName(final String callName) {

        //Search if the name is qualified ("this." ," ClassName.")
        final String[] splittedCallName = callName.split("\\.");

        //If the name is not qualified, or qualified by "this", it means
        //it is a regular object method, whose genericity is attached to the class
        //(Do not bother to manage "this.<V>foo() kind of thing...)
        if (splittedCallName.length == 1 || (splittedCallName.length == 2 && splittedCallName[0].trim().equals("this"))) {

            //NOT qualified, form a 2 group regex with an (optional) "this"-qualified pattern, ex:
            //"this.REHASH(...)" ==> "(this\.\s*)?([\w]+\s*)\("

            final String parsePattern = "(?<this>this\\s*\\.\\s*)?(?<method>[\\w]+\\s*)\\((?<args>[^\\)]*)\\)";

            final Pattern p = Pattern.compile(parsePattern, Pattern.MULTILINE | Pattern.DOTALL);

            final Matcher m = p.matcher(callName);

            if (m.find()) {

                final String method = m.group("method");
                final String args = m.group("args");

                if (method == null) {

                    //not managed
                    throw new ParseErrorException("[ERROR] : Not able to recognize method for this non-qualifed call form: '" + callName + "'");
                }

                //method
                this.methodName = method.trim();

                //args
                if (args == null || args.trim().isEmpty()) {
                    //empty arguments
                    this.arguments.clear();
                } else {
                    //separate each component
                    final String[] splittedArgs = args.split(",");
                    for (final String singleArg : splittedArgs) {

                        this.arguments.add(singleArg.trim());
                    }
                }
                //
                this.methodNamePattern = "(?<this>this\\s*\\.\\s*)?" +
                        "(?<method>" + this.methodName + "\\s*\\()"; //match the exact name + match the first parenthesis

                //compile the pattern
                this.methodNameCompiledPattern = Pattern.compile(this.methodNamePattern, Pattern.MULTILINE | Pattern.DOTALL);

            } else {

                //not managed
                throw new ParseErrorException("[ERROR] : Not able to recognize this non-qualifed call form: '" + callName + "'");
            }
        }
        else if (splittedCallName.length == 2) {

            //qualified != this, i.e with a class name, form a 3 group regex with an (optional) generic pattern ex :
            //"Intrinsics.<KType[]>newArray" ==> "(Intrinsics.\\s*)(<[^>]+>\\s*)?(newArray)"
            //also captures arguments considered as everything between the final parenthesis.

            final String parsePattern = "(?<className>[\\w]+\\s*\\.\\s*)(?<generic><[^>]+>\\s*)?(?<method>[\\w]+\\s*)\\((?<args>[^\\)]*)\\)";

            final Pattern p = Pattern.compile(parsePattern, Pattern.MULTILINE | Pattern.DOTALL);

            final Matcher m = p.matcher(callName);

            if (m.find()) {

                final String classString = m.group("className");

                final String method = m.group("method");
                final String args = m.group("args");
                String generics = m.group("generic");

                if (method == null || classString == null) {

                    //not managed
                    throw new ParseErrorException("[ERROR] : Not able to recognize class name or method for this qualifed call form: '" + callName + "'");
                }

                //class name, strip the final "."
                this.qualifiedClassName = classString.replaceAll("\\s*", "").substring(0, classString.length() - 1);

                //method
                this.methodName = method.trim();

                //generic args, strip down "<" and ">"
                if (generics == null) {
                    this.genericParameters.clear();

                } else if (generics.trim().isEmpty()) {

                    //not managed
                    throw new ParseErrorException("[ERROR] : Diamond '<>' generics for this qualifed call form: '" + callName + "' is not authorized");
                } else {

                    generics = generics.replaceAll("\\s*", "");
                    generics = generics.substring(1, generics.length() - 1);
                    //separate each component
                    final String[] splittedGenerics = generics.split(",");
                    for (final String singleGeneric : splittedGenerics) {

                        this.genericParameters.add(singleGeneric.trim());
                    }
                }

                //args
                if (args == null || args.trim().isEmpty()) {
                    //empty arguments
                    this.arguments.clear();
                } else {
                    //separate each component
                    final String[] splittedArgs = args.split(",");
                    for (final String singleArg : splittedArgs) {

                        this.arguments.add(singleArg.trim());
                    }
                }

                //
                this.methodNamePattern = "(?<className>" + this.qualifiedClassName + "\\s*)" + "\\.\\s*";  //class name

                if (!this.genericParameters.isEmpty()) {
                    this.methodNamePattern += "<" + "(?<generic>[^>]+\\s*)" + ">\\s*";
                }
                this.methodNamePattern +=
                        "(?<method>" + this.methodName + "\\s*\\()"; //match the exact name + the first parenthesis

                //compile the pattern
                this.methodNameCompiledPattern = Pattern.compile(this.methodNamePattern, Pattern.MULTILINE | Pattern.DOTALL);
            }
            else {
                //not managed
                throw new ParseErrorException("[ERROR] : Not able to create Pattern for this call form: '" + callName + "'");
            }
        } // end if qualified name
    }

    /**
     * Generate a Specialization string out of specString
     * @param specString
     * @return
     */
    protected TemplateSpecialization parseSpecializationString(final String specString) {

        final TemplateSpecialization result = new TemplateSpecialization();

        final Matcher m = InlinedMethodDef.TEMPLATESPECS_PATTERN.matcher(specString);

        if (m.find()) {

            final String[] specializedGenerics = m.group("generic").trim().split(",");

            final String specializedBody = m.group("specialization").trim();

            //A) Simply check that specializedGenerics names strings are among Type
            for (String singleSpecialization : specializedGenerics) {

                //Strip down any whitespace
                singleSpecialization = singleSpecialization.replaceAll("\\s*", "");

                //thows exception if unknown managed (strip down array brackets before testing !)
                if (Type.fromString(singleSpecialization.replaceAll("[\\[\\]]*", "")) == null && !singleSpecialization.equals("*")) {

                    //not managed
                    throw new ParseErrorException("[ERROR] : Not able to recognize valid Types in this specialization form: '" + specString + "' for this call form: '" + toString() + "'");
                }

                result.specializedGenericParameters.add(singleSpecialization);

            } //end for

            result.methodBody = specializedBody.trim();
            //be safe by encapsulating the body
            result.methodBodyPattern = InlinedMethodDef.reformatArguments(result.methodBody, this.arguments);

        } //end m.find

        else {
            //not managed
            throw new ParseErrorException("[ERROR] : Not able to parse this specialization form: '" + specString + "' for this call form: '" + toString() + "'");
        }

        return result;
    }

    /**
     * Converts the human readable arguments listed in argsArray[] as equivalent %1%s, %2%s...etc positional arguments in the method body methodBodyStr
     * (protected for Unit Testing)
     * @param methodBodyStr
     * @param argsArray
     * @return
     */
    protected static String reformatArguments(final String methodBodyStr, final ArrayList<String> argsArray)
    {
        int argPosition = 0;
        boolean argumentIsFound = false;

        final StringBuilder sb = new StringBuilder();

        final StringBuilder currentBody = new StringBuilder(methodBodyStr);

        //for each of the arguments
        for (int i = 0; i < argsArray.size(); i++)
        {
            argumentIsFound = false;

            sb.setLength(0);

            while (true)
            {
                final Matcher m = InlinedMethodDef.JAVA_IDENTIFIER_PATTERN.matcher(currentBody);

                if (m.find())
                {
                    //copy from the start of the (remaining) method body to start of the current find :
                    sb.append(currentBody, 0, m.start());

                    //this java identifier is known, replace
                    if (m.group().equals(argsArray.get(i).trim()))
                    {
                        if (!argumentIsFound)
                        {
                            argPosition++;
                            //first time this argument is encountered, increment position count
                            argumentIsFound = true;
                        }

                        //append replacement : use parenthesis to safe containement of any form of valid expression given as argument
                        sb.append("(%");
                        sb.append(argPosition);
                        sb.append("$s)");
                    }
                    else
                    {
                        //else append verbatim
                        sb.append(m.group());
                    }

                    //Truncate currentBody to only keep the remaining string, after the current find :
                    currentBody.delete(0, m.end());

                } //end if find
                else
                {
                    //append verbatim
                    sb.append(currentBody);
                    break;
                }
            } //end while

            //re-run for the next argument with the whole previous computation
            currentBody.setLength(0);
            currentBody.append(sb);

            //if a particular argument do not exist in method body, we must skip its position anyway.
            if (!argumentIsFound) {

                argPosition++;
            }
        }  //end for each arguments

        return currentBody.toString();
    }

    /**
     * Rewrite the method body methodBodyStr in order to replace
     * the genericsParameters present into the "concrete" instantiated in code.
     * Ex: Definition is Intrinsics.<T>newArray(size) with inlined form "(T[])new Object[size]
     * Then if in code we have  "Intrinsics.<KType>newArray(size) then the body must become (KType[])new Object[size]
     * (with: inlinedForm = "(T[])new Object[size]", genericsParameters = T, concreteGenerics = KType.
     * (protected for Unit Testing)
     * @param inlinedForm
     * @param genericsParameters
     * @param concreteGenerics
     * @return
     */
    protected static String rewriteGenericsInInlinedForm(final String inlinedForm, final ArrayList<String> genericsParameters,
            final ArrayList<String> concreteGenerics)
    {
        //can be empty, return original
        if (genericsParameters.isEmpty()) {

            return inlinedForm;
        }

        //generics parameters and concrete ones must be the same length.
        if (genericsParameters.size() != concreteGenerics.size()) {

            throw new ParseErrorException("[ERROR] rewriteGenericsInInlinedForm(): not same size !");
        }

        final StringBuilder sb = new StringBuilder();

        final StringBuilder currentBody = new StringBuilder(inlinedForm);

        //for each of the arguments genericsParameters
        for (int i = 0; i < genericsParameters.size(); i++)
        {
            sb.setLength(0);

            while (true)
            {
                final Matcher m = InlinedMethodDef.JAVA_IDENTIFIER_PATTERN.matcher(currentBody);

                if (m.find())
                {
                    //copy from the start of the (remaining) method body to start of the current find :
                    sb.append(currentBody, 0, m.start());

                    //this java identifier is known, replace
                    if (m.group().equals(genericsParameters.get(i).trim()))
                    {
                        //append replacement : genericsParameters[i] ==> concreteGenerics[i]
                        sb.append(concreteGenerics.get(i));
                    }
                    else
                    {
                        //else append verbatim
                        sb.append(m.group());
                    }

                    //Truncate currentBody to only keep the remaining string, after the current find :
                    currentBody.delete(0, m.end());
                } //end if find
                else
                {
                    //append verbatim
                    sb.append(currentBody);
                    break;
                }
            } //end while

            //re-run for the next argument with the whole previous computation
            currentBody.setLength(0);
            currentBody.append(sb);
        }  //end for each arguments

        return currentBody.toString();
    }

    public String getQualifiedClassName() {
        return this.qualifiedClassName;
    }

    public ArrayList<String> getGenericParameters() {
        return this.genericParameters;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public ArrayList<String> getArguments() {
        return this.arguments;
    }

    public Pattern getMethodNameCompiledPattern() {
        return this.methodNameCompiledPattern;
    }

    public String getMethodNamePattern() {
        return this.methodNamePattern;
    }
}
