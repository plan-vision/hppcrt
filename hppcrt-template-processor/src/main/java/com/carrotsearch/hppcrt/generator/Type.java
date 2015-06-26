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
    DOUBLE("0.0");

    private static final Type[] VALUES = Type.values();

    public final String defaultValue;

    private Type(final String defaultVal) {

        this.defaultValue = defaultVal;
    }

    /**
     * Convenience method replacing Enum.valueOf() that returns null on an unknown
     * name for enum instead of throwing an exception.
     * @param representation
     * @return
     */
    public static Type valueOfOrNull(final String representation) {

        Type res = null;

        try {

            res = Type.valueOf(representation.trim());

        } catch (final IllegalArgumentException e) {
            //not matching
        }

        return res;
    }

    /**
     * Retreive Type from Type.toString() i.e. (Object, int float, ...etc) and returns null on an unknown
     * representation.
     * @param representation
     * @return
     */
    public static Type fromString(final String representation) {

        for (final Type current : Type.VALUES) {

            if (current.getType().toLowerCase().equals(representation.trim().toLowerCase())) {

                return current;
            }
        }

        return null;
    }

    public String getBoxedType()
    {
        if (this == GENERIC) {
            return "Object";
        }

        final String boxed = name().toLowerCase();
        return Character.toUpperCase(boxed.charAt(0)) + boxed.substring(1);
    }

    public String getType()
    {
        if (this == GENERIC) {
            return "Object";
        }

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