package com.carrotsearch.hppcrt.generator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

import com.carrotsearch.hppcrt.generator.TemplateOptions.DoNotGenerateTypeException;
import com.carrotsearch.hppcrt.generator.parser.SignatureProcessor;
import com.google.common.collect.ImmutableList;

/**
 * Template processor for HPPC-RT templates.
 */
public final class TemplateProcessor
{
    private static final Pattern COMMENTS_PATTERN = Pattern.compile("(/\\*!)|(!\\*/)", Pattern.MULTILINE | Pattern.DOTALL);

    private static final Logger logger = Logger.getLogger(TemplateProcessor.class.getName());

    private static final String[] EMPTY_GENERIC_ARGS = new String[0];

    /**
     * Be default, print everything !
     */
    private Level verbose = Level.ALL;

    private boolean incremental = false;

    private File templatesDir;
    private File dependenciesDir;
    private File outputDir;

    private Path templatesPath;
    private Path outputPath;

    private VelocityEngine velocity;

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
     * Constructor.
     */
    public TemplateProcessor() {
        //nothing there
    }

    private void initVelocity() {

        final Properties p = new Properties();

        //use a specific name as Velocity logger backend.
        p.setProperty(RuntimeConstants.RUNTIME_LOG_NAME, "TemplateProcessor");

        //Set resource path to be templatesDir + dependenciesDir, using Resource Loader:
        //log something when a resource is found
        p.setProperty(RuntimeConstants.RESOURCE_MANAGER_LOGWHENFOUND, "true");

        //set the templatesDir to search for the '#import'ed  files....

        //if the dependencies are not set, make them the same as the templates.
        if (this.dependenciesDir == null) {
            this.dependenciesDir = this.templatesDir;
        }

        p.setProperty("file.resource.loader.path", ". , " + Paths.get(this.templatesDir.getAbsolutePath()) + " , " + Paths.get(this.dependenciesDir.getAbsolutePath()));

        p.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, "true");
        p.setProperty("file.resource.loader.modificationCheckInterval", "-1");

        this.velocity = new VelocityEngine();
        this.velocity.setProperties(p);

        //declare "#import" as a custom directive for us
        this.velocity.loadDirective("com.carrotsearch.hppcrt.generator.ImportDirective");

