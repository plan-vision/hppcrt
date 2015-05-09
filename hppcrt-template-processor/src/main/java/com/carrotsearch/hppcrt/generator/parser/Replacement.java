package com.carrotsearch.hppcrt.generator.parser;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.SyntaxTree;

/** */
public final class Replacement
{
    public final Interval interval;
    public final String source;
    public final String replacement;

    public Replacement(final String src, final Interval interval, final String replacement) {

        //source is only useful for debug purposes
        this.source = src;

        this.interval = interval;
        this.replacement = replacement;
    }

    public Replacement(final String source, final SyntaxTree ctx, final String replacement) {
        this(source, ctx.getSourceInterval(), replacement);
    }

    @Override
    public String toString() {
        return "{'" + this.source + "'(" + this.interval + ") => '" + this.replacement + "'}";
    }
}
