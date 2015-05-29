package com.carrotsearch.hppcrt;

import java.util.Comparator;
import java.util.Random;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.MethodRule;

import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit helpers for <code>KType</code>.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
/*! #if ($TemplateOptions.KTypeGeneric) !*/
@SuppressWarnings("unchecked")
/*! #end !*/
public abstract class AbstractKTypeTest<KType> extends RandomizedTest
{
    /**
     * Require assertions for all tests.
     */
    @Rule
    public MethodRule requireAssertions = new RequireAssertionsRule();

    /* Ready to use key values. */

    /**
     * KeyE is special: its value is == null for generics, and == 0 for primitives.
     */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    protected KType keyE = null;
    /*! #else
    protected KType keyE = cast(0);
     #end !*/

    protected KType key0 = cast(0), k0 = this.key0;
    protected KType key1 = cast(1), k1 = this.key1;
    protected KType key2 = cast(2), k2 = this.key2;
    protected KType key3 = cast(3), k3 = this.key3;
    protected KType key4 = cast(4), k4 = this.key4;
    protected KType key5 = cast(5), k5 = this.key5;
    protected KType key6 = cast(6), k6 = this.key6;
    protected KType key7 = cast(7), k7 = this.key7;
    protected KType key8 = cast(8), k8 = this.key8;
    protected KType key9 = cast(9), k9 = this.key9;

    /**
     * Convert int to target KType type.
     * If KType is generic, KType is also a Number, so is Comparable.
     */
    public KType cast(final int v)
    {
        /*! #if ($TemplateOptions.KTypePrimitive)
             #if ($TemplateOptions.KTypeNumeric)
               return (KType) v;
             #else
                return  (v > 0);
             #end
         #else !*/
        // @SuppressWarnings("unchecked")
        final KType k = (KType) (Object) v;
        return k;
        /*! #end !*/
    }

    /**
     * Convert a KType to int, (KType being a boxed elementary type or a primitive), else
     * returns 0.
     */
    @SuppressWarnings("hiding")
    public <KType> int castType(final KType type)
    {
        /*! #if ($TemplateOptions.KTypePrimitive)
              #if ($TemplateOptions.KTypeNumeric)
                return (int) type;
              #else
                return (type?1:0);
             #end
        #else !*/
        long k = 0L;

        if (type == null) {

            k = 0L;
        }
        else if (type instanceof Character) {

            k = ((Character) type).charValue();

        }
        else if (type instanceof Number) {

            k = ((Number) type).longValue();
        }

        return (int) k;
        /*! #end !*/
    }

