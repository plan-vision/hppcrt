package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/**
 * Unit tests for {@link KTypeArrayList}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayListTest<KType> extends AbstractKTypeIndexedContainerTest<KType>
{

    @Override
    protected KTypeIndexedContainer<KType> createNewInstance(final int initialCapacity) {

        return new KTypeArrayList<KType>(initialCapacity);
    }

    @Override
    protected KType[] getBuffer(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayList<KType> concreteClass = (KTypeArrayList<KType>) (testList);
        return Intrinsics.<KType[]> cast(concreteClass.buffer);
    }

    @Override
    protected KTypeIndexedContainer<KType> getClone(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayList<KType> concreteClass = (KTypeArrayList<KType>) (testList);
        return concreteClass.clone();
    }

    @Override
    protected KTypeIndexedContainer<KType> getFrom(final KTypeContainer<KType> container) {

        return KTypeArrayList.from(container);
    }

    @Override
    protected KTypeIndexedContainer<KType> getFrom(final KType... elements) {

        return KTypeArrayList.from(elements);
    }

    @Override
    protected KTypeIndexedContainer<KType> getFromArray(final KType[] keys) {

        return KTypeArrayList.from(keys);
    }

    @Override
    protected void addFromArray(final KTypeIndexedContainer<KType> testList, final KType... keys) {

        final KTypeArrayList<KType> concreteClass = (KTypeArrayList<KType>) (testList);
        concreteClass.add(keys);
    }

    @Override
    protected KTypeIndexedContainer<KType> getCopyConstructor(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayList<KType> concreteClass = (KTypeArrayList<KType>) (testList);
        return new KTypeArrayList<KType>(concreteClass);
    }

    @Override
    int getValuePoolSize(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayList<KType> concreteClass = (KTypeArrayList<KType>) (testList);
        return concreteClass.valueIteratorPool.size();
    }

    @Override
    int getValuePoolCapacity(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayList<KType> concreteClass = (KTypeArrayList<KType>) (testList);
        return concreteClass.valueIteratorPool.capacity();
    }

    //////////////////////////////////////
    /// Implementation-specific tests
    /////////////////////////////////////
    private KTypeArrayList<KType> arrayList = new KTypeArrayList<KType>();

    /* */

    @Test
    public void testAddTwoArgs()
    {
        this.arrayList.add(this.key1, this.key2);
        this.arrayList.add(this.key3, this.key4);
        TestUtils.assertListEquals(this.arrayList.toArray(), 1, 2, 3, 4);
    }

    /* */

    @Test
    public void testAddArray()
    {
        this.arrayList.add(asArray(0, 1, 2, 3), 1, 2);
        TestUtils.assertListEquals(this.arrayList.toArray(), 1, 2);
    }

    /* */

    @Test
    public void testAddAll()
    {
        final KTypeArrayList<KType> list2 = KTypeArrayList.newInstance();
        list2.add(asArray(0, 1, 2));

        this.arrayList.addAll(list2);
        this.arrayList.addAll(list2);

        TestUtils.assertListEquals(this.arrayList.toArray(), 0, 1, 2, 0, 1, 2);
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

        final KTypeArrayList<B> list2 = new KTypeArrayList<B>();
        list2.add(new B());

        final KTypeArrayList<A> list3 = new KTypeArrayList<A>();
        list3.add(new B());
        list3.add(new A());
        list3.addAll(list2);
        Assert.assertEquals(3, list3.size());
    }

    /*! #end !*/

    /* */

    @Test
    public void testEnsureCapacity()
    {
        this.arrayList.ensureCapacity(100);
        Assert.assertTrue(this.arrayList.buffer.length >= 100);

        this.arrayList.ensureCapacity(1000);
        this.arrayList.ensureCapacity(1000);
        Assert.assertTrue(this.arrayList.buffer.length >= 1000);
    }

    @Test
    public void testResizeAndCleanBuffer()
    {
        this.arrayList.ensureCapacity(20);
        Arrays.fill(this.arrayList.buffer, this.k1);

        this.arrayList.resize(10);
        Assert.assertEquals(10, this.arrayList.size());

        for (int i = 0; i < this.arrayList.size(); i++) {

            TestUtils.assertEquals2(Intrinsics.<KType> empty(), this.arrayList.get(i));

        }

        Arrays.fill(this.arrayList.buffer, Intrinsics.<KType> empty());

        for (int i = 5; i < this.arrayList.size(); i++) {
            this.arrayList.set(i, this.k1);
        }

        this.arrayList.resize(5);
        Assert.assertEquals(5, this.arrayList.size());

        for (int i = this.arrayList.size(); i < this.arrayList.buffer.length; i++) {
            //only objects get cleared for GC sake.
            /*! #if ($TemplateOptions.KTypeGeneric) !*/
            TestUtils.assertEquals2(Intrinsics.<KType> empty(), this.arrayList.buffer[i]);
            /*! #end !*/
        }

    }

    /* */

    @Test
    public void testTrimToSize()
    {
        this.arrayList.add(asArray(1, 2));
        this.arrayList.trimToSize();
        Assert.assertEquals(2, this.arrayList.buffer.length);
    }

    /* */

    @Test
    public void testRelease()
    {
        this.arrayList.add(asArray(1, 2));
        this.arrayList.release();
        Assert.assertEquals(0, this.arrayList.size());
        this.arrayList.add(asArray(1, 2));
        Assert.assertEquals(2, this.arrayList.size());
    }

    /* */
    @Test
    public void testGrowth()
    {
        final int maxGrowth = 10;
        final int count = 500;

        this.arrayList = new KTypeArrayList<KType>(0,
                new BoundedProportionalArraySizingStrategy(5, maxGrowth, 2));

        for (int i = 0; i < count; i++) {
            this.arrayList.add(cast(i));
        }

        Assert.assertEquals(count, this.arrayList.size());

        for (int i = 0; i < count; i++) {
            TestUtils.assertEquals2(cast(i), this.arrayList.get(i));
        }

        Assert.assertTrue("Buffer size: 510 <= " + this.arrayList.buffer.length,
                this.arrayList.buffer.length <= count + maxGrowth);
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

        //A) Sort an array of random values of primitive types

        //A-1) full sort
        KTypeArrayList<KType> primitiveList = createArrayListWithRandomData(TEST_SIZE, currentSeed);
        KTypeArrayList<KType> primitiveListOriginal = createArrayListWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort();
        assertOrder(primitiveListOriginal, primitiveList, 0, primitiveList.size());
        //A-2) Partial sort
        primitiveList = createArrayListWithRandomData(TEST_SIZE, currentSeed);
        primitiveListOriginal = createArrayListWithRandomData(TEST_SIZE, currentSeed);
        primitiveList.sort(lowerRange, upperRange);
        assertOrder(primitiveListOriginal, primitiveList, lowerRange, upperRange);

        //B) Sort with Comparator
        //B-1) Full sort
        KTypeArrayList<KType> comparatorList = createArrayListWithRandomData(TEST_SIZE, currentSeed);
        KTypeArrayList<KType> comparatorListOriginal = createArrayListWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(comp);
        assertOrder(comparatorListOriginal, comparatorList, 0, comparatorList.size());
        //B-2) Partial sort
        comparatorList = createArrayListWithRandomData(TEST_SIZE, currentSeed);
        comparatorListOriginal = createArrayListWithRandomData(TEST_SIZE, currentSeed);
        comparatorList.sort(lowerRange, upperRange, comp);
        assertOrder(comparatorListOriginal, comparatorList, lowerRange, upperRange);
    }

    private KTypeArrayList<KType> createArrayListWithRandomData(final int size, final long currentSeed)
    {
        final Random prng = new Random(currentSeed);

        final KTypeArrayList<KType> newArray = KTypeArrayList.newInstance();

        for (int i = 0; i < size; i++)
        {
            newArray.add(cast(prng.nextInt()));
        }

        return newArray;
    }

}
