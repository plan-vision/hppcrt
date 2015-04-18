package com.carrotsearch.hppcrt;

import java.util.IllegalFormatException;
import java.util.Locale;

@SuppressWarnings("serial")
public class BufferAllocationException extends RuntimeException
{
    BufferAllocationException(final String message) {
        super(message);
    }

    public BufferAllocationException(final String message, final Object... args) {
        this(message, null, args);
    }

    public BufferAllocationException(final String message, final Throwable t, final Object... args) {
        super(BufferAllocationException.formatMessage(message, t, args), t);
    }

    private static String formatMessage(final String message, final Throwable t, final Object... args) {
        try {
            return String.format(Locale.ROOT, message, args);
        }
        catch (final IllegalFormatException e) {
            final BufferAllocationException substitute =
                    new BufferAllocationException(message + " [ILLEGAL FORMAT, ARGS SUPPRESSED]");
            if (t != null) {
                substitute.addSuppressed(t);
            }
            substitute.addSuppressed(e);
            throw substitute;
        }
    }
}