        this.velocity.init();
    }

    /**
     * Also for Ant Task attribute 'verbose', MUST respect this naming.
     * @param verboseLevel
     */
    public void setVerbose(final String verboseLevel) {

        this.verbose = Level.parse(verboseLevel.toUpperCase());

        TemplateProcessor.setLoggerlevel(TemplateProcessor.logger, this.verbose);
    }

    /**
     * Also for Ant Task attribute 'incremental', MUST respect this naming.
     */
    public void setIncremental(final boolean incremental) {
        this.incremental = incremental;
    }

    /**
     * Also for Ant Task attribute 'destDir', MUST respect this naming.
     * @param dir
     */
    public void setDestDir(final File dir) {
        this.outputDir = dir;
    }

    /**
     * Also for Ant Task attribute 'templatesDir', MUST respect this naming.
     * @param dir
     */
    public void setTemplatesDir(final File dir) {
        this.templatesDir = dir;
    }

    /**
     * Also for Ant Task attribute 'dependenciesDir', MUST respect this naming.
     * @param dir
     */
    public void setDependenciesDir(final File dir) {
        this.dependenciesDir = dir;
    }

    /**
     * Ant Task main entry point. (also used in main)
     * @throws IOException
     */
    public void execute() throws IOException {

        logConf("Incremental compilation : " + this.incremental);
        logConf("Verbose level : " + this.verbose);

        initVelocity();

        this.templatesPath = this.templatesDir.toPath().toAbsolutePath().normalize();
        this.outputPath = this.outputDir.toPath().toAbsolutePath().normalize();

        // Collect files/ checksums from the output folder.
        final List<TemplateFile> templates = collectTemplateFiles(this.templatesPath);

        // Process templates
        logConf("Processing " + templates.size() + " templates to: '" + this.outputDir.getPath() + "'\n");
        final long start = System.currentTimeMillis();
        final List<OutputFile> generated = processTemplates(templates);
        final long end = System.currentTimeMillis();
        logConf(String.format(Locale.ROOT, "\nProcessed in %.2f sec.\n", (end - start) * 1e-3));

        // Remove all files != generated from outputPath
        final List<Path> removed = removeOtherFiles(this.outputPath, generated);

        int updated = generated.size();
        for (final OutputFile o : generated) {
            if (o.upToDate) {
                updated--;
            }
        }

        logConf("Generated " + generated.size() + " files (" + updated + " updated, " + removed.size() + " deleted).");
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
     * @throws IOException
     */
    private List<OutputFile> processTemplates(final List<TemplateFile> inputs) throws IOException {

        final List<OutputFile> outputs = new ArrayList<>();

        //For each template file
        for (final TemplateFile f : inputs) {

            final String fileName = f.getFileName();

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

        return outputs;
    }

    /**
     * Apply templates.
     * @throws IOException
     */
    private void generate(final TemplateFile input, final List<OutputFile> outputs, final TemplateOptions templateOptions) throws IOException {

        final String targetFileName = targetFileName(this.templatesPath.relativize(input.path).toString(), templateOptions);

        final OutputFile output = new OutputFile(this.outputPath.resolve(targetFileName).toAbsolutePath().normalize());

        if (this.incremental &&
                Files.exists(output.path) &&
                Files.getLastModifiedTime(output.path).toMillis() >= Files.getLastModifiedTime(input.path).toMillis()) {

            // No need to re-render but mark as generated.
            output.upToDate = true;
            outputs.add(output);
            return;
        }

        //Load file contents
        String template = new String(Files.readAllBytes(input.path), StandardCharsets.UTF_8);

        try {

            log(Level.FINE, "[" + templateOptions.ktype + "," + templateOptions.vtype + "] generate(), processing '"
                    + input.path + "'...");

            //0) set current file as source file.
            templateOptions.templateFile = input.path;

            //1) Apply velocity : if TemplateOptions.isDoNotGenerateKType() or TemplateOptions.isDoNotGenerateVType() throw a
            //DoNotGenerateTypeException , do not generate the final file.

            template = filterVelocity(input, template, templateOptions);

            //2) Apply generic inlining, (which inlines Intrinsics...)

            template = filterInlines(input, template, templateOptions);

            //3) Filter comments

            template = filterComments(template);

            //4) convert signatures

            template = filterTypeClassRefs(template, templateOptions);

            //5) Filter static tokens
            template = filterStaticTokens(template, templateOptions);

            //6) Commit the result to disk
            Files.createDirectories(output.path.getParent());
            Files.write(output.path, template.getBytes(StandardCharsets.UTF_8));

            outputs.add(output);

        } catch (final ParseErrorException e) {

            log(Level.SEVERE, "Velocity parsing template '" + input.getFileName() + "' with " + templateOptions
                    + " with error: '" + e.getMessage() + "'");

            //rethrow the beast to stop the thing dead.
            throw e;
        } catch (final ResourceNotFoundException e) {

            log(Level.SEVERE, "resource not found for template '" + input.getFileName() + "' with " + templateOptions
                    + " with error: '" + e.getMessage() + "'");

            //rethrow the beast to stop the thing dead.
            throw e;
        } catch (final MethodInvocationException e) {

            if (e.getCause() instanceof DoNotGenerateTypeException) {

                final DoNotGenerateTypeException doNotGenException = (DoNotGenerateTypeException) e.getCause();

                log(Level.FINE, "output from template '" + input.getFileName() + "' with KType = "
                        + doNotGenException.currentKType + " and VType =  " + doNotGenException.currentVType
                        + " was bypassed...");

            } else {

                log(Level.SEVERE, "method invocation from template '" + input.getFileName() + "' with "
                        + templateOptions + " failed with error: '" + e.getMessage() + "'");

                //rethrow the beast to stop the thing dead.
                throw e;
            }

        } catch (final Exception e) {

            log(Level.SEVERE, "Problem parsing template '" + input.getFileName() + "' with "
                    + templateOptions + " failed with exception: '" + e.getMessage() + "'");

            //rethrow the beast to stop the thing dead.
            throw e;
        }

    }

    private String filterInlines(final TemplateFile f, final String input, final TemplateOptions templateOptions) {

        final StringBuilder sb = new StringBuilder(input.length());
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

                    String[] genericArgs = TemplateProcessor.EMPTY_GENERIC_ARGS;

                    if (inlinedMethod.getGenericParameters().size() > 0) {

                        genericArgs = m.group("generic").split(",");

                        log(Level.FINE,
                                "filterInlines(): found matching with generics: '%s'", ImmutableList.copyOf(genericArgs));
                    }

                    log(Level.FINE,
                            "filterInlines(): found matching (%d,%d) for '%s' in file '%s' as %s",
                            m.start(), m.end(),
                            inlinedMethod.getMethodName(),
                            f.getFileName(), inlinedMethod);

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
                            "filterInlines(), parsed arguments '%s'"
                                    + ", passed to inlinedMethod.computeInlinedForm(this.genericParameters =  %s)... ",
                                    params, inlinedMethod.getGenericParameters());

                    //fill-in the arguments depending of the type
                    final String result = inlinedMethod.computeInlinedForm(templateOptions, genericArgs, params);

                    log(Level.FINE, "filterInlines() computeInlinedForm result ==> '%s'...", result);

                    //Try to parse recursively the result:
                    //this is OK because we parse from left to right, i.e from outer to inner functions

                    final String innerResult = filterInlines(f, result, templateOptions);

                    if (!innerResult.equals(result)) {

                        log(Level.FINE, "filterInlines() acting recursively on '%s' ==> '%s'... ", result, innerResult);
                    }

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

            TemplateProcessor.logger.log(Level.SEVERE,
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

        this.velocity.evaluate(options.context, sw, f.getFileName(), template);

        return sw.toString();
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

    private List<Path> scanFilesMatching(final Path dir, final String matchPattern) throws IOException {

        final List<Path> paths = new ArrayList<>();

        if (Files.isDirectory(dir)) {

            final PathMatcher matcher = dir.getFileSystem().getPathMatcher(matchPattern);

            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
                    if (matcher.matches(path)) {
                        paths.add(path);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } //end if isDirectory
        return paths;
    }

    /**
     * Collect all template files from this and subdirectories.
     */
    private List<TemplateFile> collectTemplateFiles(final Path dir) throws IOException {

        final List<TemplateFile> paths = new ArrayList<>();

        for (final Path path : scanFilesMatching(dir, "glob:**.java")) {

            paths.add(new TemplateFile(path));
        }

        return paths;
    }

    private List<Path> removeOtherFiles(final Path outputPath, final List<OutputFile> keep) throws IOException {

        final Set<String> keepPaths = new HashSet<>();
        for (final OutputFile o : keep) {
            keepPaths.add(o.path.toRealPath().toString());
        }

        final List<Path> toRemove = new ArrayList<>();
        Files.walkFileTree(outputPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, final BasicFileAttributes attrs) throws IOException {
                path = path.toRealPath();
                if (!keepPaths.contains(path.toString())) {
                    toRemove.add(path);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        for (final Path p : toRemove) {
            log(Level.FINE, "Deleting: " + p.toString());
            Files.delete(p);
        }

        return toRemove;
    }

    /**
     * log shortcut
     */
    private void log(final Level lvl, final String format, final Object... args) {

        //this check prevents complex toString() formatting
        if (TemplateProcessor.logger.isLoggable(this.verbose)) {

            if (args.length == 0) {
                TemplateProcessor.logger.log(lvl, format);
            } else {
                TemplateProcessor.logger.log(lvl, String.format(Locale.ROOT, format, args));
            }
        }
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
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {

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
