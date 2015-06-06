package com.carrotsearch.hppcrt.sets;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.hash.MurmurHash3;
import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.hppcrt.strategies.KTypeHashingStrategy;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit tests for {@link KTypeHashSet}.
 */
/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN")} !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeHashSetTest<KType> extends AbstractKTypeHashSetTest<KType>
{

    @Override
    protected KTypeSet<KType> createNewSetInstance(final int initialCapacity, final double loadFactor, final KTypeHashingStrategy<KType> strategy) {

        return new KTypeHashSet<KType>(initialCapacity, loadFactor);
    }

    @Override
    protected KType[] getKeys(final KTypeSet<KType> testSet) {
        final KTypeHashSet<KType> concreteClass = (KTypeHashSet<KType>) (testSet);

        return Intrinsics.<KType[]> cast(concreteClass.keys);
    }

    @Override
    protected boolean isAllocatedDefaultKey(final KTypeSet<KType> testSet) {
        final KTypeHashSet<KType> concreteClass = (KTypeHashSet<KType>) (testSet);
        return concreteClass.allocatedDefaultKey;
    }

    @Override
    protected KTypeSet<KType> getClone(final KTypeSet<KType> testSet) {
        final KTypeHashSet<KType> concreteClass = (KTypeHashSet<KType>) (testSet);
        return concreteClass.clone();
    }

    @Override
    protected KTypeSet<KType> getFrom(final KTypeContainer<KType> container) {

        return KTypeHashSet.from(container);
    }

    @Override
    protected KTypeSet<KType> getFrom(final KType... elements) {

        return KTypeHashSet.from(elements);
    }

    @Override
    protected KTypeSet<KType> getFromArray(final KType[] keys) {

        return KTypeHashSet.from(keys);
    }

    @Override
    protected void addFromArray(final KTypeSet<KType> testSet, final KType... keys) {
        final KTypeHashSet<KType> concreteClass = (KTypeHashSet<KType>) (testSet);

        for (final KType key : keys) {

            concreteClass.add(key);
        }
    }

    @Override
    protected KTypeSet<KType> getCopyConstructor(final KTypeSet<KType> testSet) {
        final KTypeHashSet<KType> concreteClass = (KTypeHashSet<KType>) (testSet);

        return new KTypeHashSet<KType>(concreteClass);
    }

    @Override
    int getEntryPoolSize(final KTypeSet<KType> testSet) {
        final KTypeHashSet<KType> concreteClass = (KTypeHashSet<KType>) (testSet);
        return concreteClass.entryIteratorPool.size();
    }

    @Override
    int getEntryPoolCapacity(final KTypeSet<KType> testSet) {
        final KTypeHashSet<KType> concreteClass = (KTypeHashSet<KType>) (testSet);
        return concreteClass.entryIteratorPool.capacity();
    }

    //////////////////////////////////////
    /// Implementation-specific tests
    /////////////////////////////////////

    /* */
    @Test
    public void testAddVarArgs()
    {
        final KTypeHashSet<KType> testSet = new KTypeHashSet<KType>();
        testSet.add(this.keyE, this.key1, this.key2, this.key1, this.keyE);
        Assert.assertEquals(3, testSet.size());
        TestUtils.assertSortedListEquals(testSet.toArray(), this.keyE, this.key1, this.key2);
    }

    /* */
    @Test
    public void testAdd2Elements() {
        final KTypeHashSet<KType> testSet = new KTypeHashSet<KType>();

        testSet.add(this.key1, this.key1);
        Assert.assertEquals(1, testSet.size());
        Assert.assertEquals(1, testSet.add(this.key1, this.key2));
        Assert.assertEquals(2, testSet.size());
        Assert.assertEquals(1, testSet.add(this.keyE, this.key1));
        Assert.assertEquals(3, testSet.size());
    }
}
