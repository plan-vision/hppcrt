package com.carrotsearch.hppcrt;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.MethodRule;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.randomizedtesting.annotations.*;

/**
 * Unit helpers for <code>KType</code> and <code>VType</code> pair containers
 */

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/*! #if ($TemplateOptions.KTypeGeneric) !*/
@SuppressWarnings("unchecked")
/*! #end !*/
/* ! ${TemplateOptions.generatedAnnotation} ! */
public abstract class AbstractKTypeVTypeTest<KType, VType> extends AbstractKTypeTest<KType>
{

    /**
     * valueE is special: its value is == null for generics, and == 0 for primitives.
     */
    /*! #if ($TemplateOptions.VTypeGeneric) !*/
    protected VType valueE = null;
    /*! #else
    protected VType valueE = vcast(0);
     #end !*/

    protected VType value0 = vcast(0);
    protected VType value1 = vcast(1);
    protected VType value2 = vcast(2);
    protected VType value3 = vcast(3);
    protected VType value4 = vcast(4);
    protected VType value5 = vcast(5);
    protected VType value6 = vcast(6);
    protected VType value7 = vcast(7);
    protected VType value8 = vcast(8);
    protected VType value9 = vcast(9);

    public volatile long guard;

    /**
     * Convert to VType type from an integer used to test stuff.
     */
    protected VType vcast(final int value)
    {
        /*! #if ($TemplateOptions.VTypePrimitive)
             return (VType) value;
         #else !*/
        @SuppressWarnings("unchecked")
        final VType v = (VType) (Object) value;
        return v;
        /*! #end !*/
    }

    /**
     * Convert a VType to int, (VType being a boxed elementary type or a primitive), else
     * returns 0.
     */
    protected int vcastType(final VType type)
    {
        /*! #if ($TemplateOptions.VTypePrimitive)
               return (int) type;
        #else !*/
        long k = 0L;

        if (type instanceof Character)
        {
            k = ((Character) type).charValue();
        }
        else if (type instanceof Number)
        {
            k = ((Number) type).longValue();
        }

        return (int) k;
        /*! #end !*/
    }

    /**
     * Create a new array of a given type and copy the arguments to this array.
     */
    protected VType[] newvArray(final VType... elements)
    {
        final VType[] values = Intrinsics.<VType> newArray(elements.length);

        for (int i = 0; i < elements.length; i++)
        {
            /*! #if ($TemplateOptions.VTypeGeneric) !*/
            if (elements[i] != null)
            {
                values[i] = elements[i];
            }
            else
            {
                values[i] = null;
            }
            /*! #else
                   values[i] =  (VType)elements[i];
                #end !*/
        }

        return values;
    }

    /**
     * Return true if VType is part of the array
     */
    public boolean isInVArray(final VType[] values, final VType expected) {

        boolean inArray = false;

        for (int i = 0; i < values.length; i++)
        {
            if (Intrinsics.<VType> equals(values[i], expected)) {

                inArray = true;
                break;
            }
        }

        return inArray;
    }

    protected void assertSameMap(

            final KTypeVTypeMap<KType, VType> c1,
            final KTypeVTypeMap<KType, VType> c2)
    {
        Assert.assertEquals(c1.size(), c2.size());

        c1.forEach(new KTypeVTypeProcedure<KType, VType>()
                {
            @Override
            public void apply(final KType key, final VType value)
            {
                Assert.assertTrue(c2.containsKey(key));
                TestUtils.assertEquals2(value, c2.get(key));
            }
                });
    }
}