package com.carrotsearch.hppcrt.generator;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.tools.generic.ClassTool;
import org.apache.velocity.tools.generic.ContextTool;
import org.apache.velocity.tools.generic.ConversionTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.FieldTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;

import com.carrotsearch.hppcrt.generator.TemplateOptions.DoNotGenerateTypeException;

/**
 * Template processor for HPPC-RT templates.
 */
public final class TemplateProcessor
{

    //TODO: Maybe load from class path ?
    private static String INTRINSICS_FILE_NAME = "com/carrotsearch/hppcrt/Intrinsics.java";

    private static final Pattern INTRINSICS_METHOD_CALL = Pattern.compile("(Intrinsics.\\s*)(<[^>]+>\\s*)?([a-zA-Z]+)", Pattern.MULTILINE | Pattern.DOTALL);

    private static final Pattern IMPORT_DECLARATION = Pattern.compile("(import\\s+[\\w.\\*\\s]*;)", Pattern.MULTILINE | Pattern.DOTALL);

    private boolean verbose = true;
    private boolean incremental = true;

    private File templatesDir;
    private File dependenciesDir;
    private File outputDir;

    private long timeVelocity, timeIntrinsics, timeTypeClassRefs, timeComments;

    private final VelocityEngine velocity;

    private static final String[] PROGRESS_BAR_PATTERN = new String[] { "||", "//", "--", "\\\\" };

    private int progressBarCount;
    private long progressBarLastDisplayDate;

    private class VelocityLogger implements LogChute
    {
        @Override
        public void init(final RuntimeServices rs) throws Exception {
            // nothing strange here
        }

        @Override
        public void log(final int level, final String message) {

            System.out.println("[VELOCITY]-" + level + " : " + message);
        }

        @Override
        public void log(final int level, final String message, final Throwable t) {

            System.out.println("[VELOCITY]-" + level + "-!!EXCEPTION!! : " + message + " , exception msg: " + t.getMessage());
        }

        @Override
        public boolean isLevelEnabled(final int level) {
            // let Velocity talk
            return true;
        }
    }

    /**
     * Initialize Velocity engine.
     */
    public TemplateProcessor()
    {
        final ExtendedProperties p = new ExtendedProperties();
        p.setProperty(RuntimeConstants.SET_NULL_ALLOWED, "false");

        //Attach a Velocity logger to see internal Velocity log messages on console
        p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new VelocityLogger());

        this.velocity = new VelocityEngine();
        this.velocity.setExtendedProperties(p);

