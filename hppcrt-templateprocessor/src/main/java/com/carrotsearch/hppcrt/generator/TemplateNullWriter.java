package com.carrotsearch.hppcrt.generator;

import java.io.IOException;
import java.io.Writer;

/**
 * Stub for Velocity when we do not really want to render anything into...
 * @author Vincent
 *
 */
public class TemplateNullWriter extends Writer
{
    public static final TemplateNullWriter NULL_WRITER = new TemplateNullWriter();

    private TemplateNullWriter() {
        // nothing
    }

    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        // nothing
    }

    @Override
    public void flush() throws IOException {
        // nothing

    }

    @Override
    public void close() throws IOException {
        // nothing

    }

    @Override
    public void write(final int c) throws IOException {
        //nothing
    }

    @Override
    public void write(final char[] cbuf) throws IOException {
        //nothing
    }

    @Override
    public void write(final String str) throws IOException {
        //nothing
    }

    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        //nothing
    }

    @Override
    public Writer append(final CharSequence csq) throws IOException {
        //do nothing useful
        return this;
    }

    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        //do nothing useful
        return this;
    }

    @Override
    public Writer append(final char c) throws IOException {
        //do nothing useful
        return this;
    }

}
