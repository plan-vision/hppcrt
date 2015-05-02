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
import java.util.Map.Entry;
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
import org.apache.velocity.tools.generic.DisplayTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.tools.generic.RenderTool;

import com.carrotsearch.hppcrt.generator.TemplateOptions.DoNotGenerateTypeException;

/**
 * Template processor for HPPC-RT templates.
 */
public final class TemplateProcessor {
  private static final Pattern COMMENTS_PATTERN = Pattern
      .compile("(/\\*!)|(!\\*/)", Pattern.MULTILINE | Pattern.DOTALL);

  private static final Pattern SIGNATURE_PATTERN = Pattern.compile("<[^<>]*>", Pattern.MULTILINE | Pattern.DOTALL);

  private static final Pattern SIGNATURE_START = Pattern.compile("<[\\?A-Z]");

  public enum VerboseLevel {
    off, min, medium, full;
  }

  //by default, generate everything, talk as much as possible
  private VerboseLevel verbose = VerboseLevel.full;
  private boolean incremental = false;

  private File templatesDir;
  private File dependenciesDir;
  private File outputDir;

  private long timeVelocity, timeInlines, timeTypeClassRefs, timeComments;

  private VelocityEngine velocity;

  private int progressBarCount;
  private long progressBarLastDisplayDate;

  private boolean isVelocityInitialized = false;

  private class VelocityLogger implements LogChute {
    @Override
    public void init(final RuntimeServices rs) throws Exception {
      // nothing strange here
    }

    @Override
    public void log(final int level, final String message) {

      if (level <= 2) {

        System.out.println("[VELOCITY]-" + level + " : " + message);
      }
    }

    @Override
    public void log(final int level, final String message, final Throwable t) {

      if (level <= 2) {

        System.out.println("[VELOCITY]-" + level + "-!!EXCEPTION!! : " + message + " , exception msg: "
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
   * Initialize Velocity engine.
   */
  public TemplateProcessor() {
    //do nothing here.
  }

  private void initVelocity() {

    if (!this.isVelocityInitialized) {

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

      this.isVelocityInitialized = true;
    }
  }

  /**
   * 
   */
  public void setVerbose(final VerboseLevel verbose) {
    this.verbose = verbose;
  }

  /**
   * 
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
   * 
   */
  public void execute() {

    System.out.println("[INFO] Incremental generation : " + this.incremental);
    System.out.println("[INFO] Verbose level : " + this.verbose);

    initVelocity();

    // Collect files/ checksums from the output folder.
    final List<OutputFile> outputs = collectOutputFiles(new ArrayList<OutputFile>(), this.outputDir);

    // Collect template files in the input folder, recursively
    final List<TemplateFile> inputs = collectTemplateFiles(new ArrayList<TemplateFile>(), this.templatesDir);

    // Process templates
    System.out.println("Processing " + inputs.size() + " templates to: '" + this.outputDir.getPath() + "'\n");
    final long start = System.currentTimeMillis();
    processTemplates(inputs, outputs);
    final long end = System.currentTimeMillis();
    System.out.println(String.format(Locale.ENGLISH, "\nProcessed in %.2f sec.\n", (end - start) * 1e-3));

    // Remove non-marked files.
    int generated = 0;
    int updated = 0;
    int deleted = 0;
    for (final OutputFile f : outputs) {
      if (!f.generated) {
        deleted++;
        if (isVerboseEnabled(VerboseLevel.min)) {
          System.out.println("Deleted: " + f.file);
        }
        f.file.delete();
      }

      if (f.generated) {
        generated++;
      }

      if (f.updated) {
        updated++;
        if (isVerboseEnabled(VerboseLevel.min)) {
          System.out.println("Updated: " + relativePath(f.file, this.outputDir));
        }
      }
    }

    System.out.println("Generated " + generated + " files (" + updated + " updated, " + deleted + " deleted).");
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
    ctx.put("ctxt", new ContextTool());
    ctx.put("convt", new ConversionTool());
    ctx.put("matht", new MathTool());
    ctx.put("numbert", new NumberTool());
    ctx.put("rendert", new RenderTool());
    ctx.put("dispt", new DisplayTool());

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

          options.setVerbose(isVerboseEnabled(VerboseLevel.full));

          generate(f, outputs, options);
        }
      }
      //B) (KType * VType) specializations
      else if (fileName.contains("KType") && fileName.contains("VType")) {
        for (final Type ktype : Type.values()) {
          for (final Type vtype : Type.values()) {
            final TemplateOptions options = new TemplateOptions(ktype, vtype);

            options.setVerbose(isVerboseEnabled(VerboseLevel.full));

            generate(f, outputs, options);

          }
        }
      }
    }

    if (isVerboseEnabled(VerboseLevel.min)) {
      System.out.println(String.format("\nVelocity: %.1f s\nInlines: %.1f s\nTypeClassRefs: %.1f s\nComments: %.1f s",
          this.timeVelocity * 1e-3, this.timeInlines * 1e-3, this.timeTypeClassRefs * 1e-3, this.timeComments * 1e-3));
    }
  }

