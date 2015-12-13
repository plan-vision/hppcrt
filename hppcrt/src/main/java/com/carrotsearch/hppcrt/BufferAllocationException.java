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

        String formattedMessage = "";

        try {

            formattedMessage = String.format(message, args);
        }
        catch (final IllegalFormatException e) {

            //something bad happened , replace by a default message
            formattedMessage = "'" + message + "' message has ILLEGAL FORMAT, ARGS SUPPRESSED !";

            //Problem is, this IllegalFormatException may have masked the originally sent exception t,
            //so be it.
            //We can't use Throwable.setSuppressed() (Java 1.7+) because we want to continue
            //to accomodate Java 1.5+.
        }

        return formattedMessage;
    }
}
