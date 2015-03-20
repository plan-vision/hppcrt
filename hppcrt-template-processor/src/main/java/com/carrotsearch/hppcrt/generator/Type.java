package com.carrotsearch.hppcrt.generator;

public enum Type
{
    GENERIC("null"),
    BYTE("(byte)0"),
    CHAR("\'\\u0000\'"),
    SHORT("(short)0"),
    INT("0"),
    LONG("0L"),
    FLOAT("0f"),
    DOUBLE("0.0"),
    BOOLEAN("false");

    public final String defaultValue;

    private Type(final String defaultVal) {

        this.defaultValue = defaultVal;
    }

    public String getBoxedType()
    {
        if (this == GENERIC)
            return "Object";

        final String boxed = name().toLowerCase();
        return Character.toUpperCase(boxed.charAt(0)) + boxed.substring(1);
    }

    public String getType()
    {
        if (this == GENERIC)
            return "Object";

        return name().toLowerCase();
    }

    /**
     * Used in Velocity when 2 Type(s) are tested for equality in template.
     */
    @Override
    public String toString() {
        return getType();
    }

    public boolean isGeneric()
    {
        return this == GENERIC;
    }

    public String getDefaultValue() {

        return this.defaultValue;
    }
}