  /**
   * Apply templates.
   */
  private void generate(final TemplateFile f, final List<OutputFile> outputs, final TemplateOptions templateOptions) {
    final String targetFileName = targetFileName(relativePath(f.file, this.templatesDir), templateOptions);

    final OutputFile output = findOrCreate(targetFileName, outputs);

    if (!this.incremental || !output.file.exists() || output.file.lastModified() <= f.file.lastModified()) {
      String input = readFile(f.file);
      long t1, t0 = System.currentTimeMillis();

      try {

        //1) Apply velocity : if TemplateOptions.isDoNotGenerateKType() or TemplateOptions.isDoNotGenerateVType() throw a
        //DoNotGenerateTypeException , do not generate the final file.
        t0 = System.currentTimeMillis();
        input = filterVelocity(f, input, templateOptions);

        this.timeVelocity += (t1 = System.currentTimeMillis()) - t0;

        //2) Apply generic inlining, (which inlines Intrinsics...)
        t0 = System.currentTimeMillis();
        input = filterInlines(f, input, templateOptions);
        this.timeInlines += (t1 = System.currentTimeMillis()) - t0;

        //3) convert signatures
        t0 = System.currentTimeMillis();
        input = filterTypeClassRefs(f, input, templateOptions);
        this.timeTypeClassRefs += (t1 = System.currentTimeMillis()) - t0;

        //4) Filter comments
        t0 = System.currentTimeMillis();
        input = filterComments(f, input, templateOptions);
        this.timeComments += (t1 = System.currentTimeMillis()) - t0;

        output.updated = true;
        saveFile(output.file, input);

      } catch (final ParseErrorException e) {

        System.out.println("[ERROR] : parsing template '" + f.fullPath + "' with " + templateOptions + " with error: '"
            + e.getMessage() + "'");

        //rethrow the beast to stop the thing dead.
        throw e;
      } catch (final ResourceNotFoundException e) {

        System.out.println("[ERROR] : resource not found for template '" + f.fullPath + "' with " + templateOptions
            + " with error: '" + e.getMessage() + "'");

        //rethrow the beast to stop the thing dead.
        throw e;
      } catch (final MethodInvocationException e) {

        if (e.getCause() instanceof DoNotGenerateTypeException) {

          final DoNotGenerateTypeException doNotGenException = (DoNotGenerateTypeException) e.getCause();
          //indeed remove the generated file
          output.file.delete();
          outputs.remove(output);

          if (isVerboseEnabled(VerboseLevel.medium)) {

            System.out.println("[INFO] : output from template '" + f.fullPath + "' with KType = "
                + doNotGenException.currentKType + " and VType =  " + doNotGenException.currentVType
                + " was bypassed...");
          }
        } else {

          System.out.println("[ERROR] : method invocation from template '" + f.fullPath + "' with " + templateOptions
              + " failed with error: '" + e.getMessage() + "'");

          //rethrow the beast to stop the thing dead.
          throw e;
        }

      } //end MethodInvocationException
    }
  }

  private String filterInlines(final TemplateFile f, String input, final TemplateOptions templateOptions) {
    if (templateOptions.hasKType() && !templateOptions.inlineKTypeDefinitions.isEmpty()) {

      input = filterInlinesSet(f, input, templateOptions.inlineKTypeDefinitions);
    }

    if (templateOptions.hasVType() && !templateOptions.inlineVTypeDefinitions.isEmpty()) {

      input = filterInlinesSet(f, input, templateOptions.inlineVTypeDefinitions);
    }

    return input;
  }

  private String filterInlinesSet(final TemplateFile f, final String input,
      final HashMap<String, InlinedMethodDef> inlineTypeDefinitions) {

    final StringBuilder sb = new StringBuilder();
    final StringBuilder currentInput = new StringBuilder(input);

    //1) Execute in passes until no more inlining occurs
    //(which means inlines defs can contain inlinable defs and so on)
    boolean continueProcessing = true;

    while (continueProcessing) {

      continueProcessing = false;

      //Attempt on each of inlineDefinitions
      for (final Entry<String, InlinedMethodDef> inlinedMethod : inlineTypeDefinitions.entrySet()) {

        String bodyPattern = "";

        final Pattern p = inlinedMethod.getValue().compiledCallName;

        //flush
        sb.setLength(0);

        //1) Search for the pattern
        while (true) {
          final Matcher m = p.matcher(currentInput);

          //end if found matcher
          if (m.find()) {
            continueProcessing = true;

            if (isVerboseEnabled(VerboseLevel.full)) {

              System.out.println("[INFO] filterInlines(): found matching for '" + inlinedMethod.getKey()
                  + "' in file '" + f.fullPath + "' with " + inlinedMethod.getValue());
            }

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

            //fill-in the arguments depending of the type
            bodyPattern = inlinedMethod.getValue().getBody();

            //apply replacement, if not null
            if (!bodyPattern.isEmpty()) {

              try {
                if (params.size() > 0) {

                  sb.append(String.format("(" + bodyPattern + ")", params.toArray()));

                  if (isVerboseEnabled(VerboseLevel.full)) {
                    System.out.println("[INFO] filterInlines(): Applying inlined body '" + bodyPattern + "' to args: '"
                        + params.toString() + "'...");
                  }
                } else {
                  //the method has no arguments, simply pass the bodyPattern with no transform
                  sb.append("(");
                  sb.append(bodyPattern);
                  sb.append(")");

                  if (isVerboseEnabled(VerboseLevel.full)) {
                    System.out.println("[INFO] filterInlines(): Applying inlined body '" + bodyPattern
                        + "' with no args...");
                  }
                }
              } catch (final UnknownFormatConversionException e) {

                throw new ParseErrorException("[ERROR] filterInlines(): unable to apply body '" + bodyPattern
                    + "' with params '" + params.toString() + "'...");
              }
            }
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
    } //end while

    return currentInput.toString();
  }

  private String filterComments(final TemplateFile f, final String input, final TemplateOptions templateOptions) {
    return TemplateProcessor.COMMENTS_PATTERN.matcher(input).replaceAll("");
  }

  private String filterTypeClassRefs(final TemplateFile f, String input, final TemplateOptions options) {
    input = unifyTypeWithSignature(f, input, options);
    input = rewriteSignatures(f, input, options);
    input = rewriteLiterals(f, input, options);
    return input;
  }

  private String unifyTypeWithSignature(final TemplateFile f, final String input, final TemplateOptions options) {
    // This is a hack. A better way would be a full source AST and
    // rewrite at the actual typeDecl level.
    // KTypePredicate<? super VType> => VTypePredicate<? super VType>
    return input.replaceAll("(KType)(?!VType)([A-Za-z]+)(<(?:(\\? super ))?VType>)", "VType$2$3");
  }

  private String rewriteSignatures(final TemplateFile f, final String input, final TemplateOptions options) {
    final Matcher m = TemplateProcessor.SIGNATURE_START.matcher(input);

    final StringBuilder sb = new StringBuilder();
    int fromIndex = 0;
    while (m.find(fromIndex)) {
      final int next = m.start();
      int end = next + 1;
      int bracketCount = 1;
      while (bracketCount > 0 && end < input.length()) {
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

  private String rewriteSignature(final String signature, final TemplateOptions options) {
    if (!signature.contains("KType") && !signature.contains("VType")) {
      return signature;
    }

    final StringBuilder sb = new StringBuilder(signature);

    Matcher m = TemplateProcessor.SIGNATURE_PATTERN.matcher(sb);

    while (m.find()) {
      String group = m.group();
      group = group.substring(1, group.length() - 1);

      final List<String> args = new ArrayList<String>(Arrays.asList(group.split(",")));

      final StringBuilder b = new StringBuilder();

      for (final Iterator<String> i = args.iterator(); i.hasNext();) {
        String arg = i.next().trim();

        if (options.isKTypePrimitive()) {
          if (options.hasKType() && isGenericOnly(arg, "KType")) {

            arg = "";
          } else {
            arg = arg.replace("KType", options.getKType().getBoxedType());
          }
        }

        if (options.hasVType() && options.isVTypePrimitive()) {
          if (isGenericOnly(arg, "VType")) {
            arg = "";
          } else {
            arg = arg.replace("VType", options.getVType().getBoxedType());
          }
        }

        if (arg.length() > 0) {
          if (b.length() > 0) {
            b.append(", ");
          }
          b.append(arg.trim());
        }
      }

      if (b.length() > 0) {
        b.insert(0, '{');
        b.append('}');
      }

      sb.replace(m.start(), m.end(), b.toString());

      m = TemplateProcessor.SIGNATURE_PATTERN.matcher(sb);
    }
    return sb.toString().replace('{', '<').replace('}', '>');
  }

  private boolean isGenericOnly(final String arg, final String type) {
    return arg.equals(type) || arg.equals("? super " + type) || arg.equals("? extends " + type);
  }

  private String rewriteLiterals(final TemplateFile f, String input, final TemplateOptions options) {
    final Type k = options.getKType();

    if (options.hasVType()) {
      final Type v = options.getVType();

      input = input.replaceAll("(KTypeVType)([A-Z][a-zA-Z]*)(<.+?>)?", (k.isGeneric() ? "Object" : k.getBoxedType())
          + (v.isGeneric() ? "Object" : v.getBoxedType()) + "$2" + (options.isAnyGeneric() ? "$3" : ""));

      input = input.replaceAll("(VType)([A-Z][a-zA-Z]*)", (v.isGeneric() ? "Object" : v.getBoxedType()) + "$2");

      if (!v.isGeneric()) {
        input = input.replaceAll("VType", v.getType());
      }
    }

    input = input.replaceAll("(KType)([A-Z][a-zA-Z]*)(<.+?>)?", k.isGeneric() ? "Object" + "$2$3" : k.getBoxedType()
        + "$2");

    if (!k.isGeneric()) {
      input = input.replaceAll("KType", k.getType());
    }

    return input;
  }

  private void saveFile(final File file, final String input) {
    try {
      if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
      }

      final FileOutputStream fos = new FileOutputStream(file);
      fos.write(input.getBytes("UTF-8"));
      fos.close();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
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

    //set current file as source file.
    options.sourceFile = f.file;

    this.velocity.evaluate(options.context, sw, f.file.getName(), template);

    return sw.toString();
  }

  /**
   * 
   */
  private String readFile(final File file) {
    try {
      final byte[] contents = new byte[(int) file.length()];

      final DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
      dataInputStream.readFully(contents);
      dataInputStream.close();
      return new String(contents, "UTF-8");
    } catch (final Exception e) {
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
   * Relative path name.
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

  private boolean isVerboseEnabled(final VerboseLevel lvl) {

    return lvl.ordinal() <= this.verbose.ordinal();
  }

  /**
   * Command line entry point run: [template source dir] [outpur dir] (option:
   * [additional template deps]) Also read the properties:
   * -Dincremental=[true|false], default false, -Dverbose=[off|min|medium|full],
   * default full
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
    processor.incremental = false;
    processor.verbose = VerboseLevel.full;

    try {

      processor.incremental = Boolean.valueOf(System.getProperty("incremental", "false"));
      processor.verbose = VerboseLevel.valueOf(System.getProperty("verbose", "full"));

    } catch (IllegalArgumentException | NullPointerException e) {

      System.out.println("ERROR in properties parsing : " + e.getLocalizedMessage());
    }

    processor.execute();
  }
}