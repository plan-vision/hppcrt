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
// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
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
    public void testAddFirst()
    {
        this.deque.addFirst(this.k1);
        this.deque.addFirst(this.k2);
        this.deque.addFirst(this.k3);
        TestUtils.assertListEquals(this.deque.toArray(), 3, 2, 1);
        Assert.assertEquals(3, this.deque.size());
    }

    /* */
    @Test
    public void testAddLast()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addLast(this.k3);
        TestUtils.assertListEquals(this.deque.toArray(), 1, 2, 3);
        Assert.assertEquals(3, this.deque.size());
    }

    /* */
    @Test
    public void testAddFirstWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++)
        {
            this.deque.addFirst(Intrinsics.<KType> cast(this.sequence.buffer[i]));
        }

        TestUtils.assertListEquals(TestUtils.reverse(this.sequence.toArray()), this.deque.toArray());
    }

    /* */
    @Test
    public void testAddLastWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++) {
            this.deque.addLast(Intrinsics.<KType> cast(this.sequence.buffer[i]));
        }

        TestUtils.assertListEquals(this.sequence.toArray(), this.deque.toArray());
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
    public void testRemoveFirst()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addLast(this.k3);

        this.deque.removeFirst();
        TestUtils.assertListEquals(this.deque.toArray(), 2, 3);
        Assert.assertEquals(2, this.deque.size());

        this.deque.addFirst(this.k4);
        TestUtils.assertListEquals(this.deque.toArray(), 4, 2, 3);
        Assert.assertEquals(3, this.deque.size());

        this.deque.removeFirst();
        this.deque.removeFirst();
        this.deque.removeFirst();
        Assert.assertEquals(0, this.deque.toArray().length);
        Assert.assertEquals(0, this.deque.size());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveFirstEmpty()
    {
        this.deque.removeFirst();
    }

    /* */
    @Test
    public void testRemoveLast()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addLast(this.k3);

        this.deque.removeLast();
        TestUtils.assertListEquals(this.deque.toArray(), 1, 2);
        Assert.assertEquals(2, this.deque.size());

        this.deque.addLast(this.k4);
        TestUtils.assertListEquals(this.deque.toArray(), 1, 2, 4);
        Assert.assertEquals(3, this.deque.size());

        this.deque.removeLast();
        this.deque.removeLast();
        this.deque.removeLast();
        Assert.assertEquals(0, this.deque.toArray().length);
        Assert.assertEquals(0, this.deque.size());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveLastEmpty()
    {
        this.deque.removeLast();
    }

    /* */
    @Test
    public void testGetFirst()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        TestUtils.assertEquals2(this.k1, this.deque.getFirst());
        this.deque.addFirst(this.k3);
        TestUtils.assertEquals2(this.k3, this.deque.getFirst());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetFirstEmpty()
    {
        this.deque.getFirst();
    }

    /* */
    @Test
    public void testGetLast()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        TestUtils.assertEquals2(this.k2, this.deque.getLast());
        this.deque.addLast(this.k3);
        TestUtils.assertEquals2(this.k3, this.deque.getLast());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetLastEmpty()
    {
        this.deque.getLast();
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
    public void testRemoveFirstOccurrence()
    {
        final int modulo = 10;
        final int count = 10000;
        this.sequence.clear();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.deque.addFirst(cast(i % modulo));
                this.sequence.insert(0, cast(i % modulo));
            }
            else {
                this.deque.addLast(cast(i % modulo));
                this.sequence.add(cast(i % modulo));
            }
        }

        //at that point, both IndexedContainers are the same
        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        final Random rnd = new Random(0xdeadbeef);

        //remove values randomly
        for (int i = 0; i < 500; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));

            //both ArrayList and ArrayDequeue returns the same index:
            Assert.assertEquals(" at i = " + i,
                    this.sequence.removeFirst(k),
                    this.deque.removeFirst(k));
        }

        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        //non-existent element
        Assert.assertTrue(0 > this.deque.removeFirst(cast(modulo + 1)));

        //now existing
        this.deque.addLast(cast(modulo + 1));
        Assert.assertTrue(0 <= this.deque.removeFirst(cast(modulo + 1)));
    }

    /* */
    @Test
    public void testEquals()
    {
        final int modulo = 127;
        final int count = 15000;

        final KTypeArrayDeque<KType> deque2 = KTypeArrayDeque.newInstance();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.deque.addFirst(cast(i % modulo));
                deque2.addFirst(cast(i % modulo));
            }
            else {
                this.deque.addLast(cast(i % modulo));
                deque2.addLast(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.deque.toArray(), deque2.toArray());

        //Both dequeue are indeed equal
        Assert.assertTrue(this.deque.equals(deque2));
    }

    /* */
    @Test
    public void testIndexedContainerEquals()
    {
        final int modulo = 127;
        final int count = 10000;
        this.sequence.clear();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.deque.addFirst(cast(i % modulo));
                this.sequence.insert(0, cast(i % modulo));
            }
            else {
                this.deque.addLast(cast(i % modulo));
                this.sequence.add(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        //The array list and dequeue are indeed equal
        Assert.assertTrue(this.deque.equals(this.sequence));
    }

    /* */
    @Test
    public void testRemoveLastOccurrence()
    {
        final int modulo = 10;
        final int count = 10000;
        this.sequence.clear();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.deque.addFirst(cast(i % modulo));
                this.sequence.insert(0, cast(i % modulo));
            }
            else {
                this.deque.addLast(cast(i % modulo));
                this.sequence.add(cast(i % modulo));
            }
        }

        //at that point, both IndexedContainers are the same
        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        final Random rnd = new Random(0x11223344);

        //remove values randomly
        for (int i = 0; i < 500; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));

            //both ArrayList and ArrayDequeue returns the same index:
            Assert.assertEquals(" at i = " + i,
                    this.sequence.removeLast(k),
                    this.deque.removeLast(k));
        }

        TestUtils.assertListEquals(this.deque.toArray(), this.sequence.toArray());

        //non existent element
        Assert.assertTrue(0 > this.deque.removeLast(cast(modulo + 1)));

        //now existing
        this.deque.addFirst(cast(modulo + 1));
        Assert.assertTrue(0 <= this.deque.removeLast(cast(modulo + 1)));
    }

    /* */
    @Test
    public void testClear()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addFirst(this.k3);
        this.deque.clear();
        Assert.assertEquals(0, this.deque.size());
        Assert.assertEquals(0, this.deque.head);
        Assert.assertEquals(0, this.deque.tail);

        this.deque.addLast(this.k1);
        TestUtils.assertListEquals(this.deque.toArray(), 1);
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

    /* */
    @Test
    public void testDescendingIterator()
    {
        this.deque.addLast(this.sequence);

        int index = this.sequence.size() - 1;
        for (final Iterator<KTypeCursor<KType>> i = this.deque.descendingIterator(); i.hasNext();)
        {
            final KTypeCursor<KType> cursor = i.next();
            TestUtils.assertEquals2(this.sequence.buffer[index], cursor.value);
            TestUtils.assertEquals2(this.deque.buffer[cursor.index], cursor.value);
            index--;
        }
        Assert.assertEquals(-1, index);

        this.deque.clear();
        Assert.assertFalse(this.deque.descendingIterator().hasNext());
    }

    /* */
    @Test
    public void testDescendingForEachWithProcedure()
    {
        this.deque.addLast(this.sequence);

        final IntHolder count = new IntHolder();
        this.deque.descendingForEach(new KTypeProcedure<KType>() {
            int index = KTypeArrayDequeTest.this.sequence.size();

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(KTypeArrayDequeTest.this.sequence.buffer[--this.index], v);
                count.value++;
            }
        });
        Assert.assertEquals(count.value, this.deque.size());
    }

    /* */
    @Test
    public void testDescendingForEachWithPredicate()
    {
        this.deque.addLast(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.deque.descendingForEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = KTypeArrayDequeTest.this.deque.size() - 1;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeArrayDequeTest.this.deque.get(this.index));
                this.value = castType(v);

                if (this.value == 9) {

                    return false;
                }

                this.index--;

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 9);
    }

    /* */
    @Test
    public void testDescendingForEachWithPredicateAllwaysTrue()
    {
        this.deque.addLast(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.deque.descendingForEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = KTypeArrayDequeTest.this.deque.size() - 1;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeArrayDequeTest.this.deque.get(this.index));
                this.value = castType(v);

                this.index--;

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 1);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAgainstArrayDeque()
    {
        final Random rnd = new Random(78461644457454L);
        final int rounds = 10000;
        final int modulo = 100;

        final ArrayDeque<KType> ad = new ArrayDeque<KType>();

        for (int i = 0; i < rounds; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));

            final int op = rnd.nextInt(8);
            if (op < 2)
            {
                this.deque.addFirst(k);
                ad.addFirst(k);
            }
            else if (op < 4)
            {
                this.deque.addLast(k);
                ad.addLast(k);
            }
            else if (op < 5 && ad.size() > 0)
            {
                this.deque.removeLast();
                ad.removeLast();
            }
            else if (op < 6 && ad.size() > 0)
            {
                this.deque.removeLast();
                ad.removeLast();
            }
            else if (op < 7)
            {
                Assert.assertEquals(
                        ad.removeFirstOccurrence(k),
                        this.deque.removeFirst(k) >= 0);
            }
            else if (op < 8)
            {
                Assert.assertEquals(
                        ad.removeLastOccurrence(k),
                        this.deque.removeLast(k) >= 0);
            }
            Assert.assertEquals(ad.size(), this.deque.size());
        }

        Assert.assertArrayEquals(ad.toArray(), this.deque.toArray());
    }

    /*! #end !*/

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAgainstArrayDequeVariousTailHeadPositions()
    {
        this.deque.clear();
        this.deque.head = this.deque.tail = 2;
        testAgainstArrayDeque();

        this.deque.clear();
        this.deque.head = this.deque.tail = this.deque.buffer.length - 2;
        testAgainstArrayDeque();

        this.deque.clear();
        this.deque.head = this.deque.tail = this.deque.buffer.length / 2;
        testAgainstArrayDeque();
    }

    /*! #end !*/

    @Test
    public void testDescendingPooledIteratorFullIteratorLoop()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeArrayDeque<KType> testContainer = createDequeWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        long testValue = 0;

        final int startingPoolDescendingSize = testContainer.descendingValueIteratorPool.size();

        for (int round = 0; round < TEST_ROUNDS; round++)
        {

            // Descending iterator loop
            final int initialPoolSize = testContainer.descendingValueIteratorPool.size();

            final KTypeArrayDeque<KType>.DescendingValueIterator loopIterator = testContainer.descendingIterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.descendingValueIteratorPool.size());

            testValue = 0;
            while (loopIterator.hasNext())
            {
                testValue += castType(loopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, testContainer.descendingValueIteratorPool.size());

            //checksum
            Assert.assertEquals(checksum, testValue);

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolDescendingSize, testContainer.descendingValueIteratorPool.size());
    }

    @Test
    public void testDescendingPooledIteratorBrokenIteratorLoop()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeArrayDeque<KType> testContainer = createDequeWithOrderedData(TEST_SIZE);

        final int startingPoolDescendingSize = testContainer.descendingValueIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            // Descending iteration
            final int initialPoolSize = testContainer.descendingValueIteratorPool.size();

            final KTypeArrayDeque<KType>.DescendingValueIterator loopIterator = testContainer.descendingIterator();

            Assert.assertEquals(initialPoolSize - 1, testContainer.descendingValueIteratorPool.size());

            count = 0;
            while (loopIterator.hasNext())
            {
                this.guard += castType(loopIterator.next().value);

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2)
                {
                    break;
                }
                count++;
            } //end IteratorLoop

            //iterator is NOT returned to its pool, due to the break.
            Assert.assertEquals(initialPoolSize - 1, testContainer.descendingValueIteratorPool.size());

            //manual return to the pool
            loopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, testContainer.descendingValueIteratorPool.size());

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolDescendingSize, testContainer.descendingValueIteratorPool.size());
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

    private KTypeArrayDeque<KType> createDequeWithOrderedData(final int size)
    {
        final KTypeArrayDeque<KType> newArray = KTypeArrayDeque.newInstance();

        for (int i = 0; i < size; i++)
        {
            newArray.addLast(cast(i));
        }

        return newArray;
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
