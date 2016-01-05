package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Tests common for all kinds of hash sets {@link KTypeDeque}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public abstract class AbstractKTypeDequeTest<KType> extends AbstractKTypeTest<KType>
{
    @BeforeClass
    public static void configure()
    {
        IteratorPool.configureInitialPoolSize(8);
    }

    /**
     * Customize this for concrete hash set creation
     * @param initialCapacity
     * @param loadFactor
     * @param strategy
     * @return
     */
    protected abstract KTypeDeque<KType> createNewInstance(final int initialCapacity);

    protected abstract void addFromArray(KTypeDeque<KType> testList, KType... keys);

    protected abstract void addFromContainer(KTypeDeque<KType> testList, KTypeContainer<KType> container);

    protected abstract KType[] getBuffer(KTypeDeque<KType> testList);

    protected abstract int getDescendingValuePoolSize(KTypeDeque<KType> testList);

    protected abstract int getDescendingValuePoolCapacity(KTypeDeque<KType> testList);

    /**
     * Per-test fresh initialized instance.
     */
    protected KTypeDeque<KType> deque;

    /**
     * Some sequence values for tests.
     */
    protected KTypeArrayList<KType> sequence;

    @Before
    public void initialize() {

        this.deque = createNewInstance();

        this.sequence = new KTypeArrayList<KType>();

        for (int i = 0; i < 10000; i++) {
            this.sequence.add(cast(i));
        }
    }

    protected KTypeDeque<KType> createNewInstance() {

        return createNewInstance(0);
    }

    /* */
    @Test
    public void testAddFirst()
    {
        this.deque.addFirst(this.k1);
        this.deque.addFirst(this.k2);
        this.deque.addFirst(this.k3);
        this.deque.addFirst(this.k7);
        this.deque.addFirst(this.k1);
        this.deque.addFirst(this.k4);
        this.deque.addFirst(this.k5);
        TestUtils.assertListEquals(this.deque.toArray(), 5, 4, 1, 7, 3, 2, 1);
        Assert.assertEquals(7, this.deque.size());
    }

    /* */
    @Test
    public void testAddLast()
    {
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k2);
        this.deque.addLast(this.k3);
        this.deque.addLast(this.k7);
        this.deque.addLast(this.k1);
        this.deque.addLast(this.k4);
        this.deque.addLast(this.k5);
        TestUtils.assertListEquals(this.deque.toArray(), 1, 2, 3, 7, 1, 4, 5);
        Assert.assertEquals(7, this.deque.size());
    }

    /* */
    @Test
    public void testAddFirstWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++)
        {
            this.deque.addFirst(this.sequence.get(i));
        }

        TestUtils.assertListEquals(TestUtils.reverse(this.sequence.toArray()), this.deque.toArray());
    }

    /* */
    @Test
    public void testAddLastWithGrowth()
    {
        for (int i = 0; i < this.sequence.size(); i++) {
            this.deque.addLast(this.sequence.get(i));
        }

        TestUtils.assertListEquals(this.sequence.toArray(), this.deque.toArray());
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
            final int intValue = rnd.nextInt(modulo);
            final KType k = cast(intValue);

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

        final KTypeDeque<KType> deque2 = createNewInstance();

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
    public void testDescendingIterable()
    {
        addFromContainer(this.deque, this.sequence);

        int index = this.sequence.size() - 1;
        for (final Iterator<KTypeCursor<KType>> i = this.deque.descendingIterator(); i.hasNext();)
        {
            final KTypeCursor<KType> cursor = i.next();
            TestUtils.assertEquals2(this.sequence.buffer[index], cursor.value);
            //general case: index in buffer matches index of cursor
            TestUtils.assertEquals2(getBuffer(this.deque)[cursor.index], cursor.value);
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
        addFromContainer(this.deque, this.sequence);

        final IntHolder count = new IntHolder();
        this.deque.descendingForEach(new KTypeProcedure<KType>() {
            int index = AbstractKTypeDequeTest.this.sequence.size();

            @Override
            public void apply(final KType v)
            {
                TestUtils.assertEquals2(AbstractKTypeDequeTest.this.sequence.buffer[--this.index], v);
                count.value++;
            }
        });
        Assert.assertEquals(count.value, this.deque.size());
    }

    /* */
    @Test
    public void testDescendingForEachWithPredicate()
    {
        addFromContainer(this.deque, this.sequence);

        final int lastValue = this.deque.descendingForEach(new KTypePredicate<KType>() {
            int value = 0;
            int index = AbstractKTypeDequeTest.this.sequence.size();

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(AbstractKTypeDequeTest.this.sequence.buffer[--this.index], v);
                this.value = castType(v);

                if (this.value == 9) {

                    return false;
                }

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 9);
    }

    /* */
    @Test
    public void testDescendingForEachWithPredicateAllwaysTrue()
    {
        addFromContainer(this.deque, this.sequence);

        final int lastValue = this.deque.descendingForEach(new KTypePredicate<KType>() {
            int value = Integer.MIN_VALUE;
            int index = AbstractKTypeDequeTest.this.sequence.size();

            @Override
            public boolean apply(final KType v)
            {
                TestUtils.assertEquals2(AbstractKTypeDequeTest.this.sequence.buffer[--this.index], v);
                this.value = castType(v);

                return true;
            }
        }).value;

        Assert.assertEquals(lastValue, 0);
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

    @Test
    public void testDescendingPooledIteratorFullIteratorLoop()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeDeque<KType> testContainer = createDequeWithOrderedData(TEST_SIZE);

        final long checksum = testContainer.forEach(new KTypeProcedure<KType>() {

            long count;

            @Override
            public void apply(final KType value)
            {
                this.count += castType(value);
            }
        }).count;

        long testValue = 0;

        final int startingPoolDescendingSize = getDescendingValuePoolSize(testContainer);

        for (int round = 0; round < TEST_ROUNDS; round++)
        {

            // Descending iterator loop
            final int initialPoolSize = getDescendingValuePoolSize(testContainer);

            final AbstractIterator<KTypeCursor<KType>> loopIterator = (AbstractIterator<KTypeCursor<KType>>) testContainer.descendingIterator();

            Assert.assertEquals(initialPoolSize - 1, getDescendingValuePoolSize(testContainer));

            testValue = 0;
            while (loopIterator.hasNext())
            {
                testValue += castType(loopIterator.next().value);
            } //end IteratorLoop

            //iterator is returned automatically to its pool, by normal iteration termination
            Assert.assertEquals(initialPoolSize, getDescendingValuePoolSize(testContainer));

            //checksum
            Assert.assertEquals(checksum, testValue);

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolDescendingSize, getDescendingValuePoolSize(testContainer));
    }

    @Test
    public void testDescendingPooledIteratorBrokenIteratorLoop()
    {
        final int TEST_SIZE = 5000;
        final long TEST_ROUNDS = 100;

        final KTypeDeque<KType> testContainer = createDequeWithOrderedData(TEST_SIZE);

        final int startingPoolDescendingSize = getDescendingValuePoolSize(testContainer);

        int count = 0;
        for (int round = 0; round < TEST_ROUNDS; round++)
        {
            // Descending iteration
            final int initialPoolSize = getDescendingValuePoolSize(testContainer);

            final AbstractIterator<KTypeCursor<KType>> loopIterator = (AbstractIterator<KTypeCursor<KType>>) testContainer.descendingIterator();

            Assert.assertEquals(initialPoolSize - 1, getDescendingValuePoolSize(testContainer));

            count = 0;
            long guard = 0;
            while (loopIterator.hasNext())
            {
                guard += castType(loopIterator.next().value);

                //brutally interrupt in the middle
                if (count > TEST_SIZE / 2)
                {
                    break;
                }
                count++;
            } //end IteratorLoop

            //iterator is NOT returned to its pool, due to the break.
            Assert.assertEquals(initialPoolSize - 1, getDescendingValuePoolSize(testContainer));

            //manual return to the pool
            loopIterator.release();

            //now the pool is restored
            Assert.assertEquals(initialPoolSize, getDescendingValuePoolSize(testContainer));

        } //end for rounds

        // pool initial size is untouched anyway
        Assert.assertEquals(startingPoolDescendingSize, getDescendingValuePoolSize(testContainer));
    }

    protected KTypeDeque<KType> createDequeWithOrderedData(final int size)
    {
        final KTypeDeque<KType> newArray = createNewInstance();

        for (int i = 0; i < size; i++)
        {
            newArray.addLast(cast(i));
        }

        return newArray;
    }

    protected KTypeDeque<KType> createDequeWithRandomData(final int size, final long randomSeed)
    {
        final Random prng = new Random(randomSeed);

        final KTypeDeque<KType> newDeque = createNewInstance();

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
