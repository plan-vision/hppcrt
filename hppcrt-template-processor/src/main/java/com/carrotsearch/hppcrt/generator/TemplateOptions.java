package com.carrotsearch.hppcrt.generator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

    /**
     * Call names to InlinedMethodDef mappings
     */
    public final HashMap<String, InlinedMethodDef> inlineKTypeDefinitions = new HashMap<String, InlinedMethodDef>();
    public final HashMap<String, InlinedMethodDef> inlineVTypeDefinitions = new HashMap<String, InlinedMethodDef>();

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
        for (String kind : strKind)
        {
            //accept Object as synonym of Generic
            if (kind.toLowerCase().equals("object")) {

                kind = Type.GENERIC.name();
            }

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
        for (String kind : strKind)
        {
            //accept Object as synonym of Generic
            if (kind.toLowerCase().equals("object")) {

                kind = Type.GENERIC.name();
            }

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
        for (String notToBeGenerated : notGeneratingType) {

            //accept Object as synonym of Generic
            if (notToBeGenerated.toLowerCase().equals("object")) {

                notToBeGenerated = Type.GENERIC.name();
            }

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
        for (String notToBeGenerated : notGeneratingType)
        {
            //accept Object as synonym of Generic
            if (notToBeGenerated.toLowerCase().equals("object")) {

                notToBeGenerated = Type.GENERIC.name();
            }

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

            final String callName, final String args,
            final String genericCallBody,
            final String integerCallBody,
            final String longCallBody,
            final String floatCallBody,
            final String doubleCallBody,
            final String booleanCallBody) {

        return internalInlineWithFullSpecialization(this.ktype, this.inlineKTypeDefinitions,
                callName, args,
                genericCallBody,
                integerCallBody,
                longCallBody,
                floatCallBody,
                doubleCallBody,
                booleanCallBody);
    }

    public boolean inlineVTypeWithFullSpecialization(
            final String callName, final String args,
            final String genericCallBody,
            final String integerCallBody,
            final String longCallBody,
            final String floatCallBody,
            final String doubleCallBody,
            final String booleanCallBody) {

        return internalInlineWithFullSpecialization(this.vtype, this.inlineVTypeDefinitions,
                callName, args,
                genericCallBody,
                integerCallBody,
                longCallBody,
                floatCallBody,
                doubleCallBody,
                booleanCallBody);
    }

    private boolean internalInlineWithFullSpecialization(final Type t,
            final HashMap<String, InlinedMethodDef> inlineDefs,
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
        String body = "";

        if (t == Type.GENERIC) {
            body = genericCallBody;

        }
        else if (t == Type.BYTE) {
            body = integerCallBody;

        }
        else if (t == Type.CHAR) {
            body = integerCallBody;

        }
        else if (t == Type.SHORT) {
            body = integerCallBody;

        }
        else if (t == Type.INT) {
            body = integerCallBody;

        }
        else if (t == Type.LONG) {
            body = longCallBody;

        }
        else if (t == Type.FLOAT) {
            body = floatCallBody;

        }
        else if (t == Type.DOUBLE) {
            body = doubleCallBody;

        }
        else if (t == Type.BOOLEAN) {
            body = booleanCallBody;
        }

        //Update Pattern cache
        if (!inlineDefs.containsKey(callName)) {
            inlineDefs.put(callName, new InlinedMethodDef(callName));
        }

        //the method has no arguments
        if (args.isEmpty()) {

            inlineDefs.get(callName).setBody(body);

        }
        else {

            final String[] argsArray = args.split(",");

            inlineDefs.get(callName).setBody(TemplateOptions.reformatArguments(body, argsArray));
        }

        if (this.verbose) {

            System.out.println("TemplateOptions : " + toString() + " captured the inlined function name '" +
                    callName + "' of type '" + t + "' as '" +
                    inlineDefs.get(callName) + "'");
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
    protected static String reformatArguments(final String methodBodyStr, final String[] argsArray)
    {
        int argPosition = 0;
        boolean argumentIsFound = false;

        final StringBuilder sb = new StringBuilder();

        final StringBuilder currentBody = new StringBuilder(methodBodyStr);

        //for each of the arguments
        for (int i = 0; i < argsArray.length; i++)
        {
            argumentIsFound = false;

            sb.setLength(0);

            while (true)
            {
                final Matcher m = TemplateOptions.JAVA_IDENTIFIER_PATTERN.matcher(currentBody);

                if (m.find())
                {
                    //copy from the start of the (remaining) method body to start of the current find :
                    sb.append(currentBody, 0, m.start());

                    //this java identifier is known, replace
                    if (m.group().equals(argsArray[i].trim()))
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