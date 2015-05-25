package com.carrotsearch.hppcrt.generator.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.xpath.XPath;

import com.carrotsearch.hppcrt.generator.TemplateOptions;
import com.carrotsearch.hppcrt.generator.TemplateProcessor;
import com.carrotsearch.hppcrt.generator.Type;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ReplacementVisitorBase extends Java7ParserBaseVisitor<List<Replacement>>
{
    public final static List<Replacement> NONE = Collections.emptyList();

    protected final TemplateOptions templateOptions;
    protected final SignatureProcessor processor;

    private final Logger logger;

    private final XPath terminalTypeIdentifierPath;

    public ReplacementVisitorBase(final TemplateOptions templateOptions, final SignatureProcessor processor, final Logger traceLogger) {

        this.templateOptions = templateOptions;
        this.processor = processor;
        this.logger = traceLogger;

        this.terminalTypeIdentifierPath = new XPath(this.processor.parser, "//typeArgument/type/classOrInterfaceType/identifierTypePair/*");

        TemplateProcessor.setLoggerlevel(traceLogger, this.templateOptions.verbose);
    }

    protected TypeBound typeBoundOf(final TypeParameterContext c) {

        final String symbol = c.Identifier().toString();
        switch (symbol) {
        case "KType":
            return new TypeBound(this.templateOptions.getKType(), c.getText());
        case "VType":
            return new TypeBound(this.templateOptions.getVType(), c.getText());
        default:
            return new TypeBound(null, c.getText());
        }
    }

    protected TypeBound typeBoundOf(final TypeArgumentContext c, final Deque<Type> wildcards) {

        if (c.getText().equals("?")) {
            return new TypeBound(wildcards.removeFirst(), c.getText());
        }

        final TypeBound t = typeBoundOf(c.type());

        if (t.isTemplateType()) {
            return new TypeBound(t.templateBound(), getSourceText(c));
        }

        return new TypeBound(null, getSourceText(c));
    }

    protected String getSourceText(final ParserRuleContext c) {
        return this.processor.tokenStream.getText(c.getSourceInterval());
    }

    protected TypeBound typeBoundOf(final TypeContext c) {

        if (c.primitiveType() != null) {
            return new TypeBound(null, c.getText());
        }

        final TypeBound t = typeBoundOf(c.classOrInterfaceType());

        if (t.isTemplateType()) {
            return new TypeBound(t.templateBound(), c.getText());
        }

        return new TypeBound(null, c.getText());
    }

    protected TypeBound typeBoundOf(final ClassOrInterfaceTypeContext c) {

        Preconditions.checkArgument(c.identifierTypePair().size() == 1, "Unexpected typeBoundOf context: " + c.getText());

        for (final IdentifierTypePairContext p : c.identifierTypePair()) {

            switch (p.Identifier().getText()) {
            case "KType":
                return new TypeBound(this.templateOptions.getKType(), p.getText());
            case "VType":
                return new TypeBound(this.templateOptions.getVType(), p.getText());
            }
        }

        return new TypeBound(null, c.getText());
    }

    @Override
    protected final List<Replacement> defaultResult() {
        return ReplacementVisitorBase.NONE;
    }

    @Override
    protected List<Replacement> aggregateResult(final List<Replacement> first, final List<Replacement> second) {

        if (second.size() == 0) {
            return first;
        }

        if (first.size() == 0) {
            return second;
        }

        // Treat partial results as immutable.
        final List<Replacement> result = new ArrayList<Replacement>();
        result.addAll(first);
        result.addAll(second);
        return result;
    }

    protected ArrayDeque<Type> getWildcards() {

        final ArrayDeque<Type> deque = new ArrayDeque<>();

        if (this.templateOptions.hasKType()) {
            deque.addLast(this.templateOptions.getKType());
        }

        if (this.templateOptions.hasVType()) {
            deque.addLast(this.templateOptions.getVType());
        }

        return deque;
    }

    /**
     * Process the Terminal Node ctx.getText() by straight-replacing all 'KType' and 'VType'
     * with their respective types according to the current TemplateOptions. Append the resulting replacement
     * into existing replacements.
     * @param ctx
     * @param replacements
     * @return
     */
    protected List<Replacement> processIdentifier(final TerminalNode ctx, List<Replacement> replacements) {

        String identifier = ctx.getText();

        //always make an independent copy
        replacements = new ArrayList<>(replacements);

        if (isTemplateIdentifier(identifier)) {

            switch (identifier) {
            case "KType":
                identifier = this.templateOptions.isKTypePrimitive() ? this.templateOptions.getKType().getType() : "KType";
                break;
            case "VType":
                identifier = this.templateOptions.isVTypePrimitive() ? this.templateOptions.getVType().getType() : "VType";
                break;
            default:
                if (identifier.contains("KType")) {
                    identifier = identifier.replace("KType", this.templateOptions.getKType().getBoxedType());
                }
                if (identifier.contains("VType")) {
                    identifier = identifier.replace("VType", this.templateOptions.getVType().getBoxedType());
                }
                break;
            }
            replacements.add(new Replacement(ctx.getText(), ctx, identifier));
        } //end if isTemplateIdentifier

        log(Level.FINEST, "processIdentifier", "intput= '" + ctx.getText() + "', result= " + Replacement.toString(replacements));

        return replacements;
    }

    /**
     * Process the Terminal Node according to specified bounds. Append the resulting replacement
     * into existing replacements.
     * @param ctxIdentifier
     * @param typeBounds
     * @param ctx
     * @param replacements
     * @return
     */
    protected List<Replacement> processIdentifierWithBounds(final TerminalNode ctxIdentifier, final List<TypeBound> typeBounds, List<Replacement> replacements) {

        String identifier = ctxIdentifier.getText();

        //always make an independent copy
        replacements = new ArrayList<>(replacements);

        if (identifier.contains("KType") && typeBounds.size() >= 1) {

            final TypeBound bb = typeBounds.get(0);
            if (bb.isTemplateType()) {
                identifier = identifier.replace("KType", bb.getBoxedType());
            } else {
                identifier = identifier.replace("KType", "Object");
            }
        }

        if (identifier.contains("VType") && typeBounds.size() >= 2) {
            final TypeBound bb = typeBounds.get(1);
            if (bb.isTemplateType()) {
                identifier = identifier.replace("VType", bb.getBoxedType());
            } else {
                identifier = identifier.replace("VType", "Object");
            }
        }

        log(Level.FINEST, "processIdentifierWithBounds", "intput= '" + ctxIdentifier.getText() + "', result= '" + identifier + "'");

        replacements.add(new Replacement(ctxIdentifier.getText(), ctxIdentifier, identifier));

        return replacements;
    }

    /**
     * Convert the list of TypeBound into a
     * {@code "<A,B...>'} representation string using Generic-only TypeBounds.
     * @param typeBounds
     * @return
     */
    protected String toString(final List<TypeBound> typeBounds) {

        final List<String> parts = new ArrayList<>();

        for (final TypeBound tb : typeBounds) {

            if (tb.isTemplateType()) {
                if (!tb.templateBound().isGeneric()) {
                    continue;
                }
            }
            parts.add(tb.originalBound());
        }

        return parts.isEmpty() ? "" : "<" + join(", ", parts) + ">";
    }

    /**
     * Assemble parts, separating with on
     * @param on
     * @param parts
     * @return
     */
    protected String join(final String on, final Iterable<String> parts) {

        final StringBuilder out = new StringBuilder();
        boolean prependOn = false;
        for (final String part : parts) {
            if (prependOn) {
                out.append(on);
            } else {
                prependOn = true;
            }
            out.append(part);
        }
        return out.toString();
    }

    /**
     * True if the symbol includes 'KType' or 'VType'
     * @param symbol
     * @return
     */
    protected boolean isTemplateIdentifier(final String symbol) {
        return symbol.contains("KType") || symbol.contains("VType");
    }

    /**
     * True if the string represents a "terminal" TypeArguments generic parameters
     * ex: {@code in Foo<KType>, KTypeFoo<KType, A>, KTypeVTypeFoo<KType, VType>..... }
     * <p>
     * but NOT: {@code KTypeFoo<KTypeInner<KType>> }
     * @param ctx
     * @return
     */
    protected boolean isTerminalTypeIdentifier(final TypeArgumentsContext ctx) {

        boolean isTerminal = false;

        if (ctx != null && ctx.typeArgument().size() > 0) {

            isTerminal = true;

            //enumerate all single typeArgument
            for (final TypeArgumentContext typeArg : ctx.typeArgument()) {

                if (typeArg.getChildCount() == 1 && typeArg.getChild(0) instanceof TerminalNode) {

                    continue; //end immediately on a terminal symbol.
                }

                final Collection<ParseTree> testResult = this.terminalTypeIdentifierPath.evaluate(typeArg);

                if (!(testResult.size() == 1 && testResult.iterator().next().getChildCount() == 0)) {

                    isTerminal = false;
                    break;
                }
            }
        } //endif ctx != null && ctx.typeArgument().size() > 0

        return isTerminal;
    }

    /**
     * log shortcut
     * @param lvl
     * @param methodName
     * @param message
     */
    protected void log(final Level lvl, final String methodName, final String message) {

        this.logger.log(lvl, methodName + " - " + message);
    }

}
