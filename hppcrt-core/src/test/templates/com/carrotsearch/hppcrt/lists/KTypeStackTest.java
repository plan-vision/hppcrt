package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.mutables.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

/**
 * Unit tests for {@link KTypeStack}.
 */
//${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeStackTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeStack<KType> stack;

    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /* */
    @Before
    public void initialize()
    {
        this.stack = KTypeStack.newInstance();
    }

    /* */
    @Test
    public void testInitiallyEmpty()
    {
        Assert.assertEquals(0, this.stack.size());
    }

    /* */
    @Test
    public void testPush1()
    {
        this.stack.push(this.key1);
        Assert.assertEquals(1, this.stack.size());
        TestUtils.assertEquals2(this.key1, this.stack.peek());
        TestUtils.assertEquals2(this.key1, this.stack.pop());
        Assert.assertEquals(0, this.stack.size());
    }

    /* */
    @Test
    public void testPush2()
    {
        this.stack.push(this.key1, this.key3);
        Assert.assertEquals(2, this.stack.size());
        TestUtils.assertEquals2(this.key3, this.stack.peek());
        TestUtils.assertEquals2(this.key3, this.stack.get(0));
        TestUtils.assertEquals2(this.key1, this.stack.get(1));
        TestUtils.assertEquals2(this.key3, this.stack.pop());
        TestUtils.assertEquals2(this.key1, this.stack.pop());
        Assert.assertEquals(0, this.stack.size());
    }

    /* */
    @Test
    public void testPushArray()
    {
        this.stack.push(asArray(1, 2, 3, 4, 5), 1, 3);
        Assert.assertEquals(3, this.stack.size());
        TestUtils.assertEquals2(this.key4, this.stack.get(0));
        TestUtils.assertEquals2(this.key3, this.stack.get(1));
        TestUtils.assertEquals2(this.key2, this.stack.get(2));
    }

    /* */
    @Test
    public void testAddAllPushAll()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2, 3, 4, 5));

        this.stack.addAll(list2);
        this.stack.pushAll(list2);

        TestUtils.assertListEquals(this.stack.toArray(), 5, 4, 3, 2, 1, 0, 5, 4, 3, 2, 1, 0);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    /* */
    @Test
    public void testNullify()
    {
        this.stack.push(asArray(1, 2, 3, 4));
        this.stack.pop();
        this.stack.discard();
        this.stack.discard(2);
        Assert.assertEquals(0, this.stack.size());

        /*
         * Cleanup only for the generic version (to allow GCing of references).
         */
        for (int i = 0; i < this.stack.buffer.length; i++)
        {
            TestUtils.assertEquals2(Intrinsics.defaultKTypeValue(), this.stack.buffer[i]);
        }
    }

    /*! #end !*/

    /* */
    @Test
    public void testDiscard()
    {
        this.stack.push(this.key1, this.key3);
        Assert.assertEquals(2, this.stack.size());

        this.stack.discard();
        Assert.assertEquals(1, this.stack.size());

        this.stack.push(this.key4);
        Assert.assertEquals(2, this.stack.size());

        TestUtils.assertEquals2(4, this.stack.get(0));
        TestUtils.assertEquals2(1, this.stack.get(1));

        this.stack.discard(2);
        Assert.assertEquals(0, this.stack.size());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetAssertions()
    {
        this.stack.push(this.key1);
        this.stack.pop();
        this.stack.get(0);
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testDiscardAssertions()
    {
        this.stack.push(this.key1);
        this.stack.discard(2);
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testHashCodeEquals()
    {
        final KTypeStack<KType> s0 = KTypeStack.newInstance();
        Assert.assertEquals(1, s0.hashCode());
        Assert.assertEquals(s0, KTypeArrayList.newInstance());

        KTypeStack<KType> s1 = KTypeStack.from(this.key1, this.key2, this.key3);
        final KTypeStack<KType> s2 = KTypeStack.from(this.key1, this.key2, this.key3);

        Assert.assertEquals(s1.hashCode(), s2.hashCode());
        Assert.assertEquals(s1, s2);

        s1 = KTypeStack.from(this.key1, this.key2);
        Assert.assertFalse(s1.equals(s2));
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArrayWithClass()
    {
        final KTypeStack<Integer> l1 = KTypeStack.from(1, 2, 3);
        final Integer[] result = l1.toArray(Integer.class);
        Assert.assertArrayEquals(new Integer[] { 3, 2, 1 }, result); // dummy
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testToArray()
    {
        final KTypeStack<Integer> l1 = KTypeStack.from(1, 2, 3);
        final Object[] result = l1.toArray();
        Assert.assertArrayEquals(new Object[] { 3, 2, 1 }, result); // dummy
    }

    /*! #end !*/

    /* */
    @Test
    public void testClone()
    {
        this.stack.push(this.key1, this.key2, this.key3);

        final KTypeStack<KType> cloned = this.stack.clone();
        cloned.removeAllOccurrences(this.key1);

        TestUtils.assertSortedListEquals(this.stack.toArray(), this.key1, this.key2, this.key3);
        TestUtils.assertSortedListEquals(cloned.toArray(), this.key2, this.key3);
    }

    /* */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @SuppressWarnings("unchecked")
    /*! #end !*/
    @Test
    public void testToString()
    {
        Assert.assertEquals("["
                + this.key3 + ", "
                + this.key2 + ", "
                + this.key1 + "]", KTypeStack.from(this.key1, this.key2, this.key3).toString());
    }

    @Repeat(iterations = 50)
    @Test
    public void testPreallocatedSize()
    {
        final Random randomVK = RandomizedTest.getRandom();
        //Test that the container do not resize if less that the initial size

        //1) Choose a random number of elements
        /*! #if ($TemplateOptions.isKType("GENERIC", "INT", "LONG", "FLOAT", "DOUBLE")) !*/
        final int PREALLOCATED_SIZE = randomVK.nextInt(10000);
        /*!
            #elseif ($TemplateOptions.isKType("SHORT", "CHAR"))
             int PREALLOCATED_SIZE = randomVK.nextInt(10000);
            #else
              int PREALLOCATED_SIZE = randomVK.nextInt(10000);
            #end !*/

        //2) Preallocate to PREALLOCATED_SIZE :
        final KTypeStack<KType> newStack = KTypeStack.newInstanceWithCapacity(PREALLOCATED_SIZE);

        //3) Add PREALLOCATED_SIZE different values. At the end, size() must be == PREALLOCATED_SIZE,
        //and internal buffer/allocated must not have changed of size
        final int contructorBufferSize = newStack.buffer.length;

        for (int i = 0; i < PREALLOCATED_SIZE; i++)
        {

            newStack.add(cast(randomVK.nextInt()));

            //internal size has not changed.
            Assert.assertEquals(contructorBufferSize, newStack.buffer.length);
        }

        Assert.assertEquals(PREALLOCATED_SIZE, newStack.size());
    }

    /////////////////////
    // Overridden list methods
    ////////////////////////
    @Test
    public void testInsert()
    {
        this.stack.insert(0, this.k1);
        this.stack.insert(0, this.k2);
        this.stack.insert(2, this.k3);
        this.stack.insert(1, this.k4);
        this.stack.insert(1, this.k6);
        this.stack.insert(3, this.k7);
        this.stack.insert(6, this.k8);

        TestUtils.assertListEquals(this.stack.toArray(), 2, 6, 4, 7, 1, 3, 8);

        Assert.assertEquals(castType(this.k2), castType(this.stack.get(0)));
        Assert.assertEquals(castType(this.k6), castType(this.stack.get(1)));
        Assert.assertEquals(castType(this.k4), castType(this.stack.get(2)));
        Assert.assertEquals(castType(this.k7), castType(this.stack.get(3)));
        Assert.assertEquals(castType(this.k1), castType(this.stack.get(4)));
        Assert.assertEquals(castType(this.k3), castType(this.stack.get(5)));
        Assert.assertEquals(castType(this.k8), castType(this.stack.get(6)));

    }

    /* */
    @Test
    public void testIndexOf()
    {
        this.stack.add(asArray(0, 1, 2, 1, 0, 5));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.stack.add((KType) null);
        Assert.assertEquals(0, this.stack.indexOf(null));

        Assert.assertEquals(2, this.stack.indexOf(this.k0));
        Assert.assertEquals(-1, this.stack.indexOf(this.k3));
        Assert.assertEquals(4, this.stack.indexOf(this.k2));

        /*! #else
        Assert.assertEquals(1, stack.indexOf(k0));
        Assert.assertEquals(-1, stack.indexOf(k3));
        Assert.assertEquals(3, stack.indexOf(k2));
        #end !*/
    }

    /* */
    @Test
    public void testLastIndexOf()
    {
        this.stack.add(asArray(0, 1, 2, 3, 0, 1, 2, 3));

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.stack.add((KType) null);
        Assert.assertEquals(0, this.stack.lastIndexOf(null));

        TestUtils.assertEquals2(8, this.stack.lastIndexOf(this.k0));
        TestUtils.assertEquals2(-1, this.stack.lastIndexOf(this.k7));
        TestUtils.assertEquals2(6, this.stack.lastIndexOf(this.k2));

        /*! #else
        TestUtils.assertEquals2(7, stack.lastIndexOf(k0));
        TestUtils.assertEquals2(-1, stack.lastIndexOf(k7));
        TestUtils.assertEquals2(5, stack.lastIndexOf(k2));
        #end !*/

    }

    /* */
    @Test
    public void testSet()
    {
        this.stack.add(asArray(0, 1, 2, 3, 4));

        TestUtils.assertEquals2(4, this.stack.set(0, this.k3));
        TestUtils.assertEquals2(3, this.stack.set(1, this.k4));
        TestUtils.assertEquals2(2, this.stack.set(2, this.k5));

        //set again
        TestUtils.assertEquals2(3, this.stack.set(0, this.k7));
        TestUtils.assertEquals2(4, this.stack.set(1, this.k6));
        TestUtils.assertEquals2(5, this.stack.set(2, this.k5));

        TestUtils.assertListEquals(this.stack.toArray(), 7, 6, 5, 1, 0);
    }

    /* */
    @Test
    public void testRemoveFirstLast()
    {
        this.stack.add(asArray(0, 1, 2, 3, 4, 0, 1, 2, 3, 4));

        Assert.assertEquals(-1, this.stack.removeFirstOccurrence(this.k5));
        Assert.assertEquals(-1, this.stack.removeLastOccurrence(this.k5));
        TestUtils.assertListEquals(this.stack.toArray(), 4, 3, 2, 1, 0, 4, 3, 2, 1, 0);

        Assert.assertEquals(3, this.stack.removeFirstOccurrence(this.k1));
        TestUtils.assertListEquals(this.stack.toArray(), 4, 3, 2, 0, 4, 3, 2, 1, 0);
        Assert.assertEquals(8, this.stack.removeLastOccurrence(this.k0));
        TestUtils.assertListEquals(this.stack.toArray(), 4, 3, 2, 0, 4, 3, 2, 1);
        Assert.assertEquals(6, this.stack.removeLastOccurrence(this.k2));
        TestUtils.assertListEquals(this.stack.toArray(), 4, 3, 2, 0, 4, 3, 1);
        Assert.assertEquals(1, this.stack.removeFirstOccurrence(this.k3));
        TestUtils.assertListEquals(this.stack.toArray(), 4, 2, 0, 4, 3, 1);

        Assert.assertEquals(2, this.stack.removeLastOccurrence(this.k0));
        Assert.assertEquals(-1, this.stack.removeLastOccurrence(this.k0));
        TestUtils.assertListEquals(this.stack.toArray(), 4, 2, 4, 3, 1);

        /*! #if ($TemplateOptions.KTypeGeneric) !*/
        this.stack.clear();
        this.stack.add(newArray(this.k0, null, this.k2, null, this.k0));
        Assert.assertEquals(1, this.stack.removeFirstOccurrence(null));
        Assert.assertEquals(2, this.stack.removeLastOccurrence(null));
        TestUtils.assertListEquals(this.stack.toArray(), 0, 2, 0);
        /*! #end !*/
    }

    /* */
    @Test
    public void testRemove()
    {
        this.stack.add(asArray(0, 1, 2, 3, 4, 5, 6));

        this.stack.remove(0);
        this.stack.remove(2);
        this.stack.remove(1);

        TestUtils.assertListEquals(this.stack.toArray(), 5, 2, 1, 0);
    }

    /* */
    @Test
    public void testRemoveRange()
    {
        this.stack.add(asArray(0, 1, 2, 3, 4, 5, 6, 7, 8));

        this.stack.removeRange(0, 2);
        TestUtils.assertListEquals(this.stack.toArray(), 6, 5, 4, 3, 2, 1, 0);

        this.stack.removeRange(2, 3);
        TestUtils.assertListEquals(this.stack.toArray(), 6, 5, 3, 2, 1, 0);

        this.stack.removeRange(1, 1);
        TestUtils.assertListEquals(this.stack.toArray(), 6, 5, 3, 2, 1, 0);

        this.stack.removeRange(0, 1);
        TestUtils.assertListEquals(this.stack.toArray(), 5, 3, 2, 1, 0);
    }

    @Repeat(iterations = 100)
    @Test
    public void testSort()
    {
        //natural ordering comparator
        final KTypeComparator<KType> comp = new KTypeComparator<KType>() {

            @Override
            public int compare(final KType e1, final KType e2)
            {
                int res = 0;

                if (castType(e1) < castType(e2))
                {
                    res = -1;
                }
                else if (castType(e1) > castType(e2))
                {
                    res = 1;
                }

                return res;
            }
        };

        final int TEST_SIZE = (int) 1e4;
        //get a new seed for the current iteration
        final long currentSeed = RandomizedTest.randomLong();

        final int upperRange = RandomizedTest.randomInt(TEST_SIZE);
        final int lowerRange = RandomizedTest.randomInt(upperRange);

        //A) Sort a stack of random values of primitive types

/*! #if ($TemplateOptions.KTypePrimitive)
        //A-1) full sort
        KTypeStack<KType> primitiveList = createStackWithRandomData(TEST_SIZE, currentSeed);
        KTypeStack<KType> primitiveListOriginal = createStackWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort();
        assertOrder(primitiveListOriginal, primitiveList, 0, primitiveList.size());
        //A-2) Partial sort
        primitiveList = createStackWithRandomData(TEST_SIZE, currentSeed);
        primitiveListOriginal = createStackWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort(lowerRange, upperRange);
        assertOrder(primitiveListOriginal, primitiveList, lowerRange, upperRange);
#end !*/

        //B) Sort with Comparator
        //B-1) Full sort
        KTypeStack<KType> comparatorList = createStackWithRandomData(TEST_SIZE, currentSeed);
        KTypeStack<KType> comparatorListOriginal = createStackWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(comp);
        assertOrder(comparatorListOriginal, comparatorList, 0, comparatorList.size());
        //B-2) Partial sort
        comparatorList = createStackWithRandomData(TEST_SIZE, currentSeed);
        comparatorListOriginal = createStackWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(lowerRange, upperRange, comp);
        assertOrder(comparatorListOriginal, comparatorList, lowerRange, upperRange);
    }

    /**
     * Test natural ordering between [startIndex; endIndex[, starting from original
     * @param expected
     * @param actual
     * @param length
     */
    private void assertOrder(final KTypeStack<KType> original, final KTypeStack<KType> order, final int startIndex, final int endIndex)
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

    private KTypeStack<KType> createStackWithRandomData(final int size, final long randomSeed)
    {
        final Random prng = new Random(randomSeed);

        final KTypeStack<KType> newArray = KTypeStack.newInstanceWithCapacity(KTypeArrayList.DEFAULT_CAPACITY);

        for (int i = 0; i < size; i++)
        {
            newArray.add(cast(prng.nextInt(size)));
        }

        return newArray;
    }

}
