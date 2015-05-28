package com.carrotsearch.hppcrt.generator.parser;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JDialog;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import com.carrotsearch.hppcrt.generator.TemplateOptions;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.CompilationUnitContext;

/** */
public class SignatureProcessor
{
    private static final int GUI_MAX_TITLE_SIZE = 150;

    /**
     * The input
     */
    private final String source;

    /**
     * The Parser instance
     */
    public final Java7Parser parser;

    /**
     * Stream of tokens created by the Lexer.
     */
    public final CommonTokenStream tokenStream;

    /**
     * The result Context of the parsing of tokenStream by the Parser.
     */
    private final CompilationUnitContext unitContext;

    /**
     * constructor from input, create the parser and parse the source.
     * @param input
     */
    public SignatureProcessor(final String input) {

        this.source = input;
        //Step 1: Lex and parse the inuput

        //1) create the Lexer to decompose into Tokens
        final Lexer lexer = new Java7Lexer(new ANTLRInputStream(input));
        //2) make a token stream of them for the Parser
        this.tokenStream = new CommonTokenStream(lexer);
        //3) Parse the tokens
        this.parser = new Java7Parser(this.tokenStream);

        //Richer than BailError
        this.parser.setErrorHandler(new DefaultErrorStrategy());

        //4) Make the parse result available through a CompilationUnit Context.
        this.unitContext = this.parser.compilationUnit();
    }

    /**
     * Main processing entry point: call to apply source file (template) conversions according to the current provided templateOptions
     * @return
     */
    public String process(final TemplateOptions templateOptions) {

        return applyReplacements(findReplacements(templateOptions), templateOptions);
    }

    /**
     * Display the parsed tree in a modeless GUI, useful for debugging.
     */
    public void displayParseTreeGui() {

        final String extractOfSource = this.source.substring(0, Math.min(this.source.length() - 1, SignatureProcessor.GUI_MAX_TITLE_SIZE));

        displayParseTree(this.unitContext, "Parse tree of : '" + extractOfSource + "'...");
    }

    /**
     * Step 2 : Compute the replacements using a Visitor-traversal of the parsed source using a SignatureReplacementVisitor.
     */
    private List<Replacement> findReplacements(final TemplateOptions templateOptions) {

        //Plug the SignatureVisitor into the final CompilationUnit context and start the Visitor traversal and computing.
        List<Replacement> replacements = ReplacementVisitorBase.NONE;

        replacements = this.unitContext.accept(new SignatureReplacementVisitor(templateOptions, this));

        //the result is a list of Replacement, i.e list of intervals of the original stream (in fact of the TokenStream), associated with a replacement string.
        return replacements;
    }

    /**
     * Step 3 : Apply all computed replacements into the original source.
     */
    protected String applyReplacements(final List<Replacement> replacements, final TemplateOptions options) {

        final StringWriter sw = new StringWriter();
        reconstruct(sw, this.tokenStream, 0, this.tokenStream.size() - 1, replacements, options);
        return sw.toString();
    }

    /**
     * Process references inside comment blocks, javadocs, etc.
     */
    protected String processComment(String text, final TemplateOptions options) {

        if (options.hasKType()) {
            text = text.replaceAll("(KType)(?=\\p{Lu})", options.getKType().getBoxedType());
            text = text.replace("KType", options.getKType().getType());
        }

        if (options.hasVType()) {
            text = text.replaceAll("(VType)(?=\\p{Lu})", options.getVType().getBoxedType());
            text = text.replace("VType", options.getVType().getType());
        }

        return text;
    }

    /**
     * Step 3-2: Low-level routine that applies replacements to a token stream.
     * Replacements are acting on whole tokens, not characters, replacing a given token by a computed
     * replacement string.
     * 
     */
    protected <T extends Writer> T reconstruct(
            final T sw,
            final BufferedTokenStream tokenStream,
            final int from, final int to,
            final Collection<Replacement> replacements,
            final TemplateOptions templateOptions) {

        try {

            if (replacements.size() > 0) {

                //1) Be safe by first ordering the replacements by their intervals.
                //so that we can replace stream-like from beginning to end.
                final ArrayList<Replacement> sorted = Replacement.sortList(replacements);

                //1-2) Control that replacements are consistent, i.e no one must overlap with a neigbour, else something
                //has gone wrong, and either way we wouldn't know how to replace that.
                for (int i = 1; i < sorted.size(); i++) {

                    final Replacement previous = sorted.get(i - 1);
                    final Replacement current = sorted.get(i);

                    if (!previous.interval.startsBeforeDisjoint(current.interval)) {
                        throw new RuntimeException("Overlapping intervals: {" + previous + "} is ovelapping the next {" + current + "}");
                    }
                }

                //Initialize [left = from...
                int left = from;

                //2) Start replacing
                for (final Replacement r : sorted) {

                    //2-1) We have [left ; right (beginning of a Replacement)[ a non-replaced range, copy the Tokens verbatim
                    //(with comments post-processing)
                    final int right = r.interval.a;

                    for (int i = left; i < right; i++) {
                        sw.append(tokenText(templateOptions, tokenStream.get(i)));
                    }

                    //2-2) Replace the Replacement interval tokens by a computed Replacement string
                    //(so multiple tokens may be replaced by a single string)
                    //[left; right[ +  replacement
                    sw.append(r.replacement);

                    //2-3) Prepare the next turn: [left; right[ +  replacement +  [next left (= starting after previous replacement)... [
                    left = r.interval.b + 1;
                }

                //3) No more replacements, copy verbatim all the remaining tokens of the stream.
                for (int i = left; i < to; i++) {
                    sw.append(tokenText(templateOptions, tokenStream.get(i)));
                }
            } else if (from >= to) {
                //1 token only
                sw.append(tokenText(templateOptions, tokenStream.get(from)));
            } else {
                //Null sized replacement, several tokens, copy verbatim [from; to[
                for (int i = from; i < to; i++) {
                    sw.append(tokenText(templateOptions, tokenStream.get(i)));
                }
            }
        } catch (final IOException e) {

            throw new RuntimeException(e);
        }

        return sw;
    }

    /**
     * Comments post-processing
     * @param templateOptions
     * @param token
     * @return
     */
    protected String tokenText(final TemplateOptions templateOptions, final Token token) {

        String text = token.getText();

        if (token.getChannel() == Java7Lexer.CHANNEL_COMMENT) {

            text = processComment(text, templateOptions);
        }
        return text;
    }

    /**
     * Open a modeless dialog that displays the Context
     * @param ctx
     * @param title
     */
    private void displayParseTree(final ParserRuleContext ctx, final String title) {

        //show AST in GUI
        final Future<JDialog> dialog = ctx.inspect(this.parser);

        try {
            dialog.get().setTitle(title);

        } catch (InterruptedException | ExecutionException e) {
            //nothing
            e.printStackTrace();
        }

    }
}
