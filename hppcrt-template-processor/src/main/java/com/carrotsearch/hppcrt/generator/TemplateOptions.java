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

import com.carrotsearch.hppcrt.generator.TemplateProcessor.VerboseLevel;

/**
 * Template options for velocity directives in templates.
 */
public class TemplateOptions
{
    public static final String TEMPLATE_FILE_TOKEN = "__TEMPLATE_SOURCE__";

    final public Type ktype;
    final public Type vtype;

    /**
     * Call names to InlinedMethodDef mappings
     */
    public final HashMap<String, InlinedMethodDef> inlineKTypeDefinitions = new HashMap<String, InlinedMethodDef>();
    public final HashMap<String, InlinedMethodDef> inlineVTypeDefinitions = new HashMap<String, InlinedMethodDef>();

    /**
     * Be default, print everything !
     */
    public VerboseLevel verbose = VerboseLevel.full;

    /**
     * Reference over the current Velocity context, so that
     * the current context could be set of get from the TemplateOptions object itself.
     */
    public VelocityContext context = null;

    public File templateFile;

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

            inlineDefs.get(callName).setBody(InlinedMethodDef.reformatArguments(body, argsArray));
        }

        if (isVerboseEnabled(VerboseLevel.full)) {

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

    public String getTemplateFile()
    {
        return this.templateFile.getName();
    }

    public String getGeneratedAnnotation() {
        return String.format(Locale.ROOT,
                "@javax.annotation.Generated(\n" +
                        "    date = \"%s\",\n" +
                        "    value = \"%s\")",
                getTimeNow(),
                TemplateOptions.TEMPLATE_FILE_TOKEN);
    }

    @Override
    public String toString() {
        return "{KType=" + this.ktype + ", VType=" + this.vtype + "}";
    }

    private boolean isVerboseEnabled(final VerboseLevel lvl) {

        return lvl.ordinal() <= this.verbose.ordinal();
    }

    /**
     * Set the verbose level for TemplateOptions
     */
    public void setVerbose(final VerboseLevel verbose)
    {
        this.verbose = verbose;
    }
}