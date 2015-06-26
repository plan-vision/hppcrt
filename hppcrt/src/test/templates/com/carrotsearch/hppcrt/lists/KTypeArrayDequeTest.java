package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.carrotsearch.randomizedtesting.annotations.Seed;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit tests for {@link KTypeArrayDeque}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayDequeTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeArrayDeque<KType> deque;

    public volatile long guard;

    /**
     * Some sequence values for tests.
     */
    private KTypeArrayList<KType> sequence;

    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /* */
    @Before
    public void initialize()
    {
        this.deque = KTypeArrayDeque.newInstance();
        this.sequence = KTypeArrayList.newInstance();

        for (int i = 0; i < 10000; i++) {
            this.sequence.add(cast(i));
        }
    }

    /**
     * Move one index to the right, wrapping around buffer of size modulus
     */
    private int oneRight(final int index, final int modulus)
    {
        return (index + 1 == modulus) ? 0 : index + 1;
    }

    @After
    public void checkConsistency()
    {
        if (this.deque != null)
        {
            int count = 0;
            //check access by get()
            for (/*! #if ($TemplateOptions.KTypeGeneric) !*/final Object
                    /*! #else
            final KType
            #end !*/
                    val : this.deque.toArray()) {

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                TestUtils.assertEquals2(val, (Object) this.deque.get(count));
                /*! #else
                TestUtils.assertEquals2(val, this.deque.get(count));
                #end !*/
                count++;
            }

            Assert.assertEquals(count, this.deque.size());

            //check beyond validity range

            for (int i = this.deque.tail; i < this.deque.head; i = oneRight(i, this.deque.buffer.length))
            {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> empty() == this.deque.buffer[i]);
                /*! #end !*/
            }
        }
    }

    /* */
    @Test
    public void testAddAllFirst()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.deque.addFirst(list2);
        TestUtils.assertListEquals(this.deque.toArray(), 2, 1, 0);
        this.deque.addFirst(list2);
        TestUtils.assertListEquals(this.deque.toArray(), 2, 1, 0, 2, 1, 0);

        this.deque.clear();
        final KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        this.deque.addFirst(deque2);
        TestUtils.assertListEquals(this.deque.toArray(), 2, 1, 0);
    }

    /* */
    @Test
    public void testAddAllLast()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.deque.addLast(list2);
        TestUtils.assertListEquals(this.deque.toArray(), 0, 1, 2);
        this.deque.addLast(list2);
        TestUtils.assertListEquals(this.deque.toArray(), 0, 1, 2, 0, 1, 2);

        this.deque.clear();
        final KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        this.deque.addLast(deque2);
        TestUtils.assertListEquals(this.deque.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testGetAndGrowth()
    {
        final int maxGrowth = 10;
        final int count = 500;

        this.deque = new KTypeArrayDeque<KType>(0,
                new BoundedProportionalArraySizingStrategy(5, maxGrowth, 2));

        for (int i = 0; i < count; i++) {
            this.deque.add(cast(i));
        }

        Assert.assertEquals(count, this.deque.size());

        for (int i = 0; i < count; i++) {
            TestUtils.assertEquals2(cast(i), this.deque.get(i));
        }

        Assert.assertTrue("Buffer size: 510 <= " + this.deque.buffer.length,
                this.deque.buffer.length <= count + maxGrowth);
    }

    /* */
    @Test
    public void testRelease()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addFirst(this.k3);
        this.deque.release();
        Assert.assertEquals(0, this.deque.size());
        Assert.assertEquals(0, this.deque.head);
        Assert.assertEquals(0, this.deque.tail);

        this.deque.addLast(this.k1);
        TestUtils.assertListEquals(this.deque.toArray(), 1);
    }

    @Seed("1F4A04B1D776DCB6")
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

        //A) Sort a deque of random values of primitive types

        //A-1) full sort
        KTypeArrayDeque<KType> primitiveDeque = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        KTypeArrayDeque<KType> primitiveDequeOriginal = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        primitiveDeque.sort();
        assertOrder(primitiveDequeOriginal, primitiveDeque, 0, primitiveDequeOriginal.size());
        //A-2) Partial sort
        primitiveDeque = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        primitiveDequeOriginal = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        primitiveDeque.sort(lowerRange, upperRange);
        assertOrder(primitiveDequeOriginal, primitiveDeque, lowerRange, upperRange);

        //B) Sort with Comparator
        //B-1) Full sort
        KTypeArrayDeque<KType> comparatorDeque = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        KTypeArrayDeque<KType> comparatorDequeOriginal = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        comparatorDeque.sort(comp);
        assertOrder(comparatorDequeOriginal, comparatorDeque, 0, comparatorDequeOriginal.size());
        //B-2) Partial sort
        comparatorDeque = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        comparatorDequeOriginal = creatDequeWithRandomData(TEST_SIZE, currentSeed);
        comparatorDeque.sort(lowerRange, upperRange, comp);
        assertOrder(comparatorDequeOriginal, comparatorDeque, lowerRange, upperRange);
    }

    private KTypeArrayDeque<KType> creatDequeWithRandomData(final int size, final long randomSeed)
    {
        final Random prng = new Random(randomSeed);

        final KTypeArrayDeque<KType> newDeque = KTypeArrayDeque.newInstance();

        while (newDeque.size() < size)
        {
            final KType newValueToInsert = cast(prng.nextInt());
            final boolean insertInTail = prng.nextInt() % 7 == 0;
            final boolean deleteHead = prng.nextInt() % 17 == 0;

            if (deleteHead && !newDeque.isEmpty())
            {
                newDeque.removeFirst();
            }
            else if (insertInTail)
            {
                newDeque.addLast(newValueToInsert);
            }
            else
            {
                newDeque.addFirst(newValueToInsert);
            }
        }

        return newDeque;
    }
}
