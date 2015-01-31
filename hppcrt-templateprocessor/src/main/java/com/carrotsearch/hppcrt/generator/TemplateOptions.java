package com.carrotsearch.hppcrt.generator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;

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
     * Reference over the current Velocity context, so that
     * the current context could be set of get from the TemplateOptions object itself.
     */
    public VelocityContext context = null;

    public File sourceFile;

    public static class LocalInlineBodies
    {
        public LocalInlineBodies(final String genericBody,
                final String integerBody,
                final String longBody,
                final String floatBody, final String doubleBody, final String booleanBody) {

            this.genericBody = genericBody;
            this.integerBody = integerBody;
            this.longBody = longBody;
            this.floatBody = floatBody;
            this.doubleBody = doubleBody;
            this.booleanBody = booleanBody;
        }

        public final String genericBody;
        public final String integerBody;
        public final String longBody;
        public final String floatBody;
        public final String doubleBody;
        public final String booleanBody;

        @Override
        public String toString() {

            return String.format("{LocalInlineBodies(gen='%s', int='%s', long=%s, float='%s', double='%s', bool='%s')}",
                    this.genericBody, this.integerBody, this.longBody, this.floatBody, this.doubleBody, this.booleanBody);
        }
    }

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

    public HashMap<String, LocalInlineBodies> localInlinesMap = new HashMap<String, LocalInlineBodies>();

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

                throw new DoNotGenerateTypeException(this.ktype, this.vtype);
            }
        }
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
                throw new DoNotGenerateTypeException(this.ktype, this.vtype);
            }
        }
    }

    public boolean inline(final String callName, String args, String universalCallBody) {

        //Rebuild the arguments with a pattern understandable by the matcher
        args = args.replace("(", "");
        args = args.replace(")", "");

        final String[] argsArray = args.split(",");

        universalCallBody = reformatJavaArguments(universalCallBody, argsArray);

        this.localInlinesMap.put(callName,
                new LocalInlineBodies(
                        universalCallBody,
                        universalCallBody,
                        universalCallBody,
                        universalCallBody,
                        universalCallBody,
                        universalCallBody));

        return false;
    }

    public boolean inlineGenericAndPrimitive(final String callName, String args, String genericCallBody, String primitiveCallBody) {

        //Rebuild the arguments with a pattern understandable by the matcher
        args = args.replace("(", "");
        args = args.replace(")", "");

        final String[] argsArray = args.split(",");

        genericCallBody = reformatJavaArguments(genericCallBody, argsArray);

        primitiveCallBody = reformatJavaArguments(primitiveCallBody, argsArray);

        this.localInlinesMap.put(callName,
                new LocalInlineBodies(
                        genericCallBody,
                        primitiveCallBody,
                        primitiveCallBody,
                        primitiveCallBody,
                        primitiveCallBody,
                        primitiveCallBody));

        return false;
    }

    public boolean inlineWithFullSpecialization(final String callName, String args,
            String genericCallBody,
            String integerCallBody,
            String longCallBody,
            String floatCallBody,
            String doubleCallBody,
            String booleanCallBody) {

        //Rebuild the arguments with a pattern understandable by the matcher
        args = args.replace("(", "");
        args = args.replace(")", "");

        final String[] argsArray = args.split(",");

        genericCallBody = reformatJavaArguments(genericCallBody, argsArray);
        integerCallBody = reformatJavaArguments(integerCallBody, argsArray);
        longCallBody = reformatJavaArguments(longCallBody, argsArray);
        floatCallBody = reformatJavaArguments(floatCallBody, argsArray);
        doubleCallBody = reformatJavaArguments(doubleCallBody, argsArray);
        booleanCallBody = reformatJavaArguments(booleanCallBody, argsArray);

        this.localInlinesMap.put(callName,
                new LocalInlineBodies(
                        genericCallBody,
                        integerCallBody,
                        longCallBody,
                        floatCallBody,
                        doubleCallBody,
                        booleanCallBody));

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
    private String reformatJavaArguments(String methodBodyStr, final String[] argsArray)
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
     * Main for test purposes
     */
    public static void main(final String[] args) {

        final TemplateOptions testInstance = new TemplateOptions(Type.GENERIC, null);

        testInstance.inline("is_allocated",
                "(alloc, slot, keys)",
                "alloc[slot] != -1");

        System.out.println(testInstance.localInlinesMap.toString());

        testInstance.inline("is_allocated",
                "(alloc, slot, keys)",
                "Intrinsics.equalsKTypeDefault(keys[slot])");

        System.out.println(testInstance.localInlinesMap.toString());

        testInstance.inline("is_allocated",
                "(alloc, slot, keys)",
                "Intrinsics.equalsKTypeDefault(keys[slot]= keys / slot + alloc)");

        System.out.println(testInstance.localInlinesMap.toString());

        testInstance.inline("is_allocated",
                "(alloc, slot, keys)",
                "alloc[slot]");

        System.out.println(testInstance.localInlinesMap.toString());

        testInstance.inline("is_allocated",
                "(alloc, slot, keys)",
                "slot[alloc]");

        System.out.println(testInstance.localInlinesMap.toString());

        testInstance.inline("is_allocated",
                "(alloc, slot, keys)",
                "slot[slot[keys[keys[alloc]]]]");

        System.out.println(testInstance.localInlinesMap.toString());

    }
}