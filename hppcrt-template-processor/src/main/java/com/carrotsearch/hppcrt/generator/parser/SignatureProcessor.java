package com.carrotsearch.hppcrt.generator.parser;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import com.carrotsearch.hppcrt.generator.TemplateOptions;
import com.carrotsearch.hppcrt.generator.TemplateProcessor.VerboseLevel;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.CompilationUnitContext;

/** */
public class SignatureProcessor
{
    final Java7Parser parser;
    final CommonTokenStream tokenStream;
    final CompilationUnitContext unitContext;

    public SignatureProcessor(final String input) {
        final Lexer lexer = new Java7Lexer(new ANTLRInputStream(input));
        this.tokenStream = new CommonTokenStream(lexer);
        this.parser = new Java7Parser(this.tokenStream);
        this.parser.setErrorHandler(new BailErrorStrategy());
        this.unitContext = this.parser.compilationUnit();
    }

    /*
     * 
     */
    public String process(final TemplateOptions templateOptions) throws IOException {
        return applyReplacements(findReplacements(templateOptions), templateOptions);
    }

    /*
     * 
     */
    private List<Replacement> findReplacements(final TemplateOptions templateOptions) {
        final List<Replacement> replacements = this.unitContext.accept(new SignatureReplacementVisitor(templateOptions, this));
        return replacements;
    }

    /*
     * 
     */
    private String applyReplacements(final List<Replacement> replacements, final TemplateOptions options) throws IOException {
        final StringWriter sw = new StringWriter();
        reconstruct(sw, this.tokenStream, 0, this.tokenStream.size() - 1, replacements, options);
        return sw.toString();
    }

    /**
     * Process references inside comment blocks, javadocs, etc.
     */
    private String processComment(String text, final TemplateOptions options) {

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

    /*
     * 
     */
    public <T extends Writer> T reconstruct(
            final T sw,
            final BufferedTokenStream tokenStream,
            final int from, final int to,
            final Collection<Replacement> replacements,
            final TemplateOptions templateOptions) throws IOException {

        final ArrayList<Replacement> sorted = new ArrayList<>(replacements);

        Collections.sort(sorted, new Comparator<Replacement>() {
            @Override
            public int compare(final Replacement a, final Replacement b) {
                return Integer.compare(a.interval.a, b.interval.b);
            }
        });

        for (int i = 1; i < sorted.size(); i++) {
            final Replacement previous = sorted.get(i - 1);
            final Replacement current = sorted.get(i);
            if (!previous.interval.startsBeforeDisjoint(current.interval)) {
                throw new RuntimeException("Overlapping intervals: " + previous + " " + current);
            }
        }

        int left = from;
        for (final Replacement r : sorted) {
            final int right = r.interval.a;
            for (int i = left; i < right; i++) {
                sw.append(tokenText(templateOptions, tokenStream.get(i)));
            }
            sw.append(r.replacement);
            left = r.interval.b + 1;
        }

        for (int i = left; i < to; i++) {
            sw.append(tokenText(templateOptions, tokenStream.get(i)));
        }
        return sw;
    }

    protected String tokenText(final TemplateOptions templateOptions, final Token token) {

        String text = token.getText();

        if (token.getChannel() == Java7Lexer.CHANNEL_COMMENT) {

            text = processComment(text, templateOptions);
        }
        return text;
    }
}