    public KType[] asArray(final int... ints)
    {
        final KType[] values = Intrinsics.<KType> newArray(ints.length);

        for (int i = 0; i < ints.length; i++)
        {
            /*! #if ($TemplateOptions.KTypePrimitive)
                 #if ($TemplateOptions.KTypeNumeric)
                  values[i] = (KType) ints[i];
               #else
                  values[i] = (ints[i] > 0)?true:false;
               #end
            #else !*/
            values[i] = (KType) (Object) ints[i];
            /*! #end !*/
        }

        return values;
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    public KType[] asArrayObjects(final Object... objects)
    {
        final KType[] values = Intrinsics.<KType> newArray(objects.length);

        for (int i = 0; i < objects.length; i++)
        {
            if (objects[i] != null) {

                values[i] = (KType) objects[i];
            }
            else {
                values[i] = null;
            }
        }

        return values;
    }

    /*! #end !*/

    /**
     * Create a new array of a given type and copy the arguments to this array.
     * These elements are indeed Comparable Numbers.
     * (deep copy)
     */
    public KType[] newArray(final KType... elements)
    {
        final KType[] values = Intrinsics.<KType> newArray(elements.length);

        for (int i = 0; i < elements.length; i++)
        {
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            if (elements[i] != null)
            {
                values[i] = elements[i];
            }
            else
            {
                values[i] = null;
            }
            /*! #else
                   values[i] =  (KType)elements[i];
                #end !*/
        }

        return values;
    }

    /**
     * Test natural ordering for a KTypeIndexedContainer between [startIndex; endIndex[, starting from original
     * @param expected
     * @param actual
     * @param length
     */
    public void assertOrder(final KTypeIndexedContainer<KType> original, final KTypeIndexedContainer<KType> order, final int startIndex, final int endIndex)
    {
        Assert.assertEquals(original.size(), order.size());

        //A) check that the required range is ordered
        for (int i = startIndex + 1; i < endIndex; i++)
        {
            if (castType(order.get(i - 1)) > castType(order.get(i)))
            {
                Assert.assertTrue(String.format("Not ordered: (previous, next) = (%d, %d) at index %d",
                        castType(order.get(i - 1)), castType(order.get(i)), i), false);
            }
        }

        //B) Check that the rest is untouched also
        for (int i = 0; i < startIndex; i++)
        {
            if (castType(original.get(i)) != castType(order.get(i)))
            {
                Assert.assertTrue(String.format("This index has been touched: (original, erroneously modified) = (%d, %d) at index %d",
                        castType(original.get(i)), castType(order.get(i)), i), false);
            }
        }

        for (int i = endIndex; i < original.size(); i++)
        {
            if (castType(original.get(i)) != castType(order.get(i)))
            {
                Assert.assertTrue(String.format("This index has been touched: (original, erroneously modified) = (%d, %d) at index %d",
                        castType(original.get(i)), castType(order.get(i)), i), false);
            }
        }
    }

    /**
     * Test KTypeComparator<KType> ordering for a array between [startIndex; endIndex[, starting from original
     * @param expected
     * @param actual
     * @param length
     */
    public void assertOrder(final KType[] original, final KType[] order, final int startIndex, final int endIndex,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/final Comparator<? super KType> /*! #else  final KTypeComparator<KType> #end !*/comp)
    {
        Assert.assertEquals(original.length, order.length);

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        Comparator<? super KType>
        /*! #else  KTypeComparator<KType> #end !*/
        internalCompare = comp;

        if (comp == null) {
            internalCompare = new KTypeStandardComparator<KType>();
        }

        //A) check that the required range is ordered
        for (int i = startIndex + 1; i < endIndex; i++)
        {
            if (internalCompare.compare(order[i - 1], order[i]) > 0)
            {
                Assert.assertTrue(String.format("Not ordered: (previous, next) = (%d, %d) at index %d",
                        castType(order[i - 1]), castType(order[i]), i), false);
            }
        }

        //B) Check that the rest is untouched also
        for (int i = 0; i < startIndex; i++)
        {
            if (internalCompare.compare(original[i], order[i]) != 0)
            {
                Assert.assertTrue(String.format("This index has been touched: (original, erroneously modified) = (%d, %d) at index %d",
                        castType(original[i]), castType(order[i]), i), false);
            }
        }

        for (int i = endIndex; i < original.length; i++)
        {
            if (internalCompare.compare(original[i], order[i]) != 0)
            {
                Assert.assertTrue(String.format("This index has been touched: (original, erroneously modified) = (%d, %d) at index %d",
                        castType(original[i]), castType(order[i]), i), false);
            }
        }
    }

    /**
     * Test natural ordering of the whole array
     * @param expected
     * @param actual
     * @param length
     */
    public void assertOrder(final KType[] order)
    {
        assertOrder(order, order, 0, order.length - 1, null);
    }

    /**
     * Test natural ordering for a array between [startIndex; endIndex[, starting from original
     * @param expected
     * @param actual
     * @param length
     */
    public void assertOrder(final KType[] original, final KType[] order, final int startIndex, final int endIndex)
    {
        assertOrder(original, order, startIndex, endIndex, null);
    }

    /**
     * Test KTypeComparator<KType> ordering of the whole array
     * @param expected
     * @param actual
     * @param length
     */
    public void assertOrder(final KType[] order,
            /*! #if ($TemplateOptions.KTypeGeneric) !*/final Comparator<? super KType> /*! #else  final KTypeComparator<KType> #end !*/comp)
    {
        if (order.length > 0) {
            assertOrder(order, order, 0, order.length - 1, comp);
        }
    }

    /**
     * Create an array filled with random data
     * @param size
     * @param currentSeed
     * @return
     */
    public KType[] createArrayWithRandomData(final int size, final long currentSeed)
    {
        final Random prng = new Random(currentSeed);

        final KType[] newArray = Intrinsics.<KType> newArray(size);

        for (int i = 0; i < size; i++)
        {
            newArray[i] = cast(prng.nextInt());
        }

        return newArray;
    }

    /**
     * Create an array filled with random Comparable data
     * @param size
     * @param currentSeed
     * @return
     */
    public KType[] createArrayWithComparableRandomData(final int size, final long currentSeed)
    {
        return newArray(createArrayWithRandomData(size, currentSeed));
    }

    /**
     * Make a deep copy, the copied elements are indeed cast to Comparable Numbers
     * @param x
     * @return
     */
    public KType[] copy(final KType[] x)
    {
        return newArray(x);
    }
}
