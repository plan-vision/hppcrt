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

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit tests for specific {@link KTypeLinkedList}.
 */
//${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeLinkedListTest<KType> extends AbstractKTypeTest<KType>
{
    /**
     * Per-test fresh initialized instance.
     */
    public KTypeLinkedList<KType> list;

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
        //create a vary small list to force reallocs.
        this.list = KTypeLinkedList.newInstance(2);

        this.sequence = KTypeArrayList.newInstance();

        for (int i = 0; i < 10000; i++) {
            this.sequence.add(cast(i));
        }
    }

    @After
    public void checkConsistency()
    {
        if (this.list != null)
        {
            for (int i = this.list.elementsCount; i < this.list.buffer.length; i++) {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> empty() == this.list.buffer[i]);
                /*! #end !*/
            }
        }
    }

    /* */
    @Test
    public void testAdd()
    {
        this.list.add(this.key1, this.key2);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2);
    }

    /* */
    @Test
    public void testAddTwoArgs()
    {
        this.list.add(this.key1, this.key2);
        this.list.add(this.key3, this.key4);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 3, 4);
    }

    /* */
    @Test
    public void testAddArray()
    {
        this.list.add(asArray(0, 1, 2, 3), 1, 2);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2);
    }

    /* */
    @Test
    public void testAddAll()
    {
        final KTypeLinkedList<KType> list2 = KTypeLinkedList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.list.addAll(list2);
        this.list.addAll(list2);

        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2, 0, 1, 2);
    }

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAddAll_subclass()
    {
        class A
        {
        }

        class B extends A
        {
        }

        final KTypeLinkedList<B> list2 = new KTypeLinkedList<B>();
        list2.add(new B());

        final KTypeLinkedList<A> list3 = new KTypeLinkedList<A>();
        list3.add(new B());
        list3.add(new A());
        list3.addAll(list2);
        Assert.assertEquals(3, list3.size());
    }

    /*! #end !*/

    /* */
    @Test
    public void testGotoIndex()
    {
        //fill with distinct values
        final int COUNT = (int) 1e4;

        for (int i = 0; i < COUNT; i++)
        {
            this.list.add(cast(i));
        }

        //check that we reach the good element, by index
        for (int i = 0; i < COUNT; i++)
        {
            Assert.assertEquals(castType(cast(i)), castType(this.list.buffer[this.list.gotoIndex(i)]));
        }
    }

    /* */
    @Test
    public void testEnsureCapacity()
    {
        this.list.ensureCapacity(100);
        Assert.assertTrue(this.list.buffer.length >= 100);

        this.list.ensureCapacity(1000);
        this.list.ensureCapacity(1000);
        Assert.assertTrue(this.list.buffer.length >= 1000);
    }

    /* */
    @Test
    public void testDescendingIterator()
    {
        this.list.addLast(this.sequence);

        int index = this.sequence.size() - 1;
        for (final Iterator<KTypeCursor<KType>> i = this.list.descendingIterator(); i.hasNext();)
        {
            final KTypeCursor<KType> cursor = i.next();
            TestUtils.assertEquals2(this.sequence.buffer[index], cursor.value);
            TestUtils.assertEquals2(index, cursor.index);
            index--;
        }
        Assert.assertEquals(-1, index);

        this.list.clear();
        Assert.assertFalse(this.list.descendingIterator().hasNext());
    }

    /* */
    @Test
    public void testDescendingForEachWithProcedure()
    {
        this.list.addLast(this.sequence);

        final IntHolder count = new IntHolder();
        this.list.descendingForEach(new KTypeProcedure<KType>() {
            int index = KTypeLinkedListTest.this.sequence.size();

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(KTypeLinkedListTest.this.sequence.buffer[--this.index], v);
                count.value++;
            }
        });
        Assert.assertEquals(count.value, this.list.size());
    }

    /* */
    @Test
    public void testDescendingForEachWithPredicate()
    {
        this.list.add(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.list.descendingForEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = KTypeLinkedListTest.this.list.size() - 1;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeLinkedListTest.this.list.get(this.index));
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
        this.list.add(asArray(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

        final int lastValue = this.list.descendingForEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = KTypeLinkedListTest.this.list.size() - 1;

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(v, KTypeLinkedListTest.this.list.get(this.index));
                this.value = castType(v);

                this.index--;

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 1);
    }

    @Test
    public void testDescendingPooledIteratorFullIteratorLoop()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeLinkedList<KType> testContainer = createLinkedListWithOrderedData(TEST_SIZE);

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

            final KTypeLinkedList<KType>.DescendingValueIterator loopIterator = testContainer.descendingIterator();

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

        final KTypeLinkedList<KType> testContainer = createLinkedListWithOrderedData(TEST_SIZE);

        final int startingPoolDescendingSize = testContainer.descendingValueIteratorPool.size();

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            // Descending iteration
            final int initialPoolSize = testContainer.descendingValueIteratorPool.size();

            final KTypeLinkedList<KType>.DescendingValueIterator loopIterator = testContainer.descendingIterator();

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

    /**
     * The beast is slow, don't do too much
     */
    @Repeat(iterations = 20)
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

        final int TEST_SIZE = (int) 1e3;

        //get a new seed for the current iteration
        final long currentSeed = RandomizedTest.randomLong();

        final int upperRange = RandomizedTest.randomInt(TEST_SIZE);
        final int lowerRange = RandomizedTest.randomInt(upperRange);

        //A) Sort an array of random values of primitive types

        //A-1) full sort
        KTypeLinkedList<KType> primitiveList = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        KTypeLinkedList<KType> primitiveListOriginal = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort();
        assertOrder(primitiveListOriginal, primitiveList, 0, primitiveListOriginal.size());
        //A-2) Partial sort
        primitiveList = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        primitiveListOriginal = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort(lowerRange, upperRange);
        assertOrder(primitiveListOriginal, primitiveList, lowerRange, upperRange);

        //B) Sort with Comparator
        //B-1) Full sort
        KTypeLinkedList<KType> comparatorList = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        KTypeLinkedList<KType> comparatorListOriginal = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(comp);
        assertOrder(comparatorListOriginal, comparatorList, 0, comparatorListOriginal.size());
        //B-2) Partial sort
        comparatorList = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        comparatorListOriginal = createLinkedListWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(lowerRange, upperRange, comp);
        assertOrder(comparatorListOriginal, comparatorList, lowerRange, upperRange);
    }

    private KTypeLinkedList<KType> createLinkedListWithOrderedData(final int size)
    {
        final KTypeLinkedList<KType> newArray = KTypeLinkedList.newInstance();

        for (int i = 0; i < size; i++)
        {
            newArray.addLast(cast(i));
        }

        return newArray;
    }

    private KTypeLinkedList<KType> createLinkedListWithRandomData(final int size, final long randomSeed)
    {
        final Random prng = new Random(randomSeed);

        final KTypeLinkedList<KType> newArray = KTypeLinkedList.newInstance();

        for (int i = 0; i < size; i++)
        {
            newArray.add(cast(prng.nextInt()));
        }

        return newArray;
    }

    ////////////////////////////////// Dequeue-like tests //////////////////////////////////////////////////////////////////

    /* */
    @Test
    public void testAddFirst()
    {
        this.list.addFirst(this.k1);
        this.list.addFirst(this.k2);
        this.list.addFirst(this.k3);
        this.list.addFirst(this.k7);
        this.list.addFirst(this.k1);
        this.list.addFirst(this.k4);
        this.list.addFirst(this.k5);
        TestUtils.assertListEquals(this.list.toArray(), 5, 4, 1, 7, 3, 2, 1);
        Assert.assertEquals(7, this.list.size());
    }

    /* */
    @Test
    public void testAddLast()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        this.list.addLast(this.k3);
        this.list.addLast(this.k7);
        this.list.addLast(this.k1);
        this.list.addLast(this.k4);
        this.list.addLast(this.k5);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 3, 7, 1, 4, 5);
        Assert.assertEquals(7, this.list.size());
    }

    /* */
    @Test
    public void testAddWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++)
        {
            this.list.addFirst(Intrinsics.<KType> cast(this.sequence.buffer[i]));
        }

        TestUtils.assertListEquals(TestUtils.reverse(this.sequence.toArray()), this.list.toArray());
    }

    /* */
    @Test
    public void testAddLastWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++) {

            this.list.addLast(Intrinsics.<KType> cast(this.sequence.buffer[i]));
        }

        TestUtils.assertListEquals(this.sequence.toArray(), this.list.toArray());
    }

    /* */
    @Test
    public void testAddAllFirst()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.list.addFirst(list2);
        TestUtils.assertListEquals(this.list.toArray(), 2, 1, 0);
        this.list.addFirst(list2);
        TestUtils.assertListEquals(this.list.toArray(), 2, 1, 0, 2, 1, 0);

        this.list.clear();
        final KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        this.list.addFirst(deque2);
        TestUtils.assertListEquals(this.list.toArray(), 2, 1, 0);
    }

    /* */
    @Test
    public void testAddAllLast()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.list.addLast(list2);
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2);
        this.list.addLast(list2);
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2, 0, 1, 2);

        this.list.clear();
        final KTypeArrayDeque<KType> deque2 = new KTypeArrayDeque<KType>();
        deque2.addLast(asArray(0, 1, 2));
        this.list.addLast(deque2);
        TestUtils.assertListEquals(this.list.toArray(), 0, 1, 2);
    }

    /* */
    @Test
    public void testRemoveFirst()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        this.list.addLast(this.k3);

        this.list.removeFirst();
        TestUtils.assertListEquals(this.list.toArray(), 2, 3);
        Assert.assertEquals(2, this.list.size());

        this.list.addFirst(this.k4);
        TestUtils.assertListEquals(this.list.toArray(), 4, 2, 3);
        Assert.assertEquals(3, this.list.size());

        this.list.removeFirst();
        this.list.removeFirst();
        this.list.removeFirst();
        Assert.assertEquals(0, this.list.toArray().length);
        Assert.assertEquals(0, this.list.size());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveFirstEmpty()
    {
        this.list.removeFirst();
    }

    /* */
    @Test
    public void testRemoveLast()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        this.list.addLast(this.k3);

        this.list.removeLast();
        TestUtils.assertListEquals(this.list.toArray(), 1, 2);
        Assert.assertEquals(2, this.list.size());

        this.list.addLast(this.k4);
        TestUtils.assertListEquals(this.list.toArray(), 1, 2, 4);
        Assert.assertEquals(3, this.list.size());

        this.list.removeLast();
        this.list.removeLast();
        this.list.removeLast();
        Assert.assertEquals(0, this.list.toArray().length);
        Assert.assertEquals(0, this.list.size());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testRemoveLastEmpty()
    {
        this.list.removeLast();
    }

    /* */
    @Test
    public void testGetFirst()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        TestUtils.assertEquals2(this.k1, this.list.getFirst());
        this.list.addFirst(this.k3);
        TestUtils.assertEquals2(this.k3, this.list.getFirst());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetFirstEmpty()
    {
        this.list.getFirst();
    }

    /* */
    @Test
    public void testGetLast()
    {
        this.list.addLast(this.k1);
        this.list.addLast(this.k2);
        TestUtils.assertEquals2(this.k2, this.list.getLast());
        this.list.addLast(this.k3);
        TestUtils.assertEquals2(this.k3, this.list.getLast());
    }

    /* */
    @Test(expected = AssertionError.class)
    public void testGetLastEmpty()
    {
        this.list.getLast();
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
            this.list.addLast(cast(i % modulo));
            this.sequence.add(cast(i % modulo));
        }

        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        final Random rnd = new Random(0xdeadbeef);
        for (int i = 0; i < 500; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));
            Assert.assertEquals(
                    this.list.removeFirst(k) >= 0,
                    this.sequence.removeFirst(k) >= 0);
        }

        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        Assert.assertTrue(0 > this.list.removeFirst(cast(modulo + 1)));
        this.list.addLast(cast(modulo + 1));
        Assert.assertTrue(0 <= this.list.removeFirst(cast(modulo + 1)));
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
            this.list.addLast(cast(i % modulo));
            this.sequence.add(cast(i % modulo));
        }

        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        final Random rnd = new Random(0x11223344);

        for (int i = 0; i < 500; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));
            Assert.assertEquals(
                    this.list.removeLast(k) >= 0,
                    this.sequence.removeLast(k) >= 0);
        }

        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        int removedIndex = this.list.removeLast(cast(modulo + 1));
        Assert.assertTrue("removeLastOccurrence(cast(modulo + 1) = " + removedIndex, removedIndex < 0);

        this.list.addFirst(cast(modulo + 1));

        removedIndex = this.list.removeLast(cast(modulo + 1));
        Assert.assertTrue("removeLastOccurrence(cast(modulo + 1) = " + removedIndex, removedIndex == 0);
    }

    ///////////////////////////////// iteration special methods ////////////////////////////////////

    /* */
    @Test
    public void testIterationHeadTail()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.ValueIterator it = this.list.iterator();

        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(0, castType(it.getNext().value));
        Assert.assertEquals(0, it.getNext().index);

        //Try to move backwards, we stay in head
        it.gotoPrevious();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(0, castType(it.getNext().value));
        Assert.assertEquals(0, it.getNext().index);

        //iteration 0
        it.gotoNext();
        Assert.assertEquals(0, castType(it.cursor.value));
        Assert.assertEquals(0, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertTrue(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(11, castType(it.getNext().value));
        Assert.assertEquals(1, it.getNext().index);

        //iteration 1
        it.gotoNext();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious().value));
        Assert.assertEquals(0, it.getPrevious().index);
        Assert.assertEquals(22, castType(it.getNext().value));
        Assert.assertEquals(2, it.getNext().index);

        //iteration 2
        it.gotoNext();
        Assert.assertEquals(22, castType(it.cursor.value));
        Assert.assertEquals(2, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(11, castType(it.getPrevious().value));
        Assert.assertEquals(1, it.getPrevious().index);
        Assert.assertEquals(33, castType(it.getNext().value));
        Assert.assertEquals(3, it.getNext().index);

        //iteration 3
        it.gotoNext();
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(3, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(22, castType(it.getPrevious().value));
        Assert.assertEquals(2, it.getPrevious().index);
        Assert.assertEquals(44, castType(it.getNext().value));
        Assert.assertEquals(4, it.getNext().index);

        //iteration 4
        it.gotoNext();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(33, castType(it.getPrevious().value));
        Assert.assertEquals(3, it.getPrevious().index);
        Assert.assertEquals(55, castType(it.getNext().value));
        Assert.assertEquals(5, it.getNext().index);

        //iteration 5
        it.gotoNext();
        Assert.assertEquals(55, castType(it.cursor.value));
        Assert.assertEquals(5, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertTrue(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(44, castType(it.getPrevious().value));
        Assert.assertEquals(4, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //iteration 6
        it.gotoNext();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious().value));
        Assert.assertEquals(5, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //iteration 7 : we are already at tail, we don't move further
        it.gotoNext();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious().value));
        Assert.assertEquals(5, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //Goes back to head
        it.gotoHead();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(0, castType(it.getNext().value));
        Assert.assertEquals(0, it.getNext().index);

        //Goes again to tail:
        it.gotoTail();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious().value));
        Assert.assertEquals(5, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

    }

    /* */
    @Test
    public void testIterationHeadTailReversed()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        //this is a reversed iteration
        final KTypeLinkedList<KType>.DescendingValueIterator it = this.list.descendingIterator();

        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(55, castType(it.getNext().value));
        Assert.assertEquals(5, it.getNext().index);

        //Try to move backwards, we stay in head
        it.gotoPrevious();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(55, castType(it.getNext().value));
        Assert.assertEquals(5, it.getNext().index);

        //iteration 0
        it.gotoNext();
        Assert.assertEquals(55, castType(it.cursor.value));
        Assert.assertEquals(5, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertTrue(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(44, castType(it.getNext().value));
        Assert.assertEquals(4, it.getNext().index);

        //iteration 1
        it.gotoNext();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious().value));
        Assert.assertEquals(5, it.getPrevious().index);
        Assert.assertEquals(33, castType(it.getNext().value));
        Assert.assertEquals(3, it.getNext().index);

        //iteration 2
        it.gotoNext();
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(3, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(44, castType(it.getPrevious().value));
        Assert.assertEquals(4, it.getPrevious().index);
        Assert.assertEquals(22, castType(it.getNext().value));
        Assert.assertEquals(2, it.getNext().index);

        //iteration 3
        it.gotoNext();
        Assert.assertEquals(22, castType(it.cursor.value));
        Assert.assertEquals(2, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(33, castType(it.getPrevious().value));
        Assert.assertEquals(3, it.getPrevious().index);
        Assert.assertEquals(11, castType(it.getNext().value));
        Assert.assertEquals(1, it.getNext().index);

        //iteration 4
        it.gotoNext();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(22, castType(it.getPrevious().value));
        Assert.assertEquals(2, it.getPrevious().index);
        Assert.assertEquals(0, castType(it.getNext().value));
        Assert.assertEquals(0, it.getNext().index);

        //iteration 5
        it.gotoNext();
        Assert.assertEquals(0, castType(it.cursor.value));
        Assert.assertEquals(0, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertTrue(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain previous / next
        Assert.assertEquals(11, castType(it.getPrevious().value));
        Assert.assertEquals(1, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //iteration 6
        it.gotoNext();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious().value));
        Assert.assertEquals(0, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //iteration 7 : we are already at tail, we don't move further
        it.gotoNext();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious().value));
        Assert.assertEquals(0, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());

        //Goes back to head
        it.gotoHead();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertTrue(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertFalse(it.isTail());
        //obtain next
        Assert.assertEquals(null, it.getPrevious());
        Assert.assertEquals(55, castType(it.getNext().value));
        Assert.assertEquals(5, it.getNext().index);

        //Goes again to tail:
        it.gotoTail();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertFalse(it.isHead());
        Assert.assertFalse(it.isFirst());
        Assert.assertFalse(it.isLast());
        Assert.assertTrue(it.isTail());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious().value));
        Assert.assertEquals(0, it.getPrevious().index);
        Assert.assertEquals(null, it.getNext());
    }

    /* */
    @Test
    public void testIterationBackAndForth()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.ValueIterator it = this.list.iterator();

        //goes 3 steps ==> , 1 step <==
        it.gotoNext().gotoNext().gotoNext().gotoPrevious();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);

        //goes 2 steps ==> , 2 step <==
        it.gotoNext().gotoNext().gotoPrevious().gotoPrevious();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);

        //goes 3 steps <== , 5 step ==> (back is clamped at head, in fact)
        it.gotoPrevious().gotoPrevious().gotoPrevious();
        it.gotoNext().gotoNext().gotoNext().gotoNext().gotoNext();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);
    }

    /* */
    @Test
    public void testIterationBackAndForthReversed()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.DescendingValueIterator it = this.list.descendingIterator();

        //goes 3 steps ==> , 1 step <==
        it.gotoNext().gotoNext().gotoNext().gotoPrevious();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);

        //goes 2 steps ==> , 2 step <==
        it.gotoNext().gotoNext().gotoPrevious().gotoPrevious();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);

        //goes 3 steps <== , 5 step ==> (back is clamped at head, in fact)
        it.gotoPrevious().gotoPrevious().gotoPrevious();
        it.gotoNext().gotoNext().gotoNext().gotoNext().gotoNext();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);
    }

    /* */
    @Test
    public void testIterationWithInsertionRemoveSetDelete()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.ValueIterator it = this.list.iterator();

        //goes to 11
        it.gotoNext().gotoNext();
        //list.add(asArray(0, 11, 22, 33, 44, 55));
        it.insertAfter(cast(111));
        it.insertAfter(cast(100));
        it.insertAfter(cast(99));
        //==> list.add(asArray(0, 11, 99, 100, 111, 22, 33, 44, 55));
        TestUtils.assertListEquals(this.list.toArray(), 0, 11, 99, 100, 111, 22, 33, 44, 55);
        it.insertBefore(cast(3));
        it.insertBefore(cast(33));
        it.insertBefore(cast(20));
        //==> list.add(asArray(0, 3,33,20, 11, 99, 100, 111, 22, 33, 44, 55));
        TestUtils.assertListEquals(this.list.toArray(), 0, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55);
        it.gotoPrevious();
        Assert.assertEquals(20, castType(it.cursor.value));

        //insert at head
        it.gotoHead();
        it.insertAfter(cast(111));
        it.insertAfter(cast(112));
        // ==> list.add(asArray(112,111,0, 3,33,20, 11, 99, 100, 111, 22, 33, 44, 55));
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 0, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55);

        //insert at tail
        it.gotoTail();
        it.insertBefore(cast(7));
        it.insertBefore(cast(9));
        // ==> list.add(asArray(112,111,0, 3,33,20, 11, 99, 100, 111, 22, 33, 44, 55,7, 9));
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 0, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9);

        //////// remove / set / delete /////////////////////

        //rewind to first
        it.gotoHead().gotoNext();
        //set -88 at 0 value
        it.gotoNext().gotoNext();
        Assert.assertEquals(0, castType(it.set(cast(88))));
        // ==> list.add(asArray(112,111,88, 3,33,20, 11, 99, 100, 111, 22, 33, 44, 55,7, 9));
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9);

        //move forward by 2 elements : 33
        it.gotoNext().gotoNext();
        //remove next = 20
        KTypeCursor<KType> removed = it.removeNext();
        Assert.assertEquals(5, removed.index);
        Assert.assertEquals(20, castType(removed.value));
        // ==> list.add(asArray(112, 111,88, 3, 33, 11, 99, 100, 111, 22, 33, 44, 55,7, 9));
        //it still points to 33
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, /*20 */11, 99, 100, 111, 22, 33, 44, 55, 7, 9);
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);

        //move again of 3 : 100
        it.gotoNext().gotoNext().gotoNext();
        //remove the previous = 99
        removed = it.removePrevious();
        Assert.assertEquals(6, removed.index);
        Assert.assertEquals(99, castType(removed.value));

        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, /*20 */11, /*99 */100, 111, 22, 33, 44, 55, 7, 9);
        //the iterator still points to 100
        Assert.assertEquals(100, castType(it.cursor.value));
        Assert.assertEquals(6, it.cursor.index);

        //move again of 4 : 44
        it.gotoNext().gotoNext().gotoNext().gotoNext();
        //remove itself
        it.delete();
        //the iterator now points to 55
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, /*20 */11, /*99 */100, 111, 22, 33, /* 44 */55, 7, 9);
        Assert.assertEquals(55, castType(it.cursor.value));
        Assert.assertEquals(10, it.cursor.index);
    }

    /* */
    @Test
    public void testIterationLoopWithDelete()
    {
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        final int poolSize = this.list.valueIteratorPool.size();

        KTypeLinkedList<KType>.ValueIterator it = null;
        try
        {
            for (it = this.list.iterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                if (it.getNext() != null && castType(it.getNext().value) == 88 ||
                        it.getNext() != null && castType(it.getNext().value) == 99 ||
                        it.getNext() != null && castType(it.getNext().value) == 55)
                {

                    it.removeNext();
                }
            }

            TestUtils.assertListEquals(this.list.toArray(), 112, 111, 3, 33, 20, 11, 100, 111, 22, 33, 44, 7, 9);
            it.release();
            //empty all
            for (it = this.list.iterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
            it.release();
            //try to iterate an empty list
            for (it = this.list.iterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
        } finally
        {
            it.release();
        }

        Assert.assertEquals(poolSize, this.list.valueIteratorPool.size());

    }

    /* */
    @Test
    public void testIterationLoopWithDeleteReversed()
    {
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        final int poolSize = this.list.valueIteratorPool.size();

        KTypeLinkedList<KType>.DescendingValueIterator it = null;
        try
        {
            for (it = this.list.descendingIterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                if (it.getNext() != null && castType(it.getNext().value) == 88 ||
                        it.getNext() != null && castType(it.getNext().value) == 99 ||
                        it.getNext() != null && castType(it.getNext().value) == 55)
                {

                    it.removeNext();
                }
            }

            TestUtils.assertListEquals(this.list.toArray(), 112, 111, 3, 33, 20, 11, 100, 111, 22, 33, 44, 7, 9);
            it.release();
            //empty all
            for (it = this.list.descendingIterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
            it.release();
            //try to iterate an empty list
            for (it = this.list.descendingIterator().gotoNext(); !it.isTail(); it.gotoNext())
            {
                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
        } finally
        {
            it.release();
        }

        Assert.assertEquals(poolSize, this.list.valueIteratorPool.size());
    }

    /* */
    @Test
    public void testIterationWithInsertionRemoveSetDeleteReversed()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.DescendingValueIterator it = this.list.descendingIterator();

        //goes to 44
        it.gotoNext().gotoNext();
        //list.add(asArray(0, 11, 22, 33, 44, 55));
        it.insertAfter(cast(111));
        it.insertAfter(cast(100));
        it.insertAfter(cast(99));
        //==> list.add(asArray(0, 11, 22, 33, 111, 100, 99, 44, 55));
        TestUtils.assertListEquals(this.list.toArray(), 0, 11, 22, 33, 111, 100, 99, 44, 55);
        it.insertBefore(cast(3));
        it.insertBefore(cast(33));
        it.insertBefore(cast(20));
        //==> list.add(asArray(0, 11, 22, 33, 111, 100, 99, 44, 20, 33,3,55));
        TestUtils.assertListEquals(this.list.toArray(), 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3, 55);
        it.gotoPrevious();
        Assert.assertEquals(20, castType(it.cursor.value));

        //insert at head
        it.gotoHead();
        it.insertAfter(cast(111));
        it.insertAfter(cast(112));
        // ==> list.add(asArray(0, 11, 22, 33, 111, 100, 99, 44, 20, 33,3,55,111,112));
        TestUtils.assertListEquals(this.list.toArray(), 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3, 55, 111, 112);

        //insert at tail
        it.gotoTail();
        it.insertBefore(cast(7));
        it.insertBefore(cast(9));
        // ==> list.add(asArray(9,7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33,3,55,111,112));
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3, 55, 111, 112);

        //////// remove / set / delete /////////////////////

        //rewind to first
        it.gotoHead().gotoNext();
        //set 88 at 55 value
        it.gotoNext().gotoNext();
        Assert.assertEquals(55, castType(it.set(cast(88))));
        // ==> list.add(asArray( 9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33,3,-88,111,112));
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3, 88, 111, 112);

        //move forward by 2 elements : 33
        it.gotoNext().gotoNext();
        //remove next = 20
        KTypeCursor<KType> removed = it.removeNext();
        Assert.assertEquals(10, removed.index);
        Assert.assertEquals(20, castType(removed.value));
        // ==> list.add(asArray(9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3,88,111,112));
        //it still points to 33
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, 99, 44 /*20 */, 33, 3, 88, 111, 112);
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(10, it.cursor.index);

        //move again of 3 : 100
        it.gotoNext().gotoNext().gotoNext();
        //remove the previous = 99
        removed = it.removePrevious();
        Assert.assertEquals(8, removed.index);
        Assert.assertEquals(99, castType(removed.value));

        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, /*99*/44 /*20 */, 33, 3, 88, 111, 112);
        //the iterator still points to 100
        Assert.assertEquals(100, castType(it.cursor.value));
        Assert.assertEquals(7, it.cursor.index);

        //move again of 4 : 11
        it.gotoNext().gotoNext().gotoNext().gotoNext();
        //remove itself
        it.delete();
        //the iterator now points to 0
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0 /*11 */, 22, 33, 111, 100, /*99*/44 /*20 */, 33, 3, 88, 111, 112);
        Assert.assertEquals(0, castType(it.cursor.value));
        Assert.assertEquals(2, it.cursor.index);
    }

    ////////////////////////////////// END iteration special methods  ////////////////////////////////////

    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Test
    public void testAgainstArrayDeque()
    {
        final Random rnd = new Random();
        final int rounds = 10000;
        final int modulo = 100;

        final ArrayDeque<KType> ad = new ArrayDeque<KType>();
        for (int i = 0; i < rounds; i++)
        {
            final KType k = cast(rnd.nextInt(modulo));

            final int op = rnd.nextInt(8);
            if (op < 2)
            {
                this.list.addFirst(k);
                ad.addFirst(k);
            }
            else if (op < 4)
            {
                this.list.addLast(k);
                ad.addLast(k);
            }
            else if (op < 5 && ad.size() > 0)
            {
                this.list.removeLast();
                ad.removeLast();
            }
            else if (op < 6 && ad.size() > 0)
            {
                this.list.removeLast();
                ad.removeLast();
            }
            else if (op < 7)
            {
                Assert.assertEquals(
                        ad.removeFirstOccurrence(k),
                        this.list.removeFirst(k) >= 0);
            }
            else if (op < 8)
            {
                Assert.assertEquals(
                        ad.removeLastOccurrence(k),
                        this.list.removeLast(k) >= 0);
            }
            Assert.assertEquals(ad.size(), this.list.size());
        }

        Assert.assertArrayEquals(ad.toArray(), this.list.toArray());
    }

    /*! #end !*/

    /* */
    @Test
    public void testEquals()
    {
        final int modulo = 127;
        final int count = 15000;

        final KTypeLinkedList<KType> list2 = KTypeLinkedList.newInstance();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.list.addFirst(cast(i % modulo));
                list2.addFirst(cast(i % modulo));
            }
            else {
                this.list.addLast(cast(i % modulo));
                list2.addLast(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.list.toArray(), list2.toArray());

        //Both dequeue are indeed equal
        Assert.assertTrue(this.list.equals(list2));
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
                this.list.addFirst(cast(i % modulo));
                this.sequence.insert(0, cast(i % modulo));
            }
            else {
                this.list.addLast(cast(i % modulo));
                this.sequence.add(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.list.toArray(), this.sequence.toArray());

        //The array list and dequeue are indeed equal
        Assert.assertTrue(this.list.equals(this.sequence));
    }

    /* */
    @Test
    public void testDequeContainerEquals()
    {
        final int modulo = 127;
        final int count = 15000;

        final KTypeArrayDeque<KType> deque2 = KTypeArrayDeque.newInstance();

        for (int i = 0; i < count; i++)
        {
            //add in head an in queue indifferently
            if (i % 3 == 0) {
                this.list.addFirst(cast(i % modulo));
                deque2.addFirst(cast(i % modulo));
            }
            else {
                this.list.addLast(cast(i % modulo));
                deque2.addLast(cast(i % modulo));
            }
        }

        //both elements by elements are equal
        TestUtils.assertListEquals(this.list.toArray(), deque2.toArray());

        //Both Dequeues are indeed equal: explicitely cast it in KTypeDeque
        Assert.assertTrue(this.list.equals((KTypeDeque<KType>) deque2));
    }
}
