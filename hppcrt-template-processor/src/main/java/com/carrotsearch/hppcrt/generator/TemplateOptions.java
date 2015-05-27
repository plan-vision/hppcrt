package com.carrotsearch.hppcrt.generator;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.velocity.VelocityContext;

/**
 * Template options for velocity directives in templates.
 */
public class TemplateOptions
{
    public static final String TEMPLATE_FILE_TOKEN = "__TEMPLATE_SOURCE__";

    private static final Logger logger = Logger.getLogger(TemplateOptions.class.getName());

    final public Type ktype;
    final public Type vtype;

    /**
     * Call names to InlinedMethodDef mappings
     */
    public final ArrayList<InlinedMethodDef> inlineDefinitions = new ArrayList<InlinedMethodDef>();

    /**
     * By default, print everything (in unit tests !)
     */
    public Level verbose = Level.ALL;

    /**
     * Reference over the current Velocity context, so that
     * the current context could be set of get from the TemplateOptions object itself.
     */
    public VelocityContext context = null;

    public Path templateFile;

    /**
     * Exception to throw when we don't want to generate a particular type
     * @author Vincent
     *
     */
    public static class DoNotGenerateTypeException extends RuntimeException
    {
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

        //By default, print everything (for unit tests)
        setVerbose(Level.ALL);
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

            if (notToBeGenerated.toUpperCase().equals("ALL") || this.ktype == Type.valueOfOrNull(notToBeGenerated.toUpperCase())) {

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

            if (notToBeGenerated.toUpperCase().equals("ALL") || this.vtype == Type.valueOfOrNull(notToBeGenerated.toUpperCase()))
            {
                doNotGenerate();
            }
        }
    }

    ///////////////////////////////////////////
    //////////////// Inline management ////////
    ///////////////////////////////////////////

    public boolean declareInline(final String callName, final String... specializations) {

        final InlinedMethodDef inlined = new InlinedMethodDef(callName);

        log(Level.FINE, "[" + this.ktype + "," + this.vtype + "] declareInline(): try adding a new '" + callName + "' ==> '" + inlined.toString() + "'");

        //only create a new def if such one do not exist yet.
        int foundIndex = this.inlineDefinitions.indexOf(inlined);

        if (foundIndex == -1) {

            log(Level.FINE, "[" + this.ktype + "," + this.vtype + "] declareInline(): '" + callName + "' do not exist, add to the list...");

            this.inlineDefinitions.add(inlined);
            foundIndex = this.inlineDefinitions.size() - 1;

        } else {
            log(Level.FINE, "[" + this.ktype + "," + this.vtype + "] declareInline(): '" + callName + "' already exist, add to existing specializations...");
        }

        for (final String singlespec : specializations) {

            this.inlineDefinitions.get(foundIndex).addSpecialization(singlespec);

            log(Level.FINE, "[" + this.ktype + "," + this.vtype + "] declareInline(): added specialization to '" + callName + "' ==> '"
                    + this.inlineDefinitions.get(foundIndex).toString() + "'");
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
        return this.templateFile.getFileName().toString();
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

    /**
     * 
     */
    public void setVerbose(final Level verbose) {
        this.verbose = verbose;

        TemplateProcessor.setLoggerlevel(TemplateOptions.logger, this.verbose);
    }

    /**
     * log shortcut
     */
    public void log(final Level lvl, final String format, final Object... args) {

        //this check prevents complex toString() formatting
        if (TemplateOptions.logger.isLoggable(this.verbose)) {

            if (args.length == 0) {
                TemplateOptions.logger.log(lvl, format);
            } else {
                TemplateOptions.logger.log(lvl, String.format(Locale.ROOT, format, args));
            }
        }
    }
}