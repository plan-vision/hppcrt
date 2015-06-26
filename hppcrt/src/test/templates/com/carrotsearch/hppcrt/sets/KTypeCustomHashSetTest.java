package com.carrotsearch.hppcrt.sets;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.hash.BitMixer;
import com.carrotsearch.hppcrt.hash.MurmurHash3;
import com.carrotsearch.hppcrt.hash.PhiMix;
import com.carrotsearch.hppcrt.lists.*;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit tests for {@link KTypeCustomHashSet}.
 */
/*! ${TemplateOptions.doNotGenerateKType("byte", "char", "short", "float", "double" )} !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeCustomHashSetTest<KType> extends AbstractKTypeHashSetTest<KType>
{

    private static final int STRIDE = 13;

    protected final KTypeHashingStrategy<KType> TEST_STRATEGY = new KTypeHashingStrategy<KType>() {

        @Override
        public int computeHashCode(final KType object) {

            return BitMixer.mix(cast(castType(object) + KTypeCustomHashSetTest.STRIDE));
        }

        @Override
        public boolean equals(final KType o1, final KType o2) {

            return Intrinsics.<KType> equals(cast(castType(o1) + KTypeCustomHashSetTest.STRIDE), cast(castType(o2)
                    + KTypeCustomHashSetTest.STRIDE));
        }

    };

    @Override
    protected KTypeSet<KType> createNewSetInstance(final int initialCapacity, final double loadFactor) {

        return new KTypeCustomHashSet<KType>(initialCapacity, loadFactor, this.TEST_STRATEGY);
    }

    @Override
    protected KType[] getKeys(final KTypeSet<KType> testSet) {
        final KTypeCustomHashSet<KType> concreteClass = (KTypeCustomHashSet<KType>) (testSet);

        return Intrinsics.<KType[]> cast(concreteClass.keys);
    }

    @Override
    protected boolean isAllocatedDefaultKey(final KTypeSet<KType> testSet) {
        final KTypeCustomHashSet<KType> concreteClass = (KTypeCustomHashSet<KType>) (testSet);
        return concreteClass.allocatedDefaultKey;
    }

    @Override
    protected KTypeSet<KType> getClone(final KTypeSet<KType> testSet) {
        final KTypeCustomHashSet<KType> concreteClass = (KTypeCustomHashSet<KType>) (testSet);
        return concreteClass.clone();
    }

    @Override
    protected KTypeSet<KType> getFrom(final KTypeContainer<KType> container) {

        return KTypeCustomHashSet.from(container, this.TEST_STRATEGY);
    }

    @Override
    protected KTypeSet<KType> getFrom(final KType... elements) {

        return KTypeCustomHashSet.from(this.TEST_STRATEGY, elements);
    }

    @Override
    protected KTypeSet<KType> getFromArray(final KType[] keys) {

        return KTypeCustomHashSet.from(this.TEST_STRATEGY, keys);
    }

    @Override
    protected void addFromArray(final KTypeSet<KType> testSet, final KType... keys) {
        final KTypeCustomHashSet<KType> concreteClass = (KTypeCustomHashSet<KType>) (testSet);

        for (final KType key : keys) {

            concreteClass.add(key);
        }
    }

    @Override
    protected KTypeSet<KType> getCopyConstructor(final KTypeSet<KType> testSet) {
        final KTypeCustomHashSet<KType> concreteClass = (KTypeCustomHashSet<KType>) (testSet);

        return new KTypeCustomHashSet<KType>(concreteClass, this.TEST_STRATEGY);
    }

    @Override
    int getEntryPoolSize(final KTypeSet<KType> testSet) {
        final KTypeCustomHashSet<KType> concreteClass = (KTypeCustomHashSet<KType>) (testSet);
        return concreteClass.entryIteratorPool.size();
    }

    @Override
    int getEntryPoolCapacity(final KTypeSet<KType> testSet) {
        final KTypeCustomHashSet<KType> concreteClass = (KTypeCustomHashSet<KType>) (testSet);
        return concreteClass.entryIteratorPool.capacity();
    }

    //////////////////////////////////////
    /// Implementation-specific tests
    /////////////////////////////////////

    /* */
    @Test
    public void testAddVarArgs()
    {
        final KTypeCustomHashSet<KType> testSet = new KTypeCustomHashSet<KType>(this.TEST_STRATEGY);
        testSet.add(this.keyE, this.key1, this.key2, this.key1, this.keyE);
        Assert.assertEquals(3, testSet.size());
        TestUtils.assertSortedListEquals(testSet.toArray(), this.keyE, this.key1, this.key2);
    }

    /* */
    @Test
    public void testAdd2Elements() {
        final KTypeCustomHashSet<KType> testSet = new KTypeCustomHashSet<KType>(this.TEST_STRATEGY);

        testSet.add(this.key1, this.key1);
        Assert.assertEquals(1, testSet.size());
        Assert.assertEquals(1, testSet.add(this.key1, this.key2));
        Assert.assertEquals(2, testSet.size());
        Assert.assertEquals(1, testSet.add(this.keyE, this.key1));
        Assert.assertEquals(3, testSet.size());
    }

    //
    @Test
    public void testHashingStrategyCloneEquals() {
        //a) Check that 2 different sets filled the same way with same values and equal strategies.
        //are indeed equal.
        final long TEST_SEED = 23167132166456L;
        final int TEST_SIZE = (int) 100e3;

        final KTypeStandardHash<KType> std1 = new KTypeStandardHash<KType>();

        final KTypeCustomHashSet<KType> refSet = createSetWithRandomData(TEST_SIZE, std1, TEST_SEED);
        KTypeCustomHashSet<KType> refSet2 = createSetWithRandomData(TEST_SIZE, std1, TEST_SEED);

        Assert.assertEquals(refSet, refSet2);

        //b) Clone the above. All sets are now identical.
        KTypeCustomHashSet<KType> refSetclone = refSet.clone();
        KTypeCustomHashSet<KType> refSet2clone = refSet2.clone();

        //all strategies are null
        Assert.assertEquals(refSet.strategy(), refSet2.strategy());
        Assert.assertEquals(refSet2.strategy(), refSetclone.strategy());
        Assert.assertEquals(refSetclone.strategy(), refSet2clone.strategy());
        Assert.assertEquals(refSet2clone.strategy(), std1);

        Assert.assertEquals(refSet, refSetclone);
        Assert.assertEquals(refSetclone, refSet2);
        Assert.assertEquals(refSet2, refSet2clone);
        Assert.assertEquals(refSet2clone, refSet);

        //cleanup
        refSetclone = null;
        refSet2 = null;
        refSet2clone = null;
        System.gc();

        //c) Create a set nb 3 with same integer content, but with a strategy mapping on equals.
        final KTypeCustomHashSet<KType> refSet3 = createSetWithRandomData(TEST_SIZE, new KTypeHashingStrategy<KType>() {

            @Override
            public int computeHashCode(final KType object) {

                return BitMixer.mix(object);
            }

            @Override
            public boolean equals(final KType o1, final KType o2) {

                return Intrinsics.<KType> equals(o1, o2);
            }
        }, TEST_SEED);

        //because they do the same thing as above, but with semantically different strategies, ref3 is != ref
        Assert.assertFalse(refSet.equals(refSet3));

        //However, if we cloned refSet3
        final KTypeCustomHashSet<KType> refSet3clone = refSet3.clone();
        Assert.assertEquals(refSet3, refSet3clone);

        //strategies are copied by reference only
        Assert.assertTrue(refSet3.strategy() == refSet3clone.strategy());

        //d) Create identical set with same different strategy instances, but which consider themselves equals()
        KTypeCustomHashSet<KType> refSet4 = createSetWithRandomData(TEST_SIZE, new KTypeHashingStrategy<KType>() {

            @Override
            public boolean equals(final Object obj) {

                return true;
            }

            @Override
            public int computeHashCode(final KType object) {

                return BitMixer.mix(object);
            }

            @Override
            public boolean equals(final KType o1, final KType o2) {

                return Intrinsics.<KType> equals(o1, o2);
            }
        }, TEST_SEED);

        KTypeCustomHashSet<KType> refSet4Image = createSetWithRandomData(TEST_SIZE, new KTypeHashingStrategy<KType>() {

            @Override
            public boolean equals(final Object obj) {

                return true;
            }

            @Override
            public int computeHashCode(final KType object) {

                return BitMixer.mix(object);
            }

            @Override
            public boolean equals(final KType o1, final KType o2) {

                return Intrinsics.<KType> equals(o1, o2);
            }
        }, TEST_SEED);

        Assert.assertEquals(refSet4, refSet4Image);
        //but strategy instances are indeed 2 different objects
        Assert.assertFalse(refSet4.strategy() == refSet4Image.strategy());

        //cleanup
        refSet4 = null;
        refSet4Image = null;
        System.gc();

        //e) Do contrary to 4), hashStrategies always != from each other by equals.
        final KTypeHashingStrategy<KType> alwaysDifferentStrategy = new KTypeHashingStrategy<KType>() {

            @Override
            public boolean equals(final Object obj) {

                //never equal !!!
                return false;
            }

            @Override
            public int computeHashCode(final KType object) {

                return BitMixer.mix(object);
            }

            @Override
            public boolean equals(final KType o1, final KType o2) {

                return Intrinsics.<KType> equals(o1, o2);
            }
        };

        final KTypeCustomHashSet<KType> refSet5 = createSetWithRandomData(TEST_SIZE, alwaysDifferentStrategy, TEST_SEED);
        final KTypeCustomHashSet<KType> refSet5alwaysDifferent = createSetWithRandomData(TEST_SIZE,
                alwaysDifferentStrategy, TEST_SEED);

        //both sets are NOT equal because their strategies said they are different
        Assert.assertFalse(refSet5.equals(refSet5alwaysDifferent));
    }

    @Test
    public void testHashingStrategyAddContainsRemove() {
        final long TEST_SEED = 749741621030146103L;
        final int TEST_SIZE = (int) 500e3;

        //those following 3  sets behave indeed the same in the test context:
        final KTypeCustomHashSet<KType> refSet = KTypeCustomHashSet.newInstance(new KTypeStandardHash<KType>());

        final KTypeCustomHashSet<KType> refSetIdenticalStrategy = KTypeCustomHashSet.newInstance(
                Containers.DEFAULT_EXPECTED_ELEMENTS, HashContainers.DEFAULT_LOAD_FACTOR, new KTypeHashingStrategy<KType>() {

                    @Override
                    public boolean equals(final Object obj) {

                        //always
                        return true;
                    }

                    @Override
                    public int computeHashCode(final KType object) {

                        return BitMixer.mix(object);
                    }

                    @Override
                    public boolean equals(final KType o1, final KType o2) {

                        return Intrinsics.<KType> equals(o1, o2);
                    }
                });

        //compute the iterations doing multiple operations
        final Random prng = new Random(TEST_SEED);

        for (int i = 0; i < TEST_SIZE; i++) {
            //a) generate a value to put
            int putValue = prng.nextInt();

            refSet.add(cast(putValue));
            refSetIdenticalStrategy.add(cast(putValue));

            Assert.assertEquals(refSet.contains(cast(putValue)), refSetIdenticalStrategy.contains(cast(putValue)));

            final boolean isToBeRemoved = (prng.nextInt() % 3 == 0);
            putValue = prng.nextInt();

            if (isToBeRemoved) {
                refSet.remove(cast(putValue));
                refSetIdenticalStrategy.remove(cast(putValue));

                Assert.assertFalse(refSet.contains(cast(putValue)));
                Assert.assertFalse(refSetIdenticalStrategy.contains(cast(putValue)));
            }

            Assert.assertEquals(refSet.contains(cast(putValue)), refSetIdenticalStrategy.contains(cast(putValue)));

            //test size
            Assert.assertEquals(refSet.size(), refSetIdenticalStrategy.size());
        }
    }

    private KTypeCustomHashSet<KType> createSetWithRandomData(final int size,
            final KTypeHashingStrategy<? super KType> strategy, final long randomSeed) {
        final Random prng = new Random(randomSeed);

        final KTypeCustomHashSet<KType> newSet = KTypeCustomHashSet.newInstance(Containers.DEFAULT_EXPECTED_ELEMENTS,
                HashContainers.DEFAULT_LOAD_FACTOR, strategy);

        for (int i = 0; i < size; i++) {
            newSet.add(cast(prng.nextInt()));
        }

        return newSet;
    }
}
