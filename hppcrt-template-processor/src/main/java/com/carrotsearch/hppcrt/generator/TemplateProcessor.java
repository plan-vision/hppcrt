package com.carrotsearch.hppcrt.generator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UnknownFormatConversionException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

import com.carrotsearch.hppcrt.generator.TemplateOptions.DoNotGenerateTypeException;
import com.carrotsearch.hppcrt.generator.parser.SignatureProcessor;

/**
 * Template processor for HPPC-RT templates.
 */
public final class TemplateProcessor
{
    private static final Pattern COMMENTS_PATTERN = Pattern.compile("(/\\*!)|(!\\*/)", Pattern.MULTILINE | Pattern.DOTALL);

    /**
     * Be default, print everything !
     */
    private Level verbose = Level.ALL;

    private boolean incremental = false;

    private File templatesDir;
    private File dependenciesDir;
    private File outputDir;

    private long timeVelocity, timeInlines, timeTypeClassRefs, timeComments;

    private VelocityEngine velocity;

    /**
     * Logger handler for Velocity
     *
     */
    private class VelocityLogger implements LogChute
    {
        @Override
        public void init(final RuntimeServices rs) throws Exception {
            // nothing strange here
        }

        @Override
        public void log(final int level, final String message) {

            if (level <= 2) {

                logConf("[VELOCITY]-" + level + " : " + message);
            }
        }

        @Override
        public void log(final int level, final String message, final Throwable t) {

            if (level <= 2) {

                logConf("[VELOCITY]-" + level + "-!!EXCEPTION!! : " + message + " , exception msg: "
                        + t.getMessage());
            }
        }

        @Override
        public boolean isLevelEnabled(final int level) {
            //let it  talk
            return true;
        }
    }

    /**
     * java.util.Logger formatter to be used throughout the whole template-processor tool.
     */
    private static class ProcessorLogsFormatter extends Formatter
    {
        @Override
        public String format(final LogRecord record) {

            String msg = "";

            //We only want to log OUR messages, not every package around (especially NOT Swing/Awt FINE logs !)
            if (record.getSourceClassName().startsWith("com.carrotsearch.hppcrt")) {

                if (record.getThrown() != null) {

                    msg = String.format(Locale.ROOT, "[%1$s] %2$s with exception: %3$s%n", record.getLevel(), record.getMessage(), record.getThrown().getMessage());
                } else {
                    msg = String.format(Locale.ROOT, "[%1$s] %2$s%n", record.getLevel(), record.getMessage(), record.getThrown());
                }
            }

            return msg;
        }
    }

    /**
     * Hack to redirect ALWAYS to Std out !
     * @author Vincent
     *
     */
    private static class StdoutConsoleHandler extends ConsoleHandler
    {
        public StdoutConsoleHandler() {
            super();
            //nothing
        }

        @Override
        protected synchronized void setOutputStream(final OutputStream out) throws SecurityException {
            //Hack to log into System.out
            super.setOutputStream(System.out);
        }
    }

    /**
     * Initialize Velocity engine.
     */
    public TemplateProcessor() {
        //nothing there
    }

    private void initVelocity() {

        final ExtendedProperties p = new ExtendedProperties();

        //Velocity 2.0+ will have this option removed,
        //with the equivalent of SET_NULL_ALLOWED = true set permanently,
        //so better to get used to.
        p.setProperty(RuntimeConstants.SET_NULL_ALLOWED, "true");

        //Attach a Velocity logger to see internal Velocity log messages on console
        p.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new VelocityLogger());

        //Set resource path to be templatesDir + dependenciesDir, using Resource Loader:
        //log something when a resource is found
        p.setProperty(RuntimeConstants.RESOURCE_MANAGER_LOGWHENFOUND, "true");

        p.setProperty(RuntimeConstants.INPUT_ENCODING, "UTF-8");
        p.setProperty(RuntimeConstants.OUTPUT_ENCODING, "UTF-8");

        //set the templatesDir to search for the '#import'ed  files....
        p.setProperty("resource.loader", "file");

        //if the dependencies are not set, make them the same as the templates.
        if (this.dependenciesDir == null) {
            this.dependenciesDir = this.templatesDir;
        }

        p.setProperty("file.resource.loader.path", ". , " + new TemplateFile(this.templatesDir).fullPath + " , "
                + new TemplateFile(this.dependenciesDir).fullPath);

