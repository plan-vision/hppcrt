package com.carrotsearch.hppcrt.generator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;

/**
 * Template options for velocity directives in templates.
 */
public class TemplateOptions
{
    private static final Pattern JAVA_IDENTIFIER_PATTERN = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*",
            Pattern.MULTILINE | Pattern.DOTALL);

    final public Type ktype;
    final public Type vtype;

    public HashMap<String, String> inlineKTypeDefinitions = new HashMap<String, String>();
    public HashMap<String, String> inlineVTypeDefinitions = new HashMap<String, String>();

    private boolean verbose = false;

    /**
     * Reference over the current Velocity context, so that
     * the current context could be set of get from the TemplateOptions object itself.
     */
    public VelocityContext context = null;

    public File sourceFile;

    /**
     * Exception to throw when we don't want to generate a particular type
     * @author Vincent
     *
     */
    public static class DoNotGenerateTypeException extends RuntimeException
    {
        //nothing
        private static final long serialVersionUID = 5770524405182569439L;

        public final Type currentKType;
        public final Type currentVType;

        public DoNotGenerateTypeException(final Type k, final Type v) {
            super();
            this.currentKType = k;
            this.currentVType = v;
        }
    }

    public TemplateOptions(final Type ktype, final Type vtype)
    {
        this.ktype = ktype;
        this.vtype = vtype;
    }

    public boolean isKTypePrimitive()
    {
        return this.ktype != Type.GENERIC;
    }

    public boolean isKTypeNumeric()
    {
        return isKTypePrimitive() && !isKTypeBoolean();
    }

    public boolean isKTypeBoolean()
    {
        return this.ktype == Type.BOOLEAN;
    }

    public boolean isKType(final String... strKind)
    {
        //return true if it matches any type of the list, case insensitively while
        //only accepting valid Type strings
        for (final String kind : strKind)
        {
            if (this.ktype == Type.valueOf(kind.toUpperCase()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isVTypePrimitive()
    {
        return this.vtype != Type.GENERIC;
    }

    public boolean isVTypeNumeric()
    {
        return isVTypePrimitive() && !isVTypeBoolean();
    }

    public boolean isVTypeBoolean()
    {
        return this.vtype == Type.BOOLEAN;
    }

    public boolean isVType(final String... strKind)
    {
        //return true if it matches any type of the list
        for (final String kind : strKind)
        {
            //return true if it matches any type of the list, case insensitively while
            //only accepting valid Type strings
            if (this.vtype == Type.valueOf(kind.toUpperCase()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isKTypeGeneric()
    {
        return this.ktype == Type.GENERIC;
    }

    public boolean isVTypeGeneric()
    {
        return this.vtype == Type.GENERIC;
    }

    public boolean isAllGeneric()
    {
        return isKTypeGeneric() && isVTypeGeneric();
    }

    public boolean isAnyPrimitive()
    {
        return isKTypePrimitive() || isVTypePrimitive();
    }

    public boolean isAnyGeneric()
    {
        return isKTypeGeneric() || isVTypeGeneric();
    }

    public boolean hasKType()
    {
        return this.ktype != null;
    }

    public boolean hasVType()
    {
        return this.vtype != null;
    }

    public Type getKType()
    {
        return this.ktype;
    }

    public Type getVType()
    {
        return this.vtype;
    }

    public void doNotGenerateKType(final String... notGeneratingType)
    {
        //if any of the notGeneratingType is this.ktype, then do not generate
        //return true if it matches any type of the list, case insensitively while
        //only accepting valid Type strings
        for (final String notToBeGenerated : notGeneratingType) {

            if (notToBeGenerated.toUpperCase().equals("ALL") || this.ktype == Type.valueOf(notToBeGenerated.toUpperCase())) {

                doNotGenerate();
            }
        }
    }

    public void doNotGenerate()
    {
        throw new DoNotGenerateTypeException(this.ktype, this.vtype);
    }

    public void doNotGenerateVType(final String... notGeneratingType)
    {
        //if any of the notGeneratingType is this.ktype, then do not generate
        //return true if it matches any type of the list, case insensitively while
        //only accepting valid Type strings
        for (final String notToBeGenerated : notGeneratingType)
        {
            if (notToBeGenerated.toUpperCase().equals("ALL") || this.vtype == Type.valueOf(notToBeGenerated.toUpperCase()))
            {
                doNotGenerate();
            }
        }
    }

    ///////////////////////////////////////////
    //////////////// Inline management ////////
    ///////////////////////////////////////////

    public boolean inlineKType(final String callName, final String args, final String universalCallBody) {

        return inlineKTypeWithFullSpecialization(callName, args,
                universalCallBody,
                universalCallBody,
                universalCallBody,
                universalCallBody,
                universalCallBody,
                universalCallBody);
    }

    public boolean inlineVType(final String callName, final String args, final String universalCallBody) {

        return inlineVTypeWithFullSpecialization(callName, args,
                universalCallBody,
                universalCallBody,
                universalCallBody,
                universalCallBody,
                universalCallBody,
                universalCallBody);
    }

    public boolean inlineKTypeGenericAndPrimitive(final String callName, final String args, final String genericCallBody, final String primitiveCallBody) {

        return inlineKTypeWithFullSpecialization(callName, args,
                genericCallBody,
                primitiveCallBody,
                primitiveCallBody,
                primitiveCallBody,
                primitiveCallBody,
                primitiveCallBody);
    }

    public boolean inlineVTypeGenericAndPrimitive(final String callName, final String args, final String genericCallBody, final String primitiveCallBody) {

        return inlineVTypeWithFullSpecialization(callName, args,
                genericCallBody,
                primitiveCallBody,
                primitiveCallBody,
                primitiveCallBody,
                primitiveCallBody,
                primitiveCallBody);
    }

    public boolean inlineKTypeWithFullSpecialization(
            final String callName, String args,
            final String genericCallBody,
            final String integerCallBody,
            final String longCallBody,
            final String floatCallBody,
            final String doubleCallBody,
            final String booleanCallBody) {

        //Rebuild the arguments with a pattern understandable by the matcher
        args = args.replace("(", "").trim();
        args = args.replace(")", "").trim();

        //Pick the ones matching TemplateOptions current Type(s) :
        String ktypeBody = "";

        if (this.ktype == Type.GENERIC) {
            ktypeBody = genericCallBody;

        }
        else if (this.ktype == Type.BYTE) {
            ktypeBody = integerCallBody;

        }
        else if (this.ktype == Type.CHAR) {
            ktypeBody = integerCallBody;

        }
        else if (this.ktype == Type.SHORT) {
            ktypeBody = integerCallBody;

        }
        else if (this.ktype == Type.INT) {
            ktypeBody = integerCallBody;

        }
        else if (this.ktype == Type.LONG) {
            ktypeBody = longCallBody;

        }
        else if (this.ktype == Type.FLOAT) {
            ktypeBody = floatCallBody;

        }
        else if (this.ktype == Type.DOUBLE) {
            ktypeBody = doubleCallBody;

        }
        else if (this.ktype == Type.BOOLEAN) {
            ktypeBody = booleanCallBody;
        }

        final String formatedCallName = TemplateOptions.reformatCallName(callName);

        //the method has no arguments
        if (args.isEmpty()) {

            this.inlineKTypeDefinitions.put(formatedCallName, ktypeBody);

            if (this.verbose) {

                System.out.println("TemplateOptions : " + toString() + " captured the inlined KType function def '" +
                        formatedCallName + "' with no argument : " +
                        this.inlineKTypeDefinitions.get(formatedCallName));
            }
        }
        else {

            final String[] argsArray = args.split(",");

            this.inlineKTypeDefinitions.put(formatedCallName,
                    TemplateOptions.reformatArguments(ktypeBody, argsArray));

            if (this.verbose) {

                System.out.println("TemplateOptions : " + toString() + " captured the inlined KType function def '" +
                        formatedCallName + "' with multiple arguments : " +
                        this.inlineKTypeDefinitions.get(formatedCallName));
            }
        }

        return false;
    }

    public boolean inlineVTypeWithFullSpecialization(
            final String callName, String args,
            final String genericCallBody,
            final String integerCallBody,
            final String longCallBody,
            final String floatCallBody,
            final String doubleCallBody,
            final String booleanCallBody) {

        //Rebuild the arguments with a pattern understandable by the matcher
        args = args.replace("(", "").trim();
        args = args.replace(")", "").trim();

        //Pick the ones matching TemplateOptions current Type(s) :

        String vtypeBody = "";

        //
        if (this.vtype == Type.GENERIC) {

            vtypeBody = genericCallBody;
        }
        else if (this.vtype == Type.BYTE) {

            vtypeBody = integerCallBody;
        }
        else if (this.vtype == Type.CHAR) {

            vtypeBody = integerCallBody;
        }
        else if (this.vtype == Type.SHORT) {

            vtypeBody = integerCallBody;
        }
        else if (this.vtype == Type.INT) {

            vtypeBody = integerCallBody;
        }
        else if (this.vtype == Type.LONG) {

            vtypeBody = longCallBody;
        }
        else if (this.vtype == Type.FLOAT) {

            vtypeBody = floatCallBody;
        }
        else if (this.vtype == Type.DOUBLE) {

            vtypeBody = doubleCallBody;
        }
        else if (this.vtype == Type.BOOLEAN) {

            vtypeBody = booleanCallBody;
        }

        final String formatedCallName = TemplateOptions.reformatCallName(callName);

        //the method has no arguments
        if (args.isEmpty()) {

            this.inlineVTypeDefinitions.put(formatedCallName,
                    vtypeBody);

            if (this.verbose) {

                System.out.println("TemplateOptions : " + toString() + " captured the inlined VType function def '" +
                        formatedCallName + "' with no argument : " +
                        this.inlineVTypeDefinitions.get(formatedCallName));
            }
        }
        else {

            final String[] argsArray = args.split(",");

            this.inlineVTypeDefinitions.put(formatedCallName,
                    TemplateOptions.reformatArguments(vtypeBody, argsArray));

            if (this.verbose) {

                System.out.println("TemplateOptions : " + toString() + " captured the inlined VType function def '" +
                        formatedCallName + "' with multiple arguments : " +
                        this.inlineVTypeDefinitions.get(formatedCallName));
            }
        }

        return false;
    }

    /**
     * Returns the current time in ISO format.
     */
    public String getTimeNow()
    {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
        return format.format(new Date());
    }

    public String getSourceFile()
    {
        return this.sourceFile.getName();
    }

    public String getGeneratedAnnotation()
    {
        return "@javax.annotation.Generated(date = \"" +
                getTimeNow() + "\", value = \"HPPC-RT generated from: " +
                this.sourceFile.getName() + "\")";
    }

    /**
     * Converts the human readable arguments listed in argsArray[] as equivalent %1%s, %2%s...etc positional arguments in the method body methodBodyStr
     * @param methodBodyStr
     * @param argsArray
     * @return
     */
    protected static String reformatArguments(String methodBodyStr, final String[] argsArray)
    {
        int argPosition = 0;
        boolean argumentIsFound = false;

        //for each of the arguments
        for (int i = 0; i < argsArray.length; i++)
        {
            argumentIsFound = false;

            final StringBuffer body = new StringBuffer();

            while (true)
            {
                final Matcher m = TemplateOptions.JAVA_IDENTIFIER_PATTERN.matcher(methodBodyStr);

                if (m.find())
                {
                    //copy from the start of the (remaining) method body to start of the current find :
                    body.append(methodBodyStr.substring(0, m.start()));

                    //this java identifier is known, replace
                    if (m.group().equals(argsArray[i].trim()))
                    {
                        if (!argumentIsFound)
                        {
                            argPosition++;
                            //first time this argument is encountered, increment position count
                            argumentIsFound = true;
                        }

                        //append replacement
                        body.append("%" + argPosition + "$s");
                    }
                    else
                    {
                        //else append verbatim
                        body.append(m.group());
                    }

                    //Truncate methodBodyStr to only keep the remaining string, after the current find :
                    methodBodyStr = methodBodyStr.substring(m.end());

                } //end if find
                else
                {
                    //append verbatim
                    body.append(methodBodyStr);
                    break;
                }
            } //end while

            //re-run for the next argument with the whole previous computation
            methodBodyStr = body.toString();

            //if a particular argument do not exist in method body, we must skip its position anyway.
            if (!argumentIsFound) {

                argPosition++;
            }
        }  //end for each arguments

        return methodBodyStr;
    }

    /**
     * Reformat the call name to a Pattern able
     * to have optional Generic arguments
     * @param callName
     * @return
     */
    protected static String reformatCallName(final String callName)
    {
        String reformatted = "";
        //Search if the name is qualified ("this." ," ClassName.")
        final String[] splittedCallName = callName.split("\\.");

        if (splittedCallName.length == 1) {

            //not qualified, form a 2 group regex with an optional generic pattern, ex:
            //"foo" ==> "(<[^>]+>\\s*)?(foo)"
            //also captures the first parenthesis of the function just after callName.
            reformatted = "(<[^>]+>\\s*)?" + "(" + callName + ")(\\()";

        }
        else if (splittedCallName.length == 2) {

            //qualified, form a 3 group regex with a generic pattern, ex :
            //"Intrinsics.newKTypeArray" ==> "(Intrinsics.\\s*)(<[^>]+>\\s*)?(newKTypeArray)"
            //also captures the first parenthesis of the function just after callName.
            reformatted = "(" + splittedCallName[0] + ".\\s*)" + "(<[^>]+>\\s*)?" + "(" + splittedCallName[1] + ")(\\()";

        }
        else {
            //not managed
            throw new ParseErrorException("[ERROR] : Not able to manage this call form: " + callName);
        }

        return reformatted;
    }

    @Override
    public String toString() {
        return "{KType=" + this.ktype + ", VType=" + this.vtype + "}";
    }

    public void setVerbose(final boolean verb) {

        this.verbose = verb;
    }

    /**
     * Main for test purposes
     */
    public static void main(final String[] args) {

        final TemplateOptions testInstance = new TemplateOptions(Type.GENERIC, null);

        testInstance.setVerbose(true);

        Matcher matcher = Pattern.compile("(Intrinsics.\\s*)(<[^>]+>\\s*)?(newKTypeArray)(\\()",
                Pattern.MULTILINE | Pattern.DOTALL).matcher("Intrinsics. <KType[]>newKTypeArray(toto)");

        matcher.find();

        System.out.println(matcher.toString());

        matcher = Pattern.compile("(Intrinsics.\\s*)(<[^>]+>\\s*)?(newKTypeArray)(\\()",
                Pattern.MULTILINE | Pattern.DOTALL).matcher("Intrinsics.<KType[]> newKTypeArray(size); \n");

        matcher.find();

        System.out.println(matcher.toString());

        System.out.println(TemplateOptions.reformatCallName("Intrinsics.defaultKTypeValue"));

        System.out.println(TemplateOptions.reformatCallName("indexToBufferPosition"));

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "alloc[slot] != -1");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "Intrinsics.equalsKTypeDefault(keys[slot])");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "Intrinsics.equalsKTypeDefault(keys[slot]= keys / slot + alloc)");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "alloc[slot]");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "slot[alloc]");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "slot[slot[keys[keys[alloc]]]]");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());
    }
}