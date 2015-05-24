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
     * Returns <code>true</code> if the provided key is an "empty slot"
     * marker. For generic types the empty slot is <code>null</code>,
     * for any other type it is an equivalent of zero.
     * 
     * For floating-point types {@link Float#floatToIntBits(float)} and
     * {@link Double#doubleToLongBits(double)} is invoked to normalize different
     * representations of zero.
     * 
     * Testing for zeros should be compiled into fast machine code.
     * @param <T>
     * @param key
     * @return
     */
    public static <T> boolean isEmpty(final Object key) {

        /*! ($TemplateOptions.declareInline("Intrinsics.<T>isEmpty(key)",
        "<float>==>Float.floatToIntBits(key) == 0",
        "<double>==>Double.doubleToLongBits(key) == 0L",
        "<boolean>==>!key",
        "<*> ==> key == ${TemplateOptions.getKType().getDefaultValue()}")) !*/

        return key == null;
    }

    /**
     * Returns the default "empty key" (<code>null</code> or <code>0</code> for
     * primitive types).
     * @param <T>
     * @return
     */
    public static <T> T empty()
    {
        /*! #if($TemplateOptions.VType)

           $TemplateOptions.declareInline("Intrinsics.<T>empty()",
            "<${TemplateOptions.VType}> ==> ${TemplateOptions.getVType().getDefaultValue()}")
         #end  !*/

        /*! ($TemplateOptions.declareInline("Intrinsics.<T>empty()",
         "<${TemplateOptions.KType}> ==> ${TemplateOptions.getKType().getDefaultValue()}")) !*/

        return (T) null;
    }

    /**
     * A template cast to the given type T. With type erasure it should work
     * internally just fine and it simplifies code. The cast will be erased for
     * primitive types.
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object value) {

        /*! ($TemplateOptions.declareInline("Intrinsics.<T>cast(obj)",
        "<Object>==>(T)obj",
        "<Object[]>==>(T)obj",
        "<*>==>obj")) !*/

        return (T) value;
    }

    /**
     * Create and return an array of template objects (<code>Object</code>s in the generic
     * version, corresponding key-primitive type in the generated version).
     * @param <T>
     * 
     * @param arraySize The size of the array to return.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(final int arraySize)
    {
        /*! #if($TemplateOptions.VType)
            ($TemplateOptions.declareInline("Intrinsics.<T>newArray(arraySize)",
            "<Object> ==> (T[])new Object[arraySize]",
            "<${TemplateOptions.VType}> ==> new ${TemplateOptions.VType}[arraySize]"))
         #end  !*/

        /*! ($TemplateOptions.declareInline("Intrinsics.<T>newArray(arraySize)",
        "<Object> ==> (T[])new Object[arraySize]",
        "<${TemplateOptions.KType}> ==> new ${TemplateOptions.KType}[arraySize]")) !*/

        return (T[]) new Object[arraySize];
    }

    /**
     * Compare two keys for equivalence. Null references return <code>true</code>.
     * Primitive types are compared using <code>==</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    public static <T> boolean equals(final Object e1, final Object e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>equals(e1, e2)",
        "<Object>==>e1 == null ? e2 == null : e1.equals(e2)",
        "<float>==>Float.floatToIntBits(e1) == Float.floatToIntBits(e2)",
        "<double>==>Double.doubleToLongBits(e1) == Double.doubleToLongBits(e2)",
        "<*>==>e1 == e2")) !*/

        return e1 == null ? e2 == null : e1.equals(e2);
    }

    /**
     * Identical as {@link #equalsKType} except that
     *  e1 Objects are assumed to be not-null.
     */
    public static <T> boolean equalsNotNull(final Object e1, final Object e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>equalsNotNull(e1, e2)",
        "<Object>==> e1.equals(e2)",
        "<float>==> Float.floatToIntBits(e1) == Float.floatToIntBits(e2)",
        "<double>==> Double.doubleToLongBits(e1) == Double.doubleToLongBits(e2)",
        "<*>==>e1 == e2")) !*/

        return e1.equals(e2);
    }

    /**
     * An intrinsic that is replaced with plain addition of arguments for
     * primitive template types. Invalid for non-number generic types.
     */
    @SuppressWarnings({ "unchecked", "boxing" })
    public static <T> T add(final T op1, final T op2) {

        /*! ($TemplateOptions.declareInline("Intrinsics.<T>add(e1 , e2)",
        "<Object>==>@INVALID_REPLACEMENT_ADD_FORBIDDEN_FOR_OBJECTS@",
        "<boolean>==>@INVALID_REPLACEMENT_ADD_FORBIDDEN_FOR_BOOLEANS@",
        "<*>==>e1 + e2")) !*/

        if (op1.getClass() != op2.getClass()) {
            throw new RuntimeException("Arguments of different classes: " + op1 + " " + op2);
        }

        if (Byte.class.isInstance(op1)) {
            return (T) (Byte) (byte) (((Byte) op1).byteValue() + ((Byte) op2).byteValue());
        }
        if (Short.class.isInstance(op1)) {
            return (T) (Short) (short) (((Short) op1).shortValue() + ((Short) op2).shortValue());
        }
        if (Character.class.isInstance(op1)) {
            return (T) (Character) (char) (((Character) op1).charValue() + ((Character) op2).charValue());
        }
        if (Integer.class.isInstance(op1)) {
            return (T) (Integer) (((Integer) op1).intValue() + ((Integer) op2).intValue());
        }
        if (Float.class.isInstance(op1)) {
            return (T) (Float) (((Float) op1).floatValue() + ((Float) op2).floatValue());
        }
        if (Long.class.isInstance(op1)) {
            return (T) (Long) (((Long) op1).longValue() + ((Long) op2).longValue());
        }
        if (Double.class.isInstance(op1)) {
            return (T) (Double) (((Double) op1).doubleValue() + ((Double) op2).doubleValue());
        }

        throw new UnsupportedOperationException("Invalid for arbitrary types: " + op1 + " " + op2);
    }

    /**
     * Compare two keys by Comparable<T>.
     * Primitive types comparison result is <code>e1 - e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare} and {@link Float#compare}.
     */
    public static <T extends Comparable<? super T>> int compare(final T e1, final T e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>compare(e1 , e2)",
        "<Object>==>e1.compareTo(e2)",
        "<float>==>Float.compare(e1 , e2)",
        "<double>==>Double.compare(e1 , e2)",
        "<boolean>==>(e1 == e2) ? 0 : ((e1)? 1 : -1)",
        "<*>==>(e1 == e2)?0:((e1 < e2)?-1:1)")) !*/

        return e1.compareTo(e2);
    }

    /**
     * Compare two keys by Comparable<T>, unchecked without Comparable signature
     * Primitive types comparison result is <code>e1 - e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare} and {@link Float#compare}.
     */
    @SuppressWarnings("unchecked")
    public static <T> int compareUnchecked(final T e1, final T e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>compareUnchecked(e1 , e2)",
        "<Object>==>((Comparable<? super T>) e1).compareTo(e2)",
        "<float>==>Float.compare(e1 , e2)",
        "<double>==>Double.compare(e1 , e2)",
        "<boolean>==>(e1 == e2) ? 0 : ((e1)? 1 : -1)",
        "<*>==>(e1 == e2)?0:((e1 < e2)?-1:1)")) !*/

        return ((Comparable<? super T>) e1).compareTo(e2);
    }

    /**
     * Compare two keys by Comparable<T>, returns true if e1.compareTo(e2) > 0
     * Primitive types comparison result is <code>e1 > e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare > 0} and {@link Float#compare > 0}.
     */
    public static <T extends Comparable<? super T>> boolean isCompSup(final T e1, final T e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>isCompSup(e1 , e2)",
         "<Object>==>e1.compareTo(e2) > 0",
        "<float>==>Float.compare(e1 , e2) > 0",
        "<double>==>Double.compare(e1 , e2) > 0",
        "<boolean>==>(e1 == e2) ? false :(e1)",
        "<*>==>e1 > e2")) !*/

        return e1.compareTo(e2) > 0;
    }

    /**
     * Compare two keys by Comparable<T>, unchecked without signature. returns true if e1.compareTo(e2) > 0
     * Primitive types comparison result is <code>e1 > e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare > 0} and {@link Float#compare > 0}.
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean isCompSupUnchecked(final T e1, final T e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>isCompSupUnchecked(e1 , e2)",
        "<Object>==>((Comparable<? super T>) e1).compareTo(e2) > 0",
        "<float>==>Float.compare(e1 , e2) > 0",
        "<double>==>Double.compare(e1 , e2) > 0",
        "<boolean>==>(e1 == e2) ? false :(e1)",
        "<*>==>e1 > e2")) !*/

        return ((Comparable<? super T>) e1).compareTo(e2) > 0;
    }

    /**
     * Compare two keys by Comparable<T>, returns true if e1.compareTo(e2) < 0
     * Primitive types comparison result is <code>e1 < e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare < 0} and {@link Float#compare < 0}.
     */
    public static <T extends Comparable<? super T>> boolean isCompInf(final T e1, final T e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>isCompInf(e1 , e2)",
        "<Object>==>e1.compareTo(e2) < 0",
        "<float>==>Float.compare(e1 , e2) < 0",
        "<double>==>Double.compare(e1 , e2) < 0",
        "<boolean>==>(e1 == e2) ? false :(e2)",
        "<*>==>e1 < e2")) !*/

        return e1.compareTo(e2) < 0;
    }

    /**
     * Compare two keys by Comparable<T>, unchecked without signature. returns true if e1.compareTo(e2) < 0
     * Primitive types comparison result is <code>e1 < e2</code>, except for floating-point types
     * where they're compared by their actual representation bits using the integrated comparison methods
     * {@link Double#compare < 0} and {@link Float#compare < 0}.
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean isCompInfUnchecked(final T e1, final T e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>isCompInfUnchecked(e1 , e2)",
        "<Object>==>((Comparable<? super T>) e1).compareTo(e2) < 0",
        "<float>==>Float.compare(e1 , e2) < 0",
        "<double>==>Double.compare(e1 , e2) < 0",
        "<boolean>==>(e1 == e2) ? false :(e2)",
        "<*>==>e1 < e2")) !*/

        return ((Comparable<? super T>) e1).compareTo(e2) < 0;
    }

    /**
     * Compare two keys by Comparable<T>, returns true if e1.compareTo(e2) == 0
     * Primitive types comparison result is <code>e1 == e2</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    public static <T extends Comparable<? super T>> boolean isCompEqual(final T e1, final T e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>isCompEqual(e1 , e2)",
        "<Object>==>e1.compareTo(e2) == 0",
        "<float>==>Float.floatToIntBits(e1) == Float.floatToIntBits(e2)",
        "<double>==>Double.doubleToLongBits(e1) == Double.doubleToLongBits(e2)",
        "<*>==>e1 == e2")) !*/

        return e1.compareTo(e2) == 0;
    }

    /**
     * Compare two keys by Comparable<T>, unchecked without signature. returns true if e1.compareTo(e2) == 0
     * Primitive types comparison result is <code>e1 == e2</code>, except for floating-point types
     * where they're compared by their actual representation bits as returned from
     * {@link Double#doubleToLongBits(double)} and {@link Float#floatToIntBits(float)}.
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean isCompEqualUnchecked(final T e1, final T e2)
    {
        /*! ($TemplateOptions.declareInline("Intrinsics.<T>isCompEqualUnchecked(e1 , e2)",
        "<Object>==>((Comparable<? super T>) e1).compareTo(e2) == 0",
        "<float>==>Float.floatToIntBits(e1) == Float.floatToIntBits(e2)",
        "<double>==>Double.doubleToLongBits(e1) == Double.doubleToLongBits(e2)",
        "<*>==>e1 == e2")) !*/

        return ((Comparable<? super T>) e1).compareTo(e2) == 0;
    }
}

//Never attempt to generate anything from this file, but parse it so put this at the end !
/*! ($TemplateOptions.doNotGenerate()) !*/

