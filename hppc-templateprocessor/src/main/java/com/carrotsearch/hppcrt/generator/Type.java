package com.carrotsearch.hppcrt.generator;

public enum Type
{
    GENERIC, BYTE, CHAR, SHORT, INT, FLOAT, LONG, DOUBLE, BOOLEAN;

    public String getBoxedType()
    {
        if (this == GENERIC) return "Object";

        final String boxed = name().toLowerCase();
        return Character.toUpperCase(boxed.charAt(0)) + boxed.substring(1);
    }

    public String getType()
    {
        if (this == GENERIC) return "Object";

        return name().toLowerCase();
    }

    public boolean isGeneric()
    {
        return this == GENERIC;
    }
}