package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import com.carrotsearch.hppcrt.*;
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

            for (int i = this.deque.tail; i != this.deque.head; i = oneRight(i, this.deque.buffer.length))
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

        final int TEST_SIZE = (int) 50;
        final int NB_ITERATIONS = (int) 1e5;

        //get a new seed for the current iteration
        final long currentSeed = RandomizedTest.randomLong();

        for (int ii = 0; ii < NB_ITERATIONS; ii++) {

            final int upperRange = RandomizedTest.randomInt(TEST_SIZE);
            final int lowerRange = RandomizedTest.randomInt(upperRange);

            //A) Sort a deque of random values of primitive types

            //A-1) full sort
            KTypeArrayDeque<KType> primitiveDeque = createDequeWithRandomData(TEST_SIZE, currentSeed);
            KTypeArrayDeque<KType> primitiveDequeOriginal = createDequeWithRandomData(TEST_SIZE, currentSeed);
            primitiveDeque.sort();
            assertOrder(primitiveDequeOriginal, primitiveDeque, 0, primitiveDequeOriginal.size());
            //A-2) Partial sort
            primitiveDeque = createDequeWithRandomData(TEST_SIZE, currentSeed);
            primitiveDequeOriginal = createDequeWithRandomData(TEST_SIZE, currentSeed);
            primitiveDeque.sort(lowerRange, upperRange);
            assertOrder(primitiveDequeOriginal, primitiveDeque, lowerRange, upperRange);

            //B) Sort with Comparator
            //B-1) Full sort
            KTypeArrayDeque<KType> comparatorDeque = createDequeWithRandomData(TEST_SIZE, currentSeed);
            KTypeArrayDeque<KType> comparatorDequeOriginal = createDequeWithRandomData(TEST_SIZE, currentSeed);
            comparatorDeque.sort(comp);
            assertOrder(comparatorDequeOriginal, comparatorDeque, 0, comparatorDequeOriginal.size());
            //B-2) Partial sort
            comparatorDeque = createDequeWithRandomData(TEST_SIZE, currentSeed);
            comparatorDequeOriginal = createDequeWithRandomData(TEST_SIZE, currentSeed);
            comparatorDeque.sort(lowerRange, upperRange, comp);
            assertOrder(comparatorDequeOriginal, comparatorDeque, lowerRange, upperRange);
        }
    }

    /* */
    @Test
    public void testEqualsVsArrayList()
    {
        this.deque.addLast(this.key1, this.key2, this.key3, this.key4, this.key5);

        final KTypeArrayList<KType> other = KTypeArrayList.newInstance();

        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        other.add(this.key1);
        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        other.add(this.key2, this.key3);
        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        other.add(this.key4, this.key5);
        Assert.assertEquals(this.deque, other);
        Assert.assertEquals(other, this.deque);
        Assert.assertEquals(this.deque.hashCode(), other.hashCode());

        //they are the same
        //modify index 2 original this.key3
        other.set(2, this.key4);
        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        //re-establish
        other.set(2, this.key3);
        Assert.assertEquals(this.deque, other);
        Assert.assertEquals(other, this.deque);
        Assert.assertEquals(this.deque.hashCode(), other.hashCode());

        //modify
        other.insert(0, this.k8);
        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        this.deque.addFirst(this.k8);
        Assert.assertEquals(this.deque, other);
        Assert.assertEquals(other, this.deque);
        Assert.assertEquals(this.deque.hashCode(), other.hashCode());
    }

    /* */
    @Test
    public void testEqualsVsLinkedList()
    {
        this.deque.addLast(this.key1, this.key2, this.key3, this.key4, this.key5);

        final KTypeLinkedList<KType> other = KTypeLinkedList.newInstance();

        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        other.add(this.key1);
        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        other.add(this.key2, this.key3);
        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        other.add(this.key4, this.key5);
        Assert.assertEquals(this.deque, other);
        Assert.assertEquals(other, this.deque);
        Assert.assertEquals(this.deque.hashCode(), other.hashCode());

        //they are the same
        //modify index 2 original this.key3
        other.set(2, this.key4);
        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        //re-establish
        other.set(2, this.key3);
        Assert.assertEquals(this.deque, other);
        Assert.assertEquals(other, this.deque);
        Assert.assertEquals(this.deque.hashCode(), other.hashCode());

        //modify
        other.insert(0, this.k8);
        Assert.assertNotEquals(this.deque, other);
        Assert.assertNotEquals(other, this.deque);

        this.deque.addFirst(this.k8);
        Assert.assertEquals(this.deque, other);
        Assert.assertEquals(other, this.deque);
        Assert.assertEquals(this.deque.hashCode(), other.hashCode());
    }

    private KTypeArrayDeque<KType> createDequeWithRandomData(final int size, final long randomSeed)
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
