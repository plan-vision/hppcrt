package com.carrotsearch.hppcrt.generator.parser;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.carrotsearch.hppcrt.generator.TemplateOptions;
import com.carrotsearch.hppcrt.generator.Type;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.ClassDeclarationContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.ConstructorDeclarationContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.CreatedNameContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.GenericMethodDeclarationContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.IdentifierTypeOrDiamondPairContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.IdentifierTypePairContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.InterfaceDeclarationContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.MethodDeclarationContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.PrimaryContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.QualifiedNameContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.TypeArgumentContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.TypeArgumentsContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.TypeBoundContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.TypeParameterContext;
import com.carrotsearch.hppcrt.generator.parser.Java7Parser.TypeTypeContext;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class SignatureReplacementVisitor extends ReplacementVisitorBase
{
    /**
     * Constructor
     * @param templateOptions
     * @param processor
     */
    public SignatureReplacementVisitor(final TemplateOptions templateOptions, final SignatureProcessor processor) {
        super(templateOptions, processor, Logger.getLogger(SignatureReplacementVisitor.class.getName()));
    }

    /**
     * Entering point: visit the class.
     */
    @Override
    public List<Replacement> visitClassDeclaration(final ClassDeclarationContext ctx) {

        //Excluding imports, the replacements starts here, so boot-up the process by running the base method
        final List<Replacement> result = new ArrayList<>(super.visitClassDeclaration(ctx));

        log(Level.FINEST, "visitClassDeclaration", "calling base, result = "
                + Replacement.toString(result));

        String className = ctx.Identifier().getText();

        log(Level.FINEST, "visitClassDeclaration", "className = " + className);

        if (isTemplateIdentifier(className) || true) {

            final List<TypeBound> typeBounds = new ArrayList<>();

            if (ctx.typeParameters() != null) {

                for (final TypeParameterContext c : ctx.typeParameters().typeParameter()) {
                    typeBounds.add(typeBoundOf(c));
                }

                result.add(new Replacement(ctx.typeParameters().getText(), ctx.typeParameters(), toString(typeBounds)));

                log(Level.FINEST, "visitClassDeclaration",
                        "type parameters replacements = " + result.get(result.size() - 1));
            }

            //1) Existing generic identifiers
            if (className.contains("KType") && typeBounds.size() >= 1) {
                className = className.replace("KType", typeBounds.get(0).templateBound().getBoxedType());
            }

            if (className.contains("VType") && typeBounds.size() >= 2) {
                className = className.replace("VType", typeBounds.get(1).templateBound().getBoxedType());
            }

            //2) At that point, if className still contains KType/VType, that
            // means that we had empty generic identifiers, so do a simple replace of className with matching TemplateOptions
            //This allow managing of KTypeArrays-like classes behaving like java.util.Arrays.
            if (className.contains("KType") && this.templateOptions.hasKType()) {
                className = className.replace("KType", this.templateOptions.ktype.getBoxedType());
            }

            if (className.contains("VType") && this.templateOptions.hasVType()) {
                className = className.replace("VType", this.templateOptions.vtype.getBoxedType());
            }

            result.add(new Replacement(ctx.Identifier().getText(), ctx.Identifier(), className));

            log(Level.FINEST, "visitClassDeclaration",
                    " result className replacement = " + result.get(result.size() - 1));
        }

        return result;
    }

    @Override
    public List<Replacement> visitInterfaceDeclaration(final InterfaceDeclarationContext ctx) {

        List<Replacement> result = super.visitInterfaceDeclaration(ctx);

        log(Level.FINEST, "visitInterfaceDeclaration", "calling base, result = "
                + Replacement.toString(result));

        String className = ctx.Identifier().getText();

        log(Level.FINEST, "visitInterfaceDeclaration", "className = " + className);

        if (isTemplateIdentifier(className)) {
            final List<TypeBound> typeBounds = new ArrayList<>();

            for (final TypeParameterContext c : ctx.typeParameters().typeParameter()) {
                typeBounds.add(typeBoundOf(c));
            }

            final Replacement replaceGenericTypes = new Replacement(ctx.typeParameters().getText(), ctx.typeParameters(),
                    toString(typeBounds));

            log(Level.FINEST, "visitInterfaceDeclaration", "replaceGenericTypes = " + replaceGenericTypes);

            int typeBoundIndex = 0;

            if (className.contains("KType")) {
                className = className.replace("KType", typeBounds.get(typeBoundIndex++).templateBound().getBoxedType());
            }

            if (className.contains("VType")) {
                className = className.replace("VType", typeBounds.get(typeBoundIndex++).templateBound().getBoxedType());
            }

            final Replacement replaceIdentifier = new Replacement(ctx.Identifier().getText(), ctx.Identifier(), className);

            log(Level.FINEST, "visitInterfaceDeclaration", "replaceIdentifier = " + replaceIdentifier);

            result = new ArrayList<>(result);

            result.addAll(Arrays.asList(replaceIdentifier, replaceGenericTypes));
        }

        return result;
    }

    @Override
    public List<Replacement> visitConstructorDeclaration(final ConstructorDeclarationContext ctx) {

        final List<Replacement> results = processIdentifier(ctx.Identifier(), super.visitConstructorDeclaration(ctx));

        log(Level.FINEST, "visitConstructorDeclaration", "results = " + Replacement.toString(results));

        return results;
    }

    @Override
    public List<Replacement> visitPrimary(final PrimaryContext ctx) {

        final TerminalNode identifier = ctx.Identifier();

        if (identifier != null && isTemplateIdentifier(identifier.getText())) {

            log(Level.FINEST, "visitPrimary", "identifier (template) text = " + identifier.getText());

            return processIdentifier(identifier, ReplacementVisitorBase.NONE);
        }

        log(Level.FINEST, "visitPrimary", "identifier (NON template) text = " + ctx.Identifier());

        return super.visitPrimary(ctx);
    }

    @Override
    public List<Replacement> visitGenericMethodDeclaration(final GenericMethodDeclarationContext ctx) {

        final ArrayList<String> bounds = new ArrayList<>();

        //for all typeParameters
        for (final TypeParameterContext c : ctx.typeParameters().typeParameter()) {

            switch (c.Identifier().getText()) {

            case "KType":
                Preconditions.checkArgument(c.typeBound() == null, "Unexpected type bound on template type: " + c.getText());
                if (this.templateOptions.isKTypeGeneric()) {
                    bounds.add(c.getText());
                }
                break;

            case "VType":
                Preconditions.checkArgument(c.typeBound() == null, "Unexpected type bound on template type: " + c.getText());
                if (this.templateOptions.isVTypeGeneric()) {
                    bounds.add(c.getText());
                }
                break;

            default:
                final TypeBoundContext tbc = c.typeBound();
                if (tbc != null) {
                    Preconditions.checkArgument(tbc.typeType().size() == 1, "Expected exactly one type bound: " + c.getText());

                    final TypeTypeContext tctx = tbc.typeType().get(0);
                    final Interval sourceInterval = tbc.getSourceInterval();

                    final StringWriter sw = this.processor.reconstruct(new StringWriter(), this.processor.tokenStream,
                            sourceInterval.a, sourceInterval.b, visitTypeType(tctx), this.templateOptions);

                    bounds.add(c.Identifier() + " extends " + sw.toString());

                } else {
                    bounds.add(c.getText());
                }
                break;
            } //end switch
        } //end for

        final List<Replacement> replacements = new ArrayList<>();

        if (bounds.isEmpty()) {
            replacements.add(new Replacement(ctx.typeParameters().getText(), ctx.typeParameters(), ""));
        } else {
            replacements.add(new Replacement(ctx.typeParameters().getText(), ctx.typeParameters(), "<" + join(", ", bounds) + ">"));
        }

        log(Level.FINEST, "visitGenericMethodDeclaration", "Adding parent....");

        replacements.addAll(super.visitMethodDeclaration(ctx.methodDeclaration()));

        log(Level.FINEST, "visitGenericMethodDeclaration", "replacements = "
                + ImmutableList.copyOf(replacements).toString());

        return replacements;
    }

    @Override
    public List<Replacement> visitMethodDeclaration(final MethodDeclarationContext ctx) {

        final List<Replacement> replacements = new ArrayList<>();

        if (ctx.typeType() != null) {
            replacements.addAll(visitTypeType(ctx.typeType()));
        }
        replacements.addAll(processIdentifier(ctx.Identifier(), ReplacementVisitorBase.NONE));

        log(Level.FINEST, "visitMethodDeclaration", "replacements after processIdentifier = "
                + Replacement.toString(replacements));

        replacements.addAll(visitFormalParameters(ctx.formalParameters()));

        log(Level.FINEST, "visitMethodDeclaration", "replacements after visitFormalParameters = "
                + Replacement.toString(replacements));

        if (ctx.qualifiedNameList() != null) {
            replacements.addAll(visitQualifiedNameList(ctx.qualifiedNameList()));
        }

        log(Level.FINEST, "visitMethodDeclaration", "replacements after visitQualifiedNameList = "
                + Replacement.toString(replacements));

        //The method body may be null, in case of an abstract method !
        if (ctx.methodBody() != null) {
            replacements.addAll(visitMethodBody(ctx.methodBody()));
        }

        log(Level.FINEST, "visitMethodDeclaration", "replacements after visitMethodBody = "
                + Replacement.toString(replacements));

        return replacements;
    }

    @Override
    public List<Replacement> visitIdentifierTypeOrDiamondPair(final IdentifierTypeOrDiamondPairContext ctx) {

        if (ctx.typeArgumentsOrDiamond() == null) {
            return processIdentifier(ctx.Identifier(), ReplacementVisitorBase.NONE);
        }

        List<Replacement> replacements = new ArrayList<>();
        final String identifier = ctx.Identifier().getText();

        log(Level.FINEST, "visitIdentifierTypeOrDiamondPair", "identifier = " + identifier);

        if (ctx.typeArgumentsOrDiamond().getText().equals("<>")) {

            if (identifier.contains("KType") && this.templateOptions.isKTypePrimitive()
                    && (!identifier.contains("VType") || this.templateOptions.isVTypePrimitive())) {

                replacements.add(new Replacement(identifier, ctx.typeArgumentsOrDiamond(), ""));
            }

            log(Level.FINEST, "visitIdentifierTypeOrDiamondPair", "replacements after '<>' = "
                    + Replacement.toString(replacements));

            //process left-most identifier.
            return processIdentifier(ctx.Identifier(), replacements);
        }

        //There are complex types to the right of identifiers
        final List<TypeBound> typeBounds = new ArrayList<>();
        final TypeArgumentsContext typeArguments = ctx.typeArgumentsOrDiamond().typeArguments();

        final Deque<Type> wildcards = getWildcards();

        //enumerate typeArguments: they could be complex, (type within type...), so visit them recursively.
        final boolean isTerminal = isTerminalTypeIdentifier(typeArguments);

        for (final TypeArgumentContext c : typeArguments.typeArgument()) {

            //Visit subtypes ...
            if (!isTerminal) {
                replacements.addAll(new ArrayList<>(super.visitTypeArgument(c)));

            } else {

                typeBounds.add(typeBoundOf(c, wildcards));
            }
        }

        //if args at the right of Identifier() are "terminal ones" (generics without sub-generics) do a replacement.
        if (isTerminal) {
            replacements.add(new Replacement(typeArguments.getText(), typeArguments, toString(typeBounds)));

            //Process the leftmost terminal identifier, with bounds since they are available
            replacements = processIdentifierWithBounds(ctx.Identifier(), typeBounds, replacements);

            log(Level.FINEST, "visitIdentifierTypeOrDiamondPair", "replacements after terminal-bounded replacements = "
                    + Replacement.toString(replacements));
        } else {
            //We are not terminal , so blindly replace the identifier: We have no bound info, so we replace
            //based on the naming only, so be it. (so testBug_ErasesUntemplated() would not work if the generic was inclosed in another generic)
            replacements = processIdentifier(ctx.Identifier(), replacements);

            log(Level.FINEST, "visitIdentifierTypeOrDiamondPair",
                    "replacements after boundless identifier replacements = " + Replacement.toString(replacements));
        }

        return replacements;
    }

    @Override
    public List<Replacement> visitCreatedName(final CreatedNameContext ctx) {

        log(Level.FINEST, "visitCreatedName", "calling base...");

        return super.visitCreatedName(ctx);
    }

    @Override
    public List<Replacement> visitIdentifierTypePair(final IdentifierTypePairContext ctx) {

        final String identifier = ctx.Identifier().getText();
        List<Replacement> replacements = new ArrayList<>();

        log(Level.FINEST, "visitIdentifierTypePair", "identifier or expression = " + identifier);

        //Only terminal
        if (isTerminalTypeIdentifier(ctx.typeArguments()) || ctx.getChildCount() == 1) {

            if (ctx.typeArguments() != null && isTemplateIdentifier(identifier)) {

                final List<TypeBound> typeBounds = new ArrayList<>();

                final Deque<Type> wildcards = getWildcards();

                for (final TypeArgumentContext c : ctx.typeArguments().typeArgument()) {

                    typeBounds.add(typeBoundOf(c, wildcards));
                }
                replacements.add(new Replacement(ctx.typeArguments().getText(), ctx.typeArguments(), toString(typeBounds)));

                //Process the leftmost terminal identifier
                replacements = processIdentifierWithBounds(ctx.Identifier(), typeBounds, replacements);

                log(Level.FINEST, "visitIdentifierTypePair",
                        "non-null template replacements = " + Replacement.toString(replacements));

                return replacements;
            } //end if ctx.typeArguments() != null

            //Translate the terminal symbols
            replacements = processIdentifier(ctx.Identifier(), ReplacementVisitorBase.NONE);

        }  //end if isTerminal
        else {

            //Not terminal: Process the left-most identifier.
            replacements = processIdentifier(ctx.Identifier(), ReplacementVisitorBase.NONE);

            //else NOT terminal: Explore the children...
            replacements.addAll(new ArrayList<>(super.visitIdentifierTypePair(ctx)));
        }

        return replacements;
    }

    @Override
    public List<Replacement> visitQualifiedName(final QualifiedNameContext ctx) {

        List<Replacement> replacements = ReplacementVisitorBase.NONE;

        for (final TerminalNode identifier : ctx.Identifier()) {

            String symbol = identifier.getText();

            if (isTemplateIdentifier(symbol)) {

                if (symbol.contains("KType")) {
                    symbol = symbol.replace("KType", this.templateOptions.getKType().getBoxedType());
                }
                if (symbol.contains("VType")) {
                    symbol = symbol.replace("VType", this.templateOptions.getVType().getBoxedType());
                }

                if (replacements == ReplacementVisitorBase.NONE) {
                    replacements = new ArrayList<>();
                }
                replacements.add(new Replacement(identifier.getText(), identifier, symbol));
            }
        }

        log(Level.FINEST, "visitQualifiedName", "replacements = " + Replacement.toString(replacements));

        return replacements;
    }
}
