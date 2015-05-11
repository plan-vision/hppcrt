package com.carrotsearch.hppcrt;

/**
 * Intrinsic methods that are fully functional for the generic ({@link Object}) versions
 * of collection classes, but are replaced with low-level corresponding structures for
 * primitive types.
 * 
 * <p><b>This class should not appear in the final distribution (all methods are replaced
 * in templates, by the generic inlining mechanism.</b></p>
 */

public final class Intrinsics
{
    private Intrinsics()
    {
        // no instances.
    }

    /**
     * Create and return an array of template objects (<code>Object</code>s in the generic
     * version, corresponding key-primitive type in the generated version).
     * 
     * @param arraySize The size of the array to return.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newKTypeArray(final int arraySize)
    {
        /*! ($TemplateOptions.inlineKTypeGenericAndPrimitive("Intrinsics.newKTypeArray",
        "(arraySize)",
        "(KType[]) new Object[arraySize]",
        "new ${TemplateOptions.KType}[arraySize]")) !*/

        return (T) new Object[arraySize];
    }

    /**
     * Create and return an array of template objects (<code>Object</code>s in the generic
     * version, corresponding value-primitive type in the generated version).
     * 
     * @param arraySize The size of the array to return.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newVTypeArray(final int arraySize)
    {
        /*! #if($TemplateOptions.VType)
             ($TemplateOptions.inlineVTypeGenericAndPrimitive("Intrinsics.newVTypeArray",
            "(arraySize)",
            "(VType[]) new Object[arraySize]",
            "new ${TemplateOptions.VType}[arraySize]"))
        #end  !*/

        return (T) new Object[arraySize];
    }

    /**
     * Returns the default value for keys (<code>null</code> or <code>0</code>
     * for primitive types).
     * 
     * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6302954"
     */
    public static <T> T defaultKTypeValue()
    {
        /*! ($TemplateOptions.inlineKType("Intrinsics.defaultKTypeValue",
        "()",
        "${TemplateOptions.getKType().getDefaultValue()}")) !*/

        return (T) null;
    }

    /**
     * Returns <code>true</code> if the provided key is an "empty slot"
     * marker. For generic types the empty slot is <code>null</code>,
     * for any other type it is an equivalent of zero.
     * 
     * For floating-point types {@link Float#floatToIntBits(float)} and
     * {@link Double#doubleToLongBits(double)} is invoked to normalize different
     * representations of zero.
     * 
     * Testing for zeros should be compiled into fast machine code.
     */
    public static boolean isEmpty(final Object key) {

        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.isEmpty",
        "(key)",
        "key == null",
        "key == ${TemplateOptions.getKType().getDefaultValue()}",
        "key == ${TemplateOptions.getKType().getDefaultValue()}",
        "Float.floatToIntBits(key) == 0",
        "Double.doubleToLongBits(key) == 0L",
        "!key")) !*/

        return key == null;
    }

    /**
     * Returns the default value for values (<code>null</code> or <code>0</code>
     * for primitive types).
     * 
     * @see "http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6302954"
     */
    public static <T> T defaultVTypeValue()
    {
        /*! #if($TemplateOptions.VType)
               ($TemplateOptions.inlineVType("Intrinsics.defaultVTypeValue",
                "()",
                "${TemplateOptions.getVType().getDefaultValue()}"))
        #end  !*/

        return (T) null;
    }

    /**
     * Compare two keys for equivalence. Null references return <code>true</code>.
     * Primitive types are compared using <code>==</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    public static boolean equalsKType(final Object e1, final Object e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.equalsKType",
        "(e1, e2)",
        "e1 == null ? e2 == null : e1.equals(e2)",
        "e1 == e2",
        "e1 == e2",
        "Float.floatToIntBits(e1) == Float.floatToIntBits(e2)",
        "Double.doubleToLongBits(e1) == Double.doubleToLongBits(e2)",
        "e1 == e2")) !*/

        return e1 == null ? e2 == null : e1.equals(e2);
    }

    /**
     * Identical as {@link #equalsKType} except that
     *  e1 Objects are assumed to be not-null.
     */
    public static boolean equalsKTypeNotNull(final Object e1, final Object e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.equalsKTypeNotNull",
        "(e1, e2)",
        "e1.equals(e2)",
        "e1 == e2",
        "e1 == e2",
        "Float.floatToIntBits(e1) == Float.floatToIntBits(e2)",
        "Double.doubleToLongBits(e1) == Double.doubleToLongBits(e2)",
        "e1 == e2")) !*/

        return e1.equals(e2);
    }

    /**
     * Compare two keys for equivalence. Null references return <code>true</code>.
     * Primitive types are compared using <code>==</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    public static boolean equalsVType(final Object e1, final Object e2)
    {
        /*! #if($TemplateOptions.VType)
            ($TemplateOptions.inlineVTypeWithFullSpecialization("Intrinsics.equalsVType",
             "(e1, e2)",
             "e1 == null ? e2 == null : e1.equals(e2)",
             "e1 == e2",
             "e1 == e2",
             "Float.floatToIntBits(e1) == Float.floatToIntBits(e2)",
             "Double.doubleToLongBits(e1) == Double.doubleToLongBits(e2)",
             "e1 == e2"))
         #end  !*/

        return e1 == null ? e2 == null : e1.equals(e2);
    }

    /**
     * Compare two keys by Comparable<T>.
     * Primitive types comparison result is <code>e1 - e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare} and {@link Float#compare}.
     */
    public static <T extends Comparable<? super T>> int compareKType(final T e1, final T e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.compareKType",
        "(e1, e2)",
        "e1.compareTo(e2)",
        "(e1 == e2)?0:((e1 < e2)?-1:1)",
        "(e1 == e2)?0:((e1 < e2)?-1:1)",
        "Float.compare(e1 , e2)",
        "Double.compare(e1 , e2)",
        "(e1 == e2) ? 0 : ((e1)? 1 : -1)")) !*/

        return e1.compareTo(e2);
    }

    /**
     * Compare two keys by Comparable<T>, unchecked without Comparable signature
     * Primitive types comparison result is <code>e1 - e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare} and {@link Float#compare}.
     */
    @SuppressWarnings("unchecked")
    public static <T> int compareKTypeUnchecked(final T e1, final T e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.compareKTypeUnchecked",
        "(e1, e2)",
        "((Comparable<? super KType>) e1).compareTo(e2)",
        "(e1 == e2)?0:((e1 < e2)?-1:1)",
        "(e1 == e2)?0:((e1 < e2)?-1:1)",
        "Float.compare(e1 , e2)",
        "Double.compare(e1 , e2)",
        "(e1 == e2) ? 0 : ((e1)? 1 : -1)")) !*/

        return ((Comparable<? super T>) e1).compareTo(e2);
    }

    /**
     * Compare two keys by Comparable<T>, returns true if e1.compareTo(e2) > 0
     * Primitive types comparison result is <code>e1 > e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare > 0} and {@link Float#compare > 0}.
     */
    public static <T extends Comparable<? super T>> boolean isCompSupKType(final T e1, final T e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.isCompSupKType",
        "(e1, e2)",
        "e1.compareTo(e2) > 0",
        "e1 > e2",
        "e1 > e2",
        "Float.compare(e1 , e2) > 0",
        "Double.compare(e1 , e2) > 0",
        "(e1 == e2) ? false :(e1)")) !*/

        return e1.compareTo(e2) > 0;
    }

    /**
     * Compare two keys by Comparable<T>, unchecked without signature. returns true if e1.compareTo(e2) > 0
     * Primitive types comparison result is <code>e1 > e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare > 0} and {@link Float#compare > 0}.
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean isCompSupKTypeUnchecked(final T e1, final T e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.isCompSupKTypeUnchecked",
        "(e1, e2)",
        "((Comparable<? super KType>) e1).compareTo(e2) > 0",
        "e1 > e2",
        "e1 > e2",
        "Float.compare(e1 , e2) > 0",
        "Double.compare(e1 , e2) > 0",
        "(e1 == e2) ? false:(e1)")) !*/

        return ((Comparable<? super T>) e1).compareTo(e2) > 0;
    }

    /**
     * Compare two keys by Comparable<T>, returns true if e1.compareTo(e2) < 0
     * Primitive types comparison result is <code>e1 < e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare < 0} and {@link Float#compare < 0}.
     */
    public static <T extends Comparable<? super T>> boolean isCompInfKType(final T e1, final T e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.isCompInfKType",
        "(e1, e2)",
        "e1.compareTo(e2) < 0",
        "e1 < e2",
        "e1 < e2",
        "Float.compare(e1 , e2) < 0",
        "Double.compare(e1 , e2) < 0",
        "(e1 == e2) ? false :(e2)")) !*/

        return e1.compareTo(e2) < 0;
    }

    /**
     * Compare two keys by Comparable<T>, unchecked without signature. returns true if e1.compareTo(e2) < 0
     * Primitive types comparison result is <code>e1 < e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare < 0} and {@link Float#compare < 0}.
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean isCompInfKTypeUnchecked(final T e1, final T e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.isCompInfKTypeUnchecked",
        "(e1, e2)",
        "((Comparable<? super KType>) e1).compareTo(e2) < 0",
        "e1 < e2",
        "e1 < e2",
        "Float.compare(e1 , e2) < 0",
        "Double.compare(e1 , e2) < 0",
        "(e1 == e2) ? false :(e2)")) !*/

        return ((Comparable<? super T>) e1).compareTo(e2) < 0;
    }

    /**
     * Compare two keys by Comparable<T>, returns true if e1.compareTo(e2) == 0
     * Primitive types comparison result is <code>e1 == e2</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    public static <T extends Comparable<? super T>> boolean isCompEqualKType(final T e1, final T e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.isCompEqualKType",
        "(e1, e2)",
        "e1.compareTo(e2) == 0",
        "e1 == e2",
        "e1 == e2",
        "Float.floatToIntBits(e1) == Float.floatToIntBits(e2)",
        "Double.doubleToLongBits(e1) == Double.doubleToLongBits(e2)",
        "e1 == e2")) !*/

        return e1.compareTo(e2) == 0;
    }

    /**
     * Compare two keys by Comparable<T>, unchecked without signature. returns true if e1.compareTo(e2) == 0
     * Primitive types comparison result is <code>e1 == e2</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean isCompEqualKTypeUnchecked(final T e1, final T e2)
    {
        /*! ($TemplateOptions.inlineKTypeWithFullSpecialization("Intrinsics.isCompEqualKTypeUnchecked",
        "(e1, e2)",
        "((Comparable<? super KType>) e1).compareTo(e2) == 0",
        "e1 == e2",
        "e1 == e2",
        "Float.floatToIntBits(e1) == Float.floatToIntBits(e2)",
        "Double.doubleToLongBits(e1) == Double.doubleToLongBits(e2)",
        "e1 == e2")) !*/

        return ((Comparable<? super T>) e1).compareTo(e2) == 0;
    }

}

//Never attempt to generate anything from this file, but parse it so put this at the end !
/*! ($TemplateOptions.doNotGenerate()) !*/

