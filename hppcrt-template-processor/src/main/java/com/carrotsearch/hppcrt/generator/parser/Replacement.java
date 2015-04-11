package com.carrotsearch.hppcrt.generator.parser;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.SyntaxTree;

/** */
final class Replacement
{
    public final Interval interval;
    public final String replacement;

    public Replacement(final Interval interval, final String replacement) {

        this.interval = interval;
        this.replacement = replacement;
    }

    public Replacement(final SyntaxTree ctx, final String replacement) {
        this(ctx.getSourceInterval(), replacement);
    }

    @Override
    public String toString() {
        return this.interval + " => " + this.replacement;
    }
}