        this.velocity.init();
    }

    /**
     * 
     */
    public void setVerbose(final boolean verbose)
    {
        this.verbose = verbose;
    }

    /**
     * 
     */
    public void setIncremental(final boolean incremental)
    {
        this.incremental = incremental;
    }

    /**
     * 
     */
    public void setDestDir(final File dir)
    {
        this.outputDir = dir;
    }

    /**
     * 
     */
    public void setTemplatesDir(final File dir)
    {
        this.templatesDir = dir;
    }

    public void setDependenciesDir(final File dir)
    {
        this.dependenciesDir = dir;
    }

    /**
     * 
     */
    public void execute()
    {
        // Collect files/ checksums from the output folder.
        final List<OutputFile> outputs = collectOutputFiles(new ArrayList<OutputFile>(), this.outputDir);

        // Collect template files in the input folder, recursively
        final List<TemplateFile> inputs = collectTemplateFiles(new ArrayList<TemplateFile>(), this.templatesDir);

        // Process templates
        System.out.println("Processing " + inputs.size() + " templates to: '" + this.outputDir.getPath() + "'");
        final long start = System.currentTimeMillis();
        processTemplates(inputs, outputs);
        final long end = System.currentTimeMillis();
        System.out.println(String.format(Locale.ENGLISH, "\nProcessed in %.2f sec.", (end - start) * 1e-3));

        // Remove non-marked files.
        int generated = 0;
        int updated = 0;
        int deleted = 0;
        for (final OutputFile f : outputs)
        {
            if (!f.generated)
            {
                deleted++;
                if (this.verbose) {
                    System.out.println("Deleted: " + f.file);
                }
                f.file.delete();
            }

            if (f.generated) {
                generated++;
            }

            if (f.updated)
            {
                updated++;
                if (this.verbose) {
                    System.out.println("Updated: "
                            + relativePath(f.file, this.outputDir));
                }
            }
        }

        System.out.println("Generated " + generated + " files (" + updated + " updated, "
                + deleted + " deleted).");
    }

    /**
     * init a new Velocity Context for a specific TemplateOptions
     */
    private void initTemplateOptionsContext(final TemplateOptions options) {

        final VelocityContext ctx = new VelocityContext();

        ctx.put("TemplateOptions", options);

        //Attach some GenericTools that may be useful for code generation :
        ctx.put("esct", new EscapeTool());
        ctx.put("classt", new ClassTool());
        ctx.put("contextt", new ContextTool());
        ctx.put("convt", new ConversionTool());
        ctx.put("fieldt", new FieldTool());
        ctx.put("matht", new MathTool());
        ctx.put("numbert", new NumberTool());
        ctx.put("rendert", new RenderTool());

        //reference the context itself into the TemplateOptions object
        options.context = ctx;
    }

    /**
     * Inject a Velocity dependency : This method captures all Velocity directives (ex inlines) found in deps, using
     * options as input/output for data; There is no code generation here.
     * @param dep
     */
    private void addSingleDependency(final TemplateFile dep, final TemplateOptions options) {

        if (this.verbose) {

            System.out.println("[INFO] addSingleDependency(): adding for options " + options.toString() +
                    " the dependency: '" + dep.file.toString() + "'...");

        }

        final StringWriter strNull = new StringWriter();

        options.sourceFile = dep.file;

        try {
            this.velocity.evaluate(options.context, strNull, dep.file.getName(), readFile(dep.file));

        }
        catch (final ParseErrorException e) {

            System.out.println("[ERROR] : parsing template '" + dep.fullPath +
                    "' with " + options + " with error: '" + e.getMessage() + "'");

            //rethrow the beast to stop the thing dead.
            throw e;
        }
        catch (final ResourceNotFoundException e) {

            System.out.println("[ERROR] : resource not found for template '" + dep.fullPath +
                    "' with " + options + " with error: '" + e.getMessage() + "'");

            //rethrow the beast to stop the thing dead.
            throw e;
        }
        catch (final MethodInvocationException e) {

            if (e.getCause() instanceof DoNotGenerateTypeException) {

                final DoNotGenerateTypeException doNotGenException = (DoNotGenerateTypeException) e.getCause();

                if (this.verbose) {

                    System.out.println("[INFO] : output from template '" + dep.fullPath +
                            "' with KType = " + doNotGenException.currentKType + " and VType =  " +
                            doNotGenException.currentVType + " was bypassed...");
                }
                else {
                    displayProgressBar();
                }
            }
            else {

                System.out.println("[ERROR] : method invocation from template '" + dep.fullPath +
                        "' with " + options + " failed with error: '" + e.getMessage() + "'");

                //rethrow the beast to stop the thing dead.
                throw e;
            }
        } //end MethodInvocationException
    }

    /**
     * Apply templates to <code>.ktype</code> files (single-argument).
     */
    private void processTemplates(final List<TemplateFile> inputs, final List<OutputFile> outputs)
    {
        //For each template file
        for (final TemplateFile f : inputs)
        {
            final String fileName = f.file.getName();

            //A) KType only specialization
            if (fileName.contains("KType") && !fileName.contains("VType"))
            {
                for (final Type t : Type.values())
                {
                    final TemplateOptions options = new TemplateOptions(t, null);

                    options.setVerbose(this.verbose);

                    //Init a new Velocity Context for the current templateOptions
                    initTemplateOptionsContext(options);

                    generate(f, outputs, options);
                    displayProgressBar();
                }
            }
            //B) (KType * VType) specializations
            else if (fileName.contains("KType") && fileName.contains("VType"))
            {
                for (final Type ktype : Type.values())
                {
                    for (final Type vtype : Type.values())
                    {
                        final TemplateOptions options = new TemplateOptions(ktype, vtype);

                        options.setVerbose(this.verbose);

                        //Init a new Velocity Context for the current templateOptions
                        initTemplateOptionsContext(options);

                        generate(f, outputs, options);
                        displayProgressBar();
                    }
                }
            }
        }

        if (this.verbose) {
            System.out.println(
                    "Velocity: " + this.timeVelocity + "\n" +
                            "Intrinsics: " + this.timeIntrinsics + "\n" +
                            "TypeClassRefs: " + this.timeTypeClassRefs + "\n" +
                            "Comments: " + this.timeComments + "\n");
        }
    }

    /**
     * Apply templates.
     */
    private void generate(final TemplateFile f, final List<OutputFile> outputs,
            final TemplateOptions templateOptions)
    {
        final String targetFileName = targetFileName(relativePath(f.file, this.templatesDir), templateOptions);

        final OutputFile output = findOrCreate(targetFileName, outputs);

        if (!this.incremental || !output.file.exists()
                || output.file.lastModified() <= f.file.lastModified())
        {
            String input = readFile(f.file);
            long t1, t0 = System.currentTimeMillis();

            try {

                //1) Add all dependencies, that will be needed, i.e Intrinsics
                final File intrinsicsFile = new File(new TemplateFile(this.dependenciesDir).fullPath + File.separator +
                        TemplateProcessor.INTRINSICS_FILE_NAME);

                addSingleDependency(new TemplateFile(intrinsicsFile), templateOptions);

                //2) Apply velocity : if TemplateOptions.isDoNotGenerateKType() or TemplateOptions.isDoNotGenerateVType() throw a
                //DoNotGenerateTypeException , do not generate the final file.

                //set current file !
                templateOptions.sourceFile = f.file;

                input = filterVelocity(f, input, templateOptions);

                this.timeVelocity += (t1 = System.currentTimeMillis()) - t0;
                displayProgressBar();

                //3) Apply generix inlining, (which inlines Intrinsics...)
                input = filterInlines(f, input, templateOptions);
                displayProgressBar();

                //4) convert signatures
                input = filterTypeClassRefs(f, input, templateOptions);
                this.timeTypeClassRefs += (t1 = System.currentTimeMillis()) - t0;
                displayProgressBar();

                //5) Filter comments
                input = filterComments(f, input, templateOptions);
                this.timeComments += (t0 = System.currentTimeMillis()) - t1;
                displayProgressBar();

                output.updated = true;
                saveFile(output.file, input);
                displayProgressBar();
            }
            catch (final ParseErrorException e) {

                System.out.println("[ERROR] : parsing template '" + f.fullPath +
                        "' with " + templateOptions + " with error: '" + e.getMessage() + "'");

                //rethrow the beast to stop the thing dead.
                throw e;
            }
            catch (final ResourceNotFoundException e) {

                System.out.println("[ERROR] : resource not found for template '" + f.fullPath +
                        "' with " + templateOptions + " with error: '" + e.getMessage() + "'");

                //rethrow the beast to stop the thing dead.
                throw e;
            }
            catch (final MethodInvocationException e) {

                if (e.getCause() instanceof DoNotGenerateTypeException) {

                    final DoNotGenerateTypeException doNotGenException = (DoNotGenerateTypeException) e.getCause();
                    //indeed remove the generated file
                    output.file.delete();
                    outputs.remove(output);

                    if (this.verbose) {

                        System.out.println("[INFO] : output from template '" + f.fullPath +
                                "' with KType = " + doNotGenException.currentKType + " and VType =  " +
                                doNotGenException.currentVType + " was bypassed...");
                    }
                    else {
                        displayProgressBar();
                    }
                }
                else {

                    System.out.println("[ERROR] : method invocation from template '" + f.fullPath +
                            "' with " + templateOptions + " failed with error: '" + e.getMessage() + "'");

                    //rethrow the beast to stop the thing dead.
                    throw e;
                }

            } //end MethodInvocationException
        }
    }

    private String filterInlines(final TemplateFile f, String input, final TemplateOptions templateOptions)
    {
        if (templateOptions.hasKType()) {

            input = filterInlinesSet(f, input, templateOptions.inlineKTypeDefinitions);
        }

        if (templateOptions.hasVType()) {

            input = filterInlinesSet(f, input, templateOptions.inlineVTypeDefinitions);
        }

        return input;
    }

    private String filterInlinesSet(final TemplateFile f, String input, final HashMap<String, String> inlineTypeDefinitions) {

        final StringBuffer sb = new StringBuffer();

        //1) Make 2 passes max.
        int nbRemainingPasses = 2;

        while (nbRemainingPasses >= 1) {

            nbRemainingPasses--;

            //Attempt on each of inlineDefinitions
            for (final String callName : inlineTypeDefinitions.keySet()) {

                String bodyPattern = "";

                final Pattern p = Pattern.compile(callName, Pattern.MULTILINE | Pattern.DOTALL);

                //flush
                sb.setLength(0);

                //0) First, check for imports
                Matcher m = TemplateProcessor.IMPORT_DECLARATION.matcher(input);

                while (m.find())
                {
                    //append what is before
                    sb.append(input.substring(0, m.end()));

                    //truncate after the found region
                    input = input.substring(m.end());

                    //restart search :
                    m = TemplateProcessor.IMPORT_DECLARATION.matcher(input);
                }

                //1) Search for the pattern
                while (true)
                {
                    m = p.matcher(input);

                    //end if found matcher
                    if (m.find())
                    {
                        if (this.verbose) {

                            System.out.println("[INFO] filterInlines(): Pattern finding '" +
                                    m.toString() + "'...");

                            System.out.println("[INFO] filterInlines(): found matching for '" + callName +
                                    "' in file '" + f.fullPath + "' with " + inlineTypeDefinitions.get(callName));
                        }

                        sb.append(input.substring(0, m.start()));

                        int bracketCount = 0;
                        int last = m.end();
                        final ArrayList<String> params = new ArrayList<String>();

                        //go back 1 character to capture the first parenthesis again
                        outer: for (int i = m.end() - 1; i < input.length(); i++)
                        {
                            switch (input.charAt(i))
                            {
                                case '(':
                                    bracketCount++;
                                    break;
                                case ')':
                                    bracketCount--;
                                    if (bracketCount == 0)
                                    {
                                        params.add(input.substring(last, i).trim());

                                        input = input.substring(i + 1);
                                        break outer;
                                    }
                                    break;
                                case ',':
                                    if (bracketCount == 1)
                                    {
                                        //add a parameter
                                        params.add(input.substring(last, i));

                                        last = i + 1;
                                    }
                                    break;
                            }
                        } //end for

                        //fill-in the arguments depending of the type
                        bodyPattern = "";

                        bodyPattern = inlineTypeDefinitions.get(callName);

                        //apply replacement, if not null
                        if (!bodyPattern.isEmpty()) {

                            try {
                                if (params.size() > 0) {

                                    sb.append(String.format("(" + bodyPattern + ")", params.toArray()));

                                    if (this.verbose) {
                                        System.out.println("[INFO] filterInlines(): Applying inlined body '" +
                                                bodyPattern + "' to args: '" + params.toString() + "'...");
                                    }
                                }
                                else {
                                    //the method has no arguments
                                    sb.append("(" + bodyPattern + ")");

                                    if (this.verbose) {
                                        System.out.println("[INFO] filterInlines(): Applying inlined body '" +
                                                bodyPattern + "' with no args...");
                                    }
                                }
                            }
                            catch (final UnknownFormatConversionException e) {

                                throw new ParseErrorException("[ERROR] filterInlines(): unable to apply body '" + bodyPattern + "' with params '" + params.toString() + "'...");
                            }
                        }
                    } //else m.find()
                    else
                    {
                        //not found, append the contents directly
                        sb.append(input);
                        break;
                    }
                } //end while true

                //re-process the same input for each method call
                input = sb.toString();

            } //end for call names
        } //end while

        return input.toString();
    }

    private String filterComments(final TemplateFile f, final String input, final TemplateOptions templateOptions)
    {
        final Pattern p = Pattern.compile("(/\\*!)|(!\\*/)", Pattern.MULTILINE | Pattern.DOTALL);

        return p.matcher(input).replaceAll("");
    }

    private String filterTypeClassRefs(final TemplateFile f, String input, final TemplateOptions options)
    {
        input = unifyTypeWithSignature(f, input, options);
        input = rewriteSignatures(f, input, options);
        input = rewriteLiterals(f, input, options);
        return input;
    }

    private String unifyTypeWithSignature(final TemplateFile f, final String input,
            final TemplateOptions options)
    {
        // This is a hack. A better way would be a full source AST and
        // rewrite at the actual typeDecl level.
        // KTypePredicate<? super VType> => VTypePredicate<? super VType>
        return input.replaceAll("(KType)(?!VType)([A-Za-z]+)(<(?:(\\? super ))?VType>)", "VType$2$3");
    }

    private String rewriteSignatures(final TemplateFile f, final String input, final TemplateOptions options)
    {
        final Pattern p = Pattern.compile("<[\\?A-Z]");
        final Matcher m = p.matcher(input);

        final StringBuilder sb = new StringBuilder();
        int fromIndex = 0;
        while (m.find(fromIndex))
        {
            final int next = m.start();
            int end = next + 1;
            int bracketCount = 1;
            while (bracketCount > 0 && end < input.length())
            {
                switch (input.charAt(end++)) {
                    case '<':
                        bracketCount++;
                        break;
                    case '>':
                        bracketCount--;
                        break;
                }
            }
            sb.append(input.substring(fromIndex, next));
            sb.append(rewriteSignature(input.substring(next, end), options));
            fromIndex = end;
        }
        sb.append(input.substring(fromIndex, input.length()));
        return sb.toString();
    }

    private String rewriteSignature(final String signature, final TemplateOptions options)
    {
        if (!signature.contains("KType") && !signature.contains("VType")) {
            return signature;
        }

        final Pattern p = Pattern.compile("<[^<>]*>", Pattern.MULTILINE | Pattern.DOTALL);

        final StringBuilder sb = new StringBuilder(signature);
        Matcher m = p.matcher(sb);
        while (m.find())
        {
            String group = m.group();
            group = group.substring(1, group.length() - 1);
            final List<String> args = new ArrayList<String>(Arrays.asList(group.split(",")));
            final StringBuilder b = new StringBuilder();
            for (final Iterator<String> i = args.iterator(); i.hasNext();)
            {
                String arg = i.next().trim();

                if (options.isKTypePrimitive())
                {
                    if (options.hasKType() && isGenericOnly(arg, "KType")) {

                        arg = "";
                    }
                    else {
                        arg = arg.replace("KType", options.getKType().getBoxedType());
                    }
                }

                if (options.hasVType() && options.isVTypePrimitive())
                {
                    if (isGenericOnly(arg, "VType")) {
                        arg = "";
                    }
                    else {
                        arg = arg.replace("VType", options.getVType().getBoxedType());
                    }
                }

                if (arg.length() > 0)
                {
                    if (b.length() > 0) {
                        b.append(", ");
                    }
                    b.append(arg.trim());
                }
            }

            if (b.length() > 0)
            {
                b.insert(0, '{');
                b.append('}');
            }

            sb.replace(m.start(), m.end(), b.toString());
            m = p.matcher(sb);
        }
        return sb.toString().replace('{', '<').replace('}', '>');
    }

    private boolean isGenericOnly(final String arg, final String type)
    {
        return arg.equals(type) || arg.equals("? super " + type) || arg.equals("? extends " + type);
    }

    private String rewriteLiterals(final TemplateFile f, String input, final TemplateOptions options)
    {
        final Type k = options.getKType();

        if (options.hasVType())
        {
            final Type v = options.getVType();

            input = input.replaceAll("(KTypeVType)([A-Z][a-zA-Z]*)(<.+?>)?",
                    (k.isGeneric() ? "Object" : k.getBoxedType()) +
                            (v.isGeneric() ? "Object" : v.getBoxedType()) +
                            "$2" +
                            (options.isAnyGeneric() ? "$3" : ""));

            input = input.replaceAll("(VType)([A-Z][a-zA-Z]*)",
                    (v.isGeneric() ? "Object" : v.getBoxedType()) + "$2");

            if (!v.isGeneric()) {
                input = input.replaceAll("VType", v.getType());
            }
        }

        input = input.replaceAll("(KType)([A-Z][a-zA-Z]*)(<.+?>)?",
                k.isGeneric() ? "Object" + "$2$3" : k.getBoxedType() + "$2");

        if (!k.isGeneric()) {
            input = input.replaceAll("KType", k.getType());
        }

        return input;
    }

    private void saveFile(final File file, final String input)
    {
        try
        {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            final FileOutputStream fos = new FileOutputStream(file);
            fos.write(input.getBytes("UTF-8"));
            fos.close();
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Apply velocity to the input.
     */
    private String filterVelocity(final TemplateFile f, final String template, final TemplateOptions options)
    {
        final StringWriter sw = new StringWriter();

        this.velocity.evaluate(options.context, sw, f.file.getName(), template);

        return sw.toString();
    }

    /**
     * 
     */
    private String readFile(final File file)
    {
        try
        {
            final byte[] contents = new byte[(int) file.length()];
            final DataInputStream dataInputStream = new DataInputStream(new FileInputStream(
                    file));
            dataInputStream.readFully(contents);
            dataInputStream.close();
            return new String(contents, "UTF-8");
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private OutputFile findOrCreate(final String targetFileName, final List<OutputFile> outputs)
    {
        final File candidate = TemplateProcessor.canonicalFile(new File(this.outputDir, targetFileName));
        for (final OutputFile o : outputs)
        {
            if (o.file.equals(candidate))
            {
                o.generated = true;
                return o;
            }
        }

        final OutputFile o = new OutputFile(candidate, true);
        outputs.add(o);
        return o;
    }

    private String targetFileName(String relativePath, final TemplateOptions templateOptions)
    {
        if (templateOptions.hasKType()) {

            relativePath = relativePath.replace("KType", templateOptions.getKType().getBoxedType());
        }

        if (templateOptions.hasVType()) {

            relativePath = relativePath.replace("VType", templateOptions.getVType().getBoxedType());
        }

        return relativePath;
    }

    /**
     * Relative path name.
     */
    private String relativePath(final File sub, final File parent)
    {
        try
        {
            return sub.getCanonicalPath().toString()
                    .substring(parent.getCanonicalPath().length());
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Collect files present in the output.
     */
    private List<OutputFile> collectOutputFiles(final List<OutputFile> list, final File dir)
    {
        if (!dir.exists()) {
            return list;
        }

        for (final File file : dir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(final File dir, final String name)
            {
                final File f = new File(dir, name);
                if (f.isDirectory())
                {
                    collectOutputFiles(list, f);
                    return false;
                }

                return name.endsWith(".java");
            }
        }))
        {
            list.add(new OutputFile(file, false));
        }
        return list;
    }

    /**
     * Collect all template files from this and subdirectories.
     */
    private List<TemplateFile> collectTemplateFiles(final List<TemplateFile> list, final File dir)
    {
        for (final File file : dir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(final File dir, final String name)
            {
                final File f = new File(dir, name);

                //if recursiveSearch is on, search in the sub-dirs
                //but even with false;
                if (f.isDirectory())
                {
                    collectTemplateFiles(list, f);
                    return false;
                }

                return name.endsWith(".java");
            }
        }))
        {
            list.add(new TemplateFile(file));
        }
        return list;
    }

    /**
     * Collect all template files from this current Dir only.
     */
    private List<TemplateFile> collectTemplateFilesCurrentDir(final List<TemplateFile> list, final File dir)
    {
        for (final File file : dir.listFiles(new FilenameFilter()
        {
            @Override
            public boolean accept(final File dir, final String name)
            {
                final File f = new File(dir, name);

                return name.endsWith(".java");
            }
        }))
        {
            list.add(new TemplateFile(file));
        }
        return list;
    }

    public static File canonicalFile(final File target)
    {
        try
        {
            return target.getCanonicalFile();
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void displayProgressBar() {

        final long currentTime = System.currentTimeMillis();

        if (currentTime - this.progressBarLastDisplayDate > 100) {

            this.progressBarCount = (this.progressBarCount + 1) % TemplateProcessor.PROGRESS_BAR_PATTERN.length;
            //display a line made of dots and advancing dot by dot, by steps of 100ms
            System.out.print("\b\b." + TemplateProcessor.PROGRESS_BAR_PATTERN[this.progressBarCount]);

            this.progressBarLastDisplayDate = currentTime;
        }
    }

    /**
     * Command line entry point.
     */
    public static void main(final String[] args)
    {
        final TemplateProcessor processor = new TemplateProcessor();
        processor.setTemplatesDir(new File(args[0]));
        processor.setDestDir(new File(args[1]));

        if (args.length >= 3) {

            processor.setDependenciesDir(new File(args[2]));
        }

        processor.execute();
    }
}