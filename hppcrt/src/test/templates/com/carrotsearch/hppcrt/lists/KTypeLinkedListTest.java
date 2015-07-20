package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit tests for specific {@link KTypeLinkedList}.
 */
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
            int count = 0;
            //check access by get()
            for (/*! #if ($TemplateOptions.KTypeGeneric) !*/final Object
                    /*! #else
            final KType
            #end !*/
                    val : this.list.toArray()) {

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                TestUtils.assertEquals2(val, (Object) this.list.get(count));
                /*! #else
                TestUtils.assertEquals2(val, this.list.get(count));
                #end !*/
                count++;
            }

            Assert.assertEquals(count, this.list.size());

            //check beyond validity range
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

    /**
     * The beast is slow, don't do too much
     */
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

        final int TEST_SIZE = (int) 20;
        final int NB_ITERATIONS = (int) 1e5;

        //get a new seed for the current iteration
        final long currentSeed = RandomizedTest.randomLong();

        for (int ii = 0; ii < NB_ITERATIONS; ii++) {

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
    }

    /* */
    @Test
    public void testEqualsVsArrayList()
    {
        this.list.add(this.key1, this.key2, this.key3, this.key4, this.key5);

        final KTypeArrayList<KType> other = KTypeArrayList.newInstance();

        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        other.add(this.key1);
        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        other.add(this.key2, this.key3);
        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        other.add(this.key4, this.key5);
        Assert.assertEquals(this.list, other);
        Assert.assertEquals(other, this.list);
        Assert.assertEquals(this.list.hashCode(), other.hashCode());

        //they are the same
        //modify index 2 original this.key3
        other.set(2, this.key4);
        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        //re-establish
        other.set(2, this.key3);
        Assert.assertEquals(this.list, other);
        Assert.assertEquals(other, this.list);
        Assert.assertEquals(this.list.hashCode(), other.hashCode());

        //modify
        other.insert(0, this.k8);
        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        this.list.insert(0, this.k8);
        Assert.assertEquals(this.list, other);
        Assert.assertEquals(other, this.list);
        Assert.assertEquals(this.list.hashCode(), other.hashCode());
    }

    /* */
    @Test
    public void testEqualsVsArrayDeque()
    {
        this.list.add(this.key1, this.key2, this.key3, this.key4, this.key5);

        final KTypeArrayDeque<KType> other = KTypeArrayDeque.newInstance();

        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        other.add(this.key1);
        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        other.addLast(this.key2, this.key3);
        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        other.addLast(this.key4, this.key5);
        Assert.assertEquals(this.list, other);
        Assert.assertEquals(other, this.list);
        Assert.assertEquals(this.list.hashCode(), other.hashCode());

        //they are the same
        //modify index 2 original this.key3
        other.set(2, this.key4);
        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        //re-establish
        other.set(2, this.key3);
        Assert.assertEquals(this.list, other);
        Assert.assertEquals(other, this.list);
        Assert.assertEquals(this.list.hashCode(), other.hashCode());

        //modify
        other.addFirst(this.k8);
        Assert.assertNotEquals(this.list, other);
        Assert.assertNotEquals(other, this.list);

        this.list.insert(0, this.k8);
        Assert.assertEquals(this.list, other);
        Assert.assertEquals(other, this.list);
        Assert.assertEquals(this.list.hashCode(), other.hashCode());
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

    ///////////////////////////////// iteration special methods ////////////////////////////////////

    /* */
    @Test
    public void testIterationHeadTail()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        final KTypeLinkedList<KType>.ValueIterator it = this.list.iterator();

        Assert.assertEquals(-1, it.cursor.index);

        Assert.assertTrue(it.hasAfter());
        Assert.assertFalse(it.hasBefore());

        boolean hasException = false;
        try {
            it.getPrevious();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //obtain next
        Assert.assertEquals(0, castType(it.getNext()));

        //Try to move backwards, we stay in head
        it.gotoPrevious();
        Assert.assertEquals(-1, it.cursor.index);

        Assert.assertTrue(it.hasAfter());
        Assert.assertFalse(it.hasBefore());

        hasException = false;
        try {
            it.getPrevious();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //obtain next

        Assert.assertEquals(0, castType(it.getNext()));

        //iteration 0
        it.gotoNext();
        Assert.assertEquals(0, castType(it.cursor.value));
        Assert.assertEquals(0, it.cursor.index);

        //obtain previous / next
        hasException = false;
        try {
            it.getPrevious();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        Assert.assertEquals(11, castType(it.getNext()));

        //iteration 1
        it.gotoNext();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious()));

        Assert.assertEquals(22, castType(it.getNext()));

        //iteration 2
        it.gotoNext();
        Assert.assertEquals(22, castType(it.cursor.value));
        Assert.assertEquals(2, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(11, castType(it.getPrevious()));

        Assert.assertEquals(33, castType(it.getNext()));

        //iteration 3
        it.gotoNext();
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(3, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(22, castType(it.getPrevious()));
        Assert.assertEquals(44, castType(it.getNext()));

        //iteration 4
        it.gotoNext();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(33, castType(it.getPrevious()));

        Assert.assertEquals(55, castType(it.getNext()));

        //iteration 5
        it.gotoNext();
        Assert.assertEquals(55, castType(it.cursor.value));
        Assert.assertEquals(5, it.cursor.index);

        Assert.assertFalse(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(44, castType(it.getPrevious()));

        hasException = false;
        try {
            it.getNext();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //iteration 6 : we are at tail
        it.gotoNext();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertFalse(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious()));

        hasException = false;
        try {
            it.getNext();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //iteration 7 : we are already at tail, we don't move further
        it.gotoNext();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertFalse(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious()));

        hasException = false;
        try {
            it.getNext();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //Goes back to head :
        it.gotoHead();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertFalse(it.hasBefore());

        //obtain previous /  next
        hasException = false;
        try {
            it.getPrevious();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        Assert.assertEquals(0, castType(it.getNext()));

        //Goes again to tail:
        it.gotoTail();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertFalse(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious()));
        hasException = false;
        try {
            it.getNext();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //clear
        this.list.clear();
        Assert.assertFalse(it.hasAfter());
        Assert.assertFalse(it.hasBefore());
    }

    /* */
    @Test
    public void testIterationHeadTailReversed()
    {
        this.list.add(asArray(0, 11, 22, 33, 44, 55));

        //this is a reversed iteration
        final KTypeLinkedList<KType>.DescendingValueIterator it = this.list.descendingIterator();

        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertFalse(it.hasBefore());
        //obtain next
        boolean hasException = false;
        try {
            it.getPrevious();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        Assert.assertEquals(55, castType(it.getNext()));

        //Try to move backwards, we stay in head
        it.gotoPrevious();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertFalse(it.hasBefore());
        //obtain next
        hasException = false;
        try {
            it.getPrevious();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        Assert.assertEquals(55, castType(it.getNext()));

        //iteration 0
        it.gotoNext();
        Assert.assertEquals(55, castType(it.cursor.value));
        Assert.assertEquals(5, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertFalse(it.hasBefore());
        //obtain previous / next
        hasException = false;
        try {
            it.getPrevious();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);
        Assert.assertEquals(44, castType(it.getNext()));

        //iteration 1
        it.gotoNext();
        Assert.assertEquals(44, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(55, castType(it.getPrevious()));

        Assert.assertEquals(33, castType(it.getNext()));

        //iteration 2
        it.gotoNext();
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(3, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(44, castType(it.getPrevious()));

        Assert.assertEquals(22, castType(it.getNext()));

        //iteration 3
        it.gotoNext();
        Assert.assertEquals(22, castType(it.cursor.value));
        Assert.assertEquals(2, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(33, castType(it.getPrevious()));

        Assert.assertEquals(11, castType(it.getNext()));

        //iteration 4
        it.gotoNext();
        Assert.assertEquals(11, castType(it.cursor.value));
        Assert.assertEquals(1, it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(22, castType(it.getPrevious()));

        Assert.assertEquals(0, castType(it.getNext()));
        //iteration 5 : no next element
        it.gotoNext();
        Assert.assertEquals(0, castType(it.cursor.value));
        Assert.assertEquals(0, it.cursor.index);
        Assert.assertFalse(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(11, castType(it.getPrevious()));

        hasException = false;
        try {
            it.getNext();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //iteration 6 : we are at tail
        it.gotoNext();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertFalse(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious()));

        hasException = false;
        try {
            it.getNext();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //iteration 7 : we are already at tail, we don't move further
        it.gotoNext();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertFalse(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious()));

        hasException = false;
        try {
            it.getNext();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //Goes back to head
        it.gotoHead();
        Assert.assertEquals(this.list.size(), it.cursor.index);
        Assert.assertTrue(it.hasAfter());
        Assert.assertFalse(it.hasBefore());
        //obtain previous
        hasException = false;
        try {
            it.getPrevious();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        Assert.assertEquals(55, castType(it.getNext()));

        //Goes again to tail:
        it.gotoTail();
        Assert.assertEquals(-1, it.cursor.index);
        Assert.assertFalse(it.hasAfter());
        Assert.assertTrue(it.hasBefore());
        //obtain previous / next
        Assert.assertEquals(0, castType(it.getPrevious()));
        hasException = false;
        try {
            it.getNext();
        } catch (final NoSuchElementException e) {
            hasException = true;
        }
        Assert.assertTrue(hasException);

        //clear
        this.list.clear();
        Assert.assertFalse(it.hasAfter());
        Assert.assertFalse(it.hasBefore());
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
        //set 88 at 0 value
        it.gotoNext().gotoNext();
        Assert.assertEquals(0, castType(it.cursor.value));
        it.set(cast(88));
        Assert.assertEquals(88, castType(it.cursor.value));
        // ==> list.add(asArray(112,111,88, 3,33,20, 11, 99, 100, 111, 22, 33, 44, 55,7, 9));
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9);

        //move forward by 2 elements : 33
        it.gotoNext().gotoNext();
        //remove next = 20
        KType removed = it.removeNext();

        Assert.assertEquals(20, castType(removed));
        // ==> list.add(asArray(112, 111,88, 3, 33, 11, 99, 100, 111, 22, 33, 44, 55,7, 9));
        //it still points to 33
        TestUtils.assertListEquals(this.list.toArray(), 112, 111, 88, 3, 33, /*20 */11, 99, 100, 111, 22, 33, 44, 55, 7, 9);
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(4, it.cursor.index);

        //move again of 3 : 100
        it.gotoNext().gotoNext().gotoNext();
        //remove the previous = 99
        removed = it.removePrevious();

        Assert.assertEquals(99, castType(removed));

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
    public void testIterationWhileLoop()
    {
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        final ArrayList<Integer> iterationResult = new ArrayList<Integer>();

        //A) Head to tail iteration
        int poolSize = this.list.valueIteratorPool.size();

        KTypeLinkedList<KType>.ValueIterator it = null;

        it = this.list.iterator();

        while (it.hasAfter())
        {
            it = it.gotoNext();

            iterationResult.add(castType(it.cursor.value));
        }

        compareLists(this.list, iterationResult);
        Assert.assertEquals(poolSize - 1, this.list.valueIteratorPool.size());
        it.release();
        Assert.assertEquals(poolSize, this.list.valueIteratorPool.size());

        //A-2) Normal iteration, in reverse.
        iterationResult.clear();
        poolSize = this.list.valueIteratorPool.size();

        it = null;

        it = this.list.iterator().gotoTail();

        while (it.hasBefore())
        {
            it = it.gotoPrevious();

            iterationResult.add(castType(it.cursor.value));
        }

        //it is like a reversed iteration
        Collections.reverse(iterationResult);

        compareLists(this.list, iterationResult);

        Assert.assertEquals(poolSize - 1, this.list.valueIteratorPool.size());
        it.release();
        Assert.assertEquals(poolSize, this.list.valueIteratorPool.size());

        //B) descending iteration
        iterationResult.clear();
        int descendingPoolSize = this.list.descendingValueIteratorPool.size();

        KTypeLinkedList<KType>.DescendingValueIterator descIt = null;

        descIt = this.list.descendingIterator();

        while (descIt.hasAfter())
        {
            descIt.gotoNext();

            iterationResult.add(castType(descIt.cursor.value));
        }

        Collections.reverse(iterationResult);

        compareLists(this.list, iterationResult);

        Assert.assertEquals(descendingPoolSize - 1, this.list.descendingValueIteratorPool.size());
        descIt.release();
        Assert.assertEquals(descendingPoolSize, this.list.descendingValueIteratorPool.size());

        //B) descending iteration, in reverse
        iterationResult.clear();
        descendingPoolSize = this.list.descendingValueIteratorPool.size();

        descIt = null;

        descIt = this.list.descendingIterator().gotoTail();

        while (descIt.hasBefore())
        {
            descIt.gotoPrevious();

            iterationResult.add(castType(descIt.cursor.value));
        }

        //this is the normal order
        compareLists(this.list, iterationResult);

        Assert.assertEquals(descendingPoolSize - 1, this.list.descendingValueIteratorPool.size());
        descIt.release();
        Assert.assertEquals(descendingPoolSize, this.list.descendingValueIteratorPool.size());
    }

    /* */
    @Test
    public void testSimpleDeleteIterationLoop()
    {
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        final int poolSize = this.list.valueIteratorPool.size();

        //
        KTypeLinkedList<KType>.ValueIterator it = this.list.iterator();
        try
        {
            while (it.hasAfter())
            {
                it.removeNext();
            }

            Assert.assertEquals(0, this.list.size());

            //try to iterate an empty list
            while (it.hasAfter())
            {
                Assert.fail();
                it.removeNext();
            }

            Assert.assertEquals(0, this.list.size());

        } finally
        {
            it.release();
        }

        Assert.assertEquals(poolSize, this.list.valueIteratorPool.size());

        //Delete back to front
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        //
        it = this.list.iterator().gotoTail();
        try
        {
            while (it.hasBefore())
            {
                it.removePrevious();
            }

            Assert.assertEquals(0, this.list.size());

            //try to iterate an empty list
            while (it.hasBefore())
            {
                Assert.fail();
                it.removePrevious();
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
    public void testIterationLoopWithDelete()
    {
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        final int poolSize = this.list.valueIteratorPool.size();

        KTypeLinkedList<KType>.ValueIterator it = null;
        try
        {
            for (it = this.list.iterator(); it.hasAfter();)
            {
                it.gotoNext();

                if (it.hasAfter() && (castType(it.getNext()) == 88 ||
                        castType(it.getNext()) == 99 ||
                        castType(it.getNext()) == 55))
                {

                    it.removeNext();
                }
            }

            TestUtils.assertListEquals(this.list.toArray(), 112, 111, 3, 33, 20, 11, 100, 111, 22, 33, 44, 7, 9);
            it.release();
            //empty all

            for (it = this.list.iterator(); it.hasAfter();)
            {
                it.gotoNext();
                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
            it.release();
            //try to iterate an empty list
            for (it = this.list.iterator(); it.hasAfter();)
            {
                //we must never enter this loop
                Assert.fail();

                it.gotoNext();
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
    public void testSimpleDeleteIterationLoopReversed()
    {
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        final int poolSize = this.list.descendingValueIteratorPool.size();

        KTypeLinkedList<KType>.DescendingValueIterator it = this.list.descendingIterator();
        try
        {
            while (it.hasAfter())
            {
                it.removeNext();
            }

            Assert.assertEquals(0, this.list.size());

            //try to iterate an empty list
            while (it.hasAfter())
            {
                Assert.fail();
                it.removeNext();
            }

            Assert.assertEquals(0, this.list.size());

        } finally
        {
            it.release();
        }

        Assert.assertEquals(poolSize, this.list.descendingValueIteratorPool.size());

        //Delete back to front
        this.list.add(asArray(112, 111, 88, 3, 33, 20, 11, 99, 100, 111, 22, 33, 44, 55, 7, 9));

        //
        it = this.list.descendingIterator().gotoTail();
        try
        {
            while (it.hasBefore())
            {
                it.removePrevious();
            }

            Assert.assertEquals(0, this.list.size());

            //try to iterate an empty list
            while (it.hasBefore())
            {
                Assert.fail();
                it.removePrevious();
            }

            Assert.assertEquals(0, this.list.size());

        } finally
        {
            it.release();
        }

        Assert.assertEquals(poolSize, this.list.descendingValueIteratorPool.size());
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
            for (it = this.list.descendingIterator(); it.hasAfter();)
            {
                it.gotoNext();

                if (it.hasAfter() && (castType(it.getNext()) == 88 ||
                        castType(it.getNext()) == 99 ||
                        castType(it.getNext()) == 55))
                {
                    it.removeNext();
                }
            }

            TestUtils.assertListEquals(this.list.toArray(), 112, 111, 3, 33, 20, 11, 100, 111, 22, 33, 44, 7, 9);
            it.release();
            //empty all
            for (it = this.list.descendingIterator(); it.hasAfter();)
            {
                it.gotoNext();

                it.delete();
                //the for advanced itself, so go back one position
                it.gotoPrevious();
            }

            Assert.assertEquals(0, this.list.size());
            it.release();
            //try to iterate an empty list
            for (it = this.list.descendingIterator(); it.hasAfter();)
            {
                it.gotoNext();
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
        Assert.assertEquals(55, castType(it.cursor.value));
        it.set(cast(88));
        Assert.assertEquals(88, castType(it.cursor.value));
        // ==> list.add(asArray( 9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33,3,-88,111,112));
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3, 88, 111, 112);

        //move forward by 2 elements : 33
        it.gotoNext().gotoNext();
        //remove next = 20
        KType removed = it.removeNext();

        Assert.assertEquals(20, castType(removed));
        // ==> list.add(asArray(9, 7, 0, 11, 22, 33, 111, 100, 99, 44, 20, 33, 3,88,111,112));
        //it still points to 33
        TestUtils.assertListEquals(this.list.toArray(), 9, 7, 0, 11, 22, 33, 111, 100, 99, 44 /*20 */, 33, 3, 88, 111, 112);
        Assert.assertEquals(33, castType(it.cursor.value));
        Assert.assertEquals(10, it.cursor.index);

        //move again of 3 : 100
        it.gotoNext().gotoNext().gotoNext();
        //remove the previous = 99
        removed = it.removePrevious();

        Assert.assertEquals(99, castType(removed));

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

    private void compareLists(final KTypeIndexedContainer<KType> hppcList, final List<Integer> expectedValues) {

        Assert.assertEquals(expectedValues.size(), hppcList.size());

        for (int ii = 0; ii < expectedValues.size(); ii++) {

            Assert.assertEquals("At index = " + ii + ", ", expectedValues.get(ii).intValue(), castType(hppcList.get(ii)));
        }
    }
}