        p.setProperty("file.resource.loader.cache", "true");
        p.setProperty("file.resource.loader.modificationCheckInterval", "-1");

        //declare "#import" as a user directive:
        p.setProperty("userdirective", "com.carrotsearch.hppcrt.generator.ImportDirective");

        this.velocity = new VelocityEngine();
        this.velocity.setExtendedProperties(p);

        this.velocity.init();
    }

    /**
     * Also for Ant Task attribute 'verbose', MUST respect this naming.
     * @param verboseLevel
     */
    public void setVerbose(final String verboseLevel) {

        this.verbose = Level.parse(verboseLevel.toUpperCase());

        TemplateProcessor.setLoggerlevel(TemplateProcessor.getLog(), this.verbose);
    }

    /**
     * Also for Ant Task attribute 'incremental', MUST respect this naming.
     */
    public void setIncremental(final boolean incremental) {
        this.incremental = incremental;
    }

    /**
     * 
     */
    public void setDestDir(final File dir) {
        this.outputDir = dir;
    }

    /**
     * 
     */
    public void setTemplatesDir(final File dir) {
        this.templatesDir = dir;
    }

    public void setDependenciesDir(final File dir) {
        this.dependenciesDir = dir;
    }

    /**
     * Ant Task main entry point. (also used in main)
     */
    public void execute() {

        logConf("Incremental compilation : " + this.incremental);
        logConf("Verbose level : " + this.verbose);

        initVelocity();

        // Collect files/ checksums from the output folder.
        final List<OutputFile> outputs = collectOutputFiles(new ArrayList<OutputFile>(), this.outputDir);

        // Collect template files in the input folder, recursively
        final List<TemplateFile> inputs = collectTemplateFiles(new ArrayList<TemplateFile>(), this.templatesDir);

        // Process templates
        logConf("Processing " + inputs.size() + " templates to: '" + this.outputDir.getPath() + "'\n");
        final long start = System.currentTimeMillis();
        processTemplates(inputs, outputs);
        final long end = System.currentTimeMillis();
        logConf(String.format(Locale.ROOT, "\nProcessed in %.2f sec.\n", (end - start) * 1e-3));

        // Remove non-marked files.
        int generated = 0;
        int updated = 0;
        int deleted = 0;
        for (final OutputFile f : outputs) {
            if (!f.generated) {
                deleted++;

                log(Level.FINE, "Deleted: " + f.file);

                f.file.delete();
            }

            if (f.generated) {
                generated++;
            }

            if (f.updated) {
                updated++;

                log(Level.FINE, "Updated: " + relativePath(f.file, this.outputDir));

            }
        }

        logConf("Generated " + generated + " files (" + updated + " updated, " + deleted + " deleted).");
    }

    /**
     * init a new Velocity Context for a specific TemplateOptions
     */
    private void initTemplateOptionsContext(final TemplateOptions options) {

        final VelocityContext ctx = new VelocityContext();

        ctx.put("TemplateOptions", options);
        ctx.put("true", Boolean.TRUE);
        ctx.put("templateOnly", Boolean.FALSE);
        ctx.put("false", Boolean.FALSE);

        //reference the context itself into the TemplateOptions object
        options.context = ctx;
    }

    /**
     * Apply templates to <code>.ktype</code> files (single-argument).
     */
    private void processTemplates(final List<TemplateFile> inputs, final List<OutputFile> outputs) {

        //For each template file
        for (final TemplateFile f : inputs) {

            final String fileName = f.file.getName();

            //A) KType only specialization
            if (fileName.contains("KType") && !fileName.contains("VType")) {
                for (final Type t : Type.values()) {

                    final TemplateOptions options = new TemplateOptions(t, null);

                    options.setVerbose(this.verbose);
                    generate(f, outputs, options);

                }
            }
            //B) (KType * VType) specializations
            else if (fileName.contains("KType") && fileName.contains("VType")) {

                for (final Type ktype : Type.values()) {

                    for (final Type vtype : Type.values()) {

                        final TemplateOptions options = new TemplateOptions(ktype, vtype);

                        options.setVerbose(this.verbose);
                        generate(f, outputs, options);

                    }
                }
            }
        }

        log(Level.FINE, String.format("\nVelocity: %.1f s\nInlines: %.1f s\nTypeClassRefs: %.1f s\nComments: %.1f s",
                this.timeVelocity * 1e-3, this.timeInlines * 1e-3, this.timeTypeClassRefs * 1e-3, this.timeComments * 1e-3));

    }

    /**
     * Apply templates.
     */
    private void generate(final TemplateFile input, final List<OutputFile> outputs, final TemplateOptions templateOptions) {

        final String targetFileName = targetFileName(relativePath(input.file, this.templatesDir), templateOptions);

        final OutputFile output = findOrCreate(targetFileName, outputs);

        if (!this.incremental || !output.file.exists() || output.file.lastModified() <= input.file.lastModified()) {
            String template = readFile(input.file);

            long t1, t0 = System.currentTimeMillis();

            try {

                log(Level.FINE, "[" + templateOptions.ktype + "," + templateOptions.vtype + "] generate(), processing '"
                        + input.fullPath + "'...");

                //0) set current file as source file.
                templateOptions.templateFile = input.file;

                //1) Apply velocity : if TemplateOptions.isDoNotGenerateKType() or TemplateOptions.isDoNotGenerateVType() throw a
                //DoNotGenerateTypeException , do not generate the final file.
                t0 = System.currentTimeMillis();
                template = filterVelocity(input, template, templateOptions);

                this.timeVelocity += (t1 = System.currentTimeMillis()) - t0;

                //2) Apply generic inlining, (which inlines Intrinsics...)
                t0 = System.currentTimeMillis();
                template = filterInlines(input, template, templateOptions);
                this.timeInlines += (t1 = System.currentTimeMillis()) - t0;

                //3) Filter comments
                t0 = System.currentTimeMillis();
                template = filterComments(template);
                this.timeComments += (t1 = System.currentTimeMillis()) - t0;

                //4) convert signatures
                t0 = System.currentTimeMillis();
                template = filterTypeClassRefs(template, templateOptions);

                //5) Filter static tokens
                template = filterStaticTokens(template, templateOptions);
                this.timeTypeClassRefs += (t1 = System.currentTimeMillis()) - t0;

                output.updated = true;
                saveFile(output.file, template);
            } catch (final ParseErrorException e) {

                log(Level.SEVERE, "Velocity parsing template '" + input.fullPath + "' with " + templateOptions
                        + " with error: '" + e.getMessage() + "'");

                //rethrow the beast to stop the thing dead.
                throw e;
            } catch (final ResourceNotFoundException e) {

                log(Level.SEVERE, "resource not found for template '" + input.fullPath + "' with " + templateOptions
                        + " with error: '" + e.getMessage() + "'");

                //rethrow the beast to stop the thing dead.
                throw e;
            } catch (final MethodInvocationException e) {

                if (e.getCause() instanceof DoNotGenerateTypeException) {

                    final DoNotGenerateTypeException doNotGenException = (DoNotGenerateTypeException) e.getCause();
                    //indeed remove the generated file
                    output.file.delete();
                    outputs.remove(output);

                    log(Level.FINE, "output from template '" + input.fullPath + "' with KType = "
                            + doNotGenException.currentKType + " and VType =  " + doNotGenException.currentVType
                            + " was bypassed...");

                } else {

                    log(Level.SEVERE, "method invocation from template '" + input.fullPath + "' with "
                            + templateOptions + " failed with error: '" + e.getMessage() + "'");

                    //rethrow the beast to stop the thing dead.
                    throw e;
                }

            } catch (final Exception e) {

                log(Level.SEVERE, "Problem parsing template '" + input.fullPath + "' with "
                        + templateOptions + " failed with exception: '" + e.getMessage() + "'");

                //rethrow the beast to stop the thing dead.
                throw e;
            }
        }
    }

    private String filterInlines(final TemplateFile f, final String input, final TemplateOptions templateOptions) {

        final StringBuilder sb = new StringBuilder();
        final StringBuilder currentInput = new StringBuilder(input);

        //Attempt on each of inlineDefinitions
        for (final InlinedMethodDef inlinedMethod : templateOptions.inlineDefinitions) {

            //extract the method matcher pre-compiled Pattern
            final Pattern p = inlinedMethod.getMethodNameCompiledPattern();

            //flush
            sb.setLength(0);

            //1) Search for the pattern
            while (true) {

                final Matcher m = p.matcher(currentInput);

                //end if found matcher
                if (m.find()) {

                    String[] genericArgs = new String[0];

                    if (inlinedMethod.getGenericParameters().size() > 0) {

                        genericArgs = m.group("generic").split(",");

                        log(Level.FINE, "filterInlines(): found matching with generics : " + Arrays.deepToString(genericArgs));
                    }

                    log(Level.FINE, "filterInlines(): found matching (" + m.start() + "," + m.end() + ") for '" + inlinedMethod.getMethodName()
                            + "' in file '" + f.fullPath + "' as " + inlinedMethod.toString());

                    sb.append(currentInput, 0, m.start());

                    int bracketCount = 0;
                    int last = m.end();

                    final ArrayList<String> params = new ArrayList<String>();

                    //go back 1 character to capture the first parenthesis again
                    outer: for (int i = m.end() - 1; i < input.length(); i++) {
                        switch (currentInput.charAt(i)) {
                        case '(':
                            bracketCount++;
                            break;
                        case ')':
                            bracketCount--;
                            if (bracketCount == 0) {
                                params.add(currentInput.substring(last, i).trim());

                                currentInput.delete(0, i + 1);
                                break outer;
                            }
                            break;
                        case ',':
                            if (bracketCount == 1) {
                                //add a parameter
                                params.add(currentInput.substring(last, i));

                                last = i + 1;
                            }
                            break;
                        }
                    } //end for

                    //corner case
                    if (params.size() == 1 && params.get(0).trim().isEmpty()) {

                        params.clear();
                    }

                    log(Level.FINE,
                            "filterInlines(), parsed arguments " + params.toString() +
                            ", passed to inlinedMethod.computeInlinedForm(this.genericParameters = " + inlinedMethod.getGenericParameters() + ")...");

                    //fill-in the arguments depending of the type
                    final String result = inlinedMethod.computeInlinedForm(templateOptions, genericArgs, params);

                    //Try to parse recursively the result:
                    //this is OK because we parse from left to right, i.e from outer to inner functions
                    log(Level.FINE, "filterInlines(), ENTER attempt to filterInlines() recursively on text '" + result + "'...");

                    final String innerResult = filterInlines(f, result, templateOptions);

                    log(Level.FINE, "filterInlines(), EXIT attempt to filterInlines() recursively on text '" + result +
                            "' ==> '" + innerResult + "'");

                    sb.append(innerResult);

                } //else m.find()
                else {
                    //not found, append the contents directly
                    sb.append(currentInput);
                    break;
                }
            } //end while true

            //re-process the same input for each method call
            currentInput.setLength(0);
            currentInput.append(sb);
        } //end for call names

        return currentInput.toString();
    }

    /**
     * Cleanup the remaining 'void comments' blocks that are a byproduct of Velocity processing.
     * @param input
     * @return
     */
    private String filterComments(final String input) {
        return TemplateProcessor.COMMENTS_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Full AST signature processor
     * 
     * @param input
     * @param options
     * @return
     */
    private String filterTypeClassRefs(final String input, final TemplateOptions options) {
        try {

            final SignatureProcessor signatureProcessor = new SignatureProcessor(input);

            return signatureProcessor.process(options);

        } catch (final ParseCancellationException | IllegalArgumentException e) {

            TemplateProcessor.getLog().log(Level.SEVERE,
                    "Signature processor parsing failure for template '" + options.getTemplateFile() + "' ", e);

            throw e;
        }
    }

    private String filterStaticTokens(final String template, final TemplateOptions templateOptions) {

        return template.replace(TemplateOptions.TEMPLATE_FILE_TOKEN, templateOptions.getTemplateFile());
    }

    /**
     * Apply velocity to the input.
     */
    private String filterVelocity(final TemplateFile f, final String template, final TemplateOptions options) {

        //each Template file receives an independent Context,
        //so references don't leak from file to file....
        //(like in separate compilation idioms)
        initTemplateOptionsContext(options);

        final StringWriter sw = new StringWriter();

        this.velocity.evaluate(options.context, sw, f.file.getName(), template);

        return sw.toString();
    }

    /**
     * Read the whole file contents into a UTF-8 string.
     */
    private String readFile(final File file) {

        try {

            return new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())), StandardCharsets.UTF_8);

        } catch (final Exception e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * Save the whole String input in File as UTF-8 (replacing the existing contents)
     * @param file
     * @param input
     */
    private void saveFile(final File file, final String input) {

        try {
            Files.createDirectories(Paths.get(file.getParent()));

            Files.write(Paths.get(file.getAbsolutePath()), input.getBytes(StandardCharsets.UTF_8));

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OutputFile findOrCreate(final String targetFileName, final List<OutputFile> outputs) {

        final File candidate = TemplateProcessor.canonicalFile(new File(this.outputDir, targetFileName));
        for (final OutputFile o : outputs) {
            if (o.file.equals(candidate)) {
                o.generated = true;
                return o;
            }
        }

        final OutputFile o = new OutputFile(candidate, true);
        outputs.add(o);
        return o;
    }

    private String targetFileName(String relativePath, final TemplateOptions templateOptions) {

        if (templateOptions.hasKType()) {

            relativePath = relativePath.replace("KType", templateOptions.getKType().getBoxedType());
        }

        if (templateOptions.hasVType()) {

            relativePath = relativePath.replace("VType", templateOptions.getVType().getBoxedType());
        }

        return relativePath;
    }

    /**
     * Relative path name sub from a parent
     */
    private String relativePath(final File sub, final File parent) {

        try {
            return sub.getCanonicalPath().toString().substring(parent.getCanonicalPath().length());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Collect files present in the output.
     */
    private List<OutputFile> collectOutputFiles(final List<OutputFile> list, final File dir) {

        if (!dir.exists()) {
            return list;
        }

        for (final File file : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                final File f = new File(dir, name);
                if (f.isDirectory()) {
                    collectOutputFiles(list, f);
                    return false;
                }

                return name.endsWith(".java");
            }
        })) {
            list.add(new OutputFile(file, false));
        }
        return list;
    }

    /**
     * Collect all template files from this and subdirectories.
     */
    private List<TemplateFile> collectTemplateFiles(final List<TemplateFile> list, final File dir) {

        for (final File file : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                final File f = new File(dir, name);

                //if recursiveSearch is on, search in the sub-dirs
                //but even with false;
                if (f.isDirectory()) {
                    collectTemplateFiles(list, f);
                    return false;
                }

                return name.endsWith(".java");
            }
        })) {
            list.add(new TemplateFile(file));
        }
        return list;
    }

    public static File canonicalFile(final File target) {
        try {
            return target.getCanonicalFile();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a standard java.util.logger instance
     * @return
     */
    private static Logger getLog() {

        return Logger.getLogger(TemplateProcessor.class.getName());
    }

    /**
     * log shortcut
     * @param lvl
     * @param message
     */
    private void log(final Level lvl, final String message) {

        TemplateProcessor.getLog().log(lvl, message);
    }

    /**
     * Log config messages
     */
    private void logConf(final String message) {

        log(Level.CONFIG, message);
    }

    /**
     * Command line entry point: [template source dir] [output dir] (option:
     * [additional template deps]) Also read the properties:
     * -Dincremental=[true|false], default false, -Dverbose=[off|severe|info|config|warning|fine|finer|finest],
     * default config
     */
    public static void main(final String[] args) {

        final TemplateProcessor processor = new TemplateProcessor();

        processor.setTemplatesDir(new File(args[0]));
        processor.setDestDir(new File(args[1]));

        if (args.length >= 3) {

            processor.setDependenciesDir(new File(args[2]));
        } else {
            //if no dep, set it to be same as templatesDir
            processor.setDependenciesDir(processor.templatesDir);
        }

        //read properties
        try {
            processor.setIncremental(Boolean.valueOf(System.getProperty("incremental", "false")));
            processor.setVerbose(System.getProperty("verbose", Level.CONFIG.getName()));

        } catch (IllegalArgumentException | NullPointerException e) {

            processor.log(Level.SEVERE, "in properties parsing : " + e.getLocalizedMessage());
        }

        processor.execute();
    }

    /**
     * Utility method to set the log levels of all handlers and force formatting
     * @param logger
     * @param lvl
     */
    public static void setLoggerlevel(final Logger logger, final Level lvl) {

        Logger tempLogger = logger;

        while (tempLogger != null) {

            //replace all ConsoleHandler thats goes to Std.err
            //by ones that go to Std.out
            for (final Handler handler : tempLogger.getHandlers()) {

                if (handler.getClass() == ConsoleHandler.class) {

                    tempLogger.removeHandler(handler);
                    tempLogger.addHandler(new StdoutConsoleHandler());
                }
            }

            //force log levels and formatters for everyone
            tempLogger.setLevel(lvl);

            for (final Handler handler : tempLogger.getHandlers()) {
                handler.setLevel(lvl);
                handler.setFormatter(new ProcessorLogsFormatter());
            }

            tempLogger = tempLogger.getParent();
        }
    }
}