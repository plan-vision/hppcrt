package com.carrotsearch.hppcrt.maps;

import java.util.*;

import org.junit.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.hash.BitMixer;
import com.carrotsearch.hppcrt.strategies.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Tests for {@link KTypeVTypeCustomHashMap}.
 */
/*! ${TemplateOptions.doNotGenerateKType("byte", "char", "short", "float", "double" )} !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeCustomHashMapTest<KType, VType> extends AbstractKTypeVTypeHashMapTest<KType, VType>
{
    private static final int STRIDE = 13;

    protected final KTypeHashingStrategy<KType> TEST_STRATEGY = new KTypeHashingStrategy<KType>() {

        @Override
        public int computeHashCode(final KType object) {

            return BitMixer.mix(cast(castType(object) + KTypeVTypeCustomHashMapTest.STRIDE));
        }

        @Override
        public boolean equals(final KType o1, final KType o2) {

            return Intrinsics.<KType> equals(cast(castType(o1) + KTypeVTypeCustomHashMapTest.STRIDE), cast(castType(o2)
                    + KTypeVTypeCustomHashMapTest.STRIDE));
        }

    };

    @Override
    protected KTypeVTypeMap<KType, VType> createNewMapInstance(final int initialCapacity, final double loadFactor) {

        return new KTypeVTypeCustomHashMap<KType, VType>(initialCapacity, loadFactor, this.TEST_STRATEGY);
    }

    @Override
    protected KType[] getKeys(final KTypeVTypeMap<KType, VType> testMap) {

        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (testMap);

        return Intrinsics.<KType[]> cast(concreteClass.keys);
    }

    @Override
    protected VType[] getValues(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (testMap);

        return Intrinsics.<VType[]> cast(concreteClass.values);
    }

    @Override
    protected boolean isAllocatedDefaultKey(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (testMap);

        return concreteClass.allocatedDefaultKey;

    }

    @Override
    protected VType getAllocatedDefaultKeyValue(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (testMap);

        return concreteClass.allocatedDefaultKeyValue;
    }

    @Override
    protected KTypeVTypeMap<KType, VType> getClone(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (testMap);

        return concreteClass.clone();
    }

    @Override
    protected KTypeVTypeMap<KType, VType> getFrom(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (testMap);

        return KTypeVTypeCustomHashMap.from(concreteClass, concreteClass.hashStrategy);
    }

    @Override
    protected KTypeVTypeMap<KType, VType> getFromArrays(final KType[] keys, final VType[] values) {
        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (this.map);

        return KTypeVTypeCustomHashMap.from(Intrinsics.<KType[]> cast(keys),
                Intrinsics.<VType[]> cast(values), concreteClass.hashStrategy);
    }

    @Override
    protected KTypeVTypeMap<KType, VType> getCopyConstructor(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (testMap);

        return new KTypeVTypeCustomHashMap<KType, VType>(concreteClass, concreteClass.hashStrategy);
    }

    @Override
    int getEntryPoolSize(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (testMap);

        return concreteClass.entryIteratorPool.size();
    }

    @Override
    int getKeysPoolSize(final KTypeCollection<KType> keys) {

        final KTypeVTypeCustomHashMap<KType, VType>.KeysCollection concreteClass = (KTypeVTypeCustomHashMap<KType, VType>.KeysCollection) (keys);

        return concreteClass.keyIteratorPool.size();
    }

    @Override
    int getValuesPoolSize(final KTypeCollection<VType> values) {
        final KTypeVTypeCustomHashMap<KType, VType>.ValuesCollection concreteClass = (KTypeVTypeCustomHashMap<KType, VType>.ValuesCollection) (values);

        return concreteClass.valuesIteratorPool.size();
    }

    @Override
    int getEntryPoolCapacity(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomHashMap<KType, VType> concreteClass = (KTypeVTypeCustomHashMap<KType, VType>) (testMap);

        return concreteClass.entryIteratorPool.capacity();
    }

    @Override
    int getKeysPoolCapacity(final KTypeCollection<KType> keys) {
        final KTypeVTypeCustomHashMap<KType, VType>.KeysCollection concreteClass = (KTypeVTypeCustomHashMap<KType, VType>.KeysCollection) (keys);

        return concreteClass.keyIteratorPool.capacity();
    }

    @Override
    int getValuesPoolCapacity(final KTypeCollection<VType> values) {
        final KTypeVTypeCustomHashMap<KType, VType>.ValuesCollection concreteClass = (KTypeVTypeCustomHashMap<KType, VType>.ValuesCollection) (values);

        return concreteClass.valuesIteratorPool.capacity();
    }

    //////////////////////////////////////
    /// Implementation-specific tests
    /////////////////////////////////////

    //
    @Test
    public void testHashingStrategyCloneEquals() {
        //a) Check that 2 different sets filled the same way with same values and equal strategies.
        //are indeed equal.
        final long TEST_SEED = 23167132166456L;
        final int TEST_SIZE = (int) 100e3;

        final KTypeStandardHash<KType> std1 = new KTypeStandardHash<KType>();

        final KTypeVTypeCustomHashMap<KType, VType> refMap = createMapWithRandomData(TEST_SIZE, std1, TEST_SEED);
        KTypeVTypeCustomHashMap<KType, VType> refMap2 = createMapWithRandomData(TEST_SIZE, std1, TEST_SEED);

        Assert.assertEquals(refMap, refMap2);

        //b) Clone the above. All sets are now identical.
        KTypeVTypeCustomHashMap<KType, VType> refMapclone = refMap.clone();
        KTypeVTypeCustomHashMap<KType, VType> refMap2clone = refMap2.clone();

        //all strategies are null
        Assert.assertEquals(refMap.strategy(), refMap2.strategy());
        Assert.assertEquals(refMap2.strategy(), refMapclone.strategy());
        Assert.assertEquals(refMapclone.strategy(), refMap2clone.strategy());
        Assert.assertEquals(refMap2clone.strategy(), std1);

        Assert.assertEquals(refMap, refMapclone);
        Assert.assertEquals(refMapclone, refMap2);
        Assert.assertEquals(refMap2, refMap2clone);
        Assert.assertEquals(refMap2clone, refMap);

        //cleanup
        refMapclone = null;
        refMap2 = null;
        refMap2clone = null;
        System.gc();

        //c) Create a set nb 3 with same integer content, but with a strategy mapping on equals.
        final KTypeVTypeCustomHashMap<KType, VType> refMap3 = createMapWithRandomData(TEST_SIZE,
                new KTypeHashingStrategy<KType>() {

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
        Assert.assertFalse(refMap.equals(refMap3));

        //However, if we cloned refMap3
        final KTypeVTypeCustomHashMap<KType, VType> refMap3clone = refMap3.clone();
        Assert.assertEquals(refMap3, refMap3clone);

        //strategies are copied by reference only
        Assert.assertTrue(refMap3.strategy() == refMap3clone.strategy());

        //d) Create identical set with same different strategy instances, but which consider themselves equals()
        KTypeVTypeCustomHashMap<KType, VType> refMap4 = createMapWithRandomData(TEST_SIZE,
                new KTypeHashingStrategy<KType>() {

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

        KTypeVTypeCustomHashMap<KType, VType> refMap4Image = createMapWithRandomData(TEST_SIZE,
                new KTypeHashingStrategy<KType>() {

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

        Assert.assertEquals(refMap4, refMap4Image);
        //but strategy instances are indeed 2 different objects
        Assert.assertFalse(refMap4.strategy() == refMap4Image.strategy());

        //cleanup
        refMap4 = null;
        refMap4Image = null;
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

        final KTypeVTypeCustomHashMap<KType, VType> refMap5 = createMapWithRandomData(TEST_SIZE, alwaysDifferentStrategy,
                TEST_SEED);
        final KTypeVTypeCustomHashMap<KType, VType> refMap5alwaysDifferent = createMapWithRandomData(TEST_SIZE,
                alwaysDifferentStrategy, TEST_SEED);

        //both sets are NOT equal because their strategies said they are different
        Assert.assertFalse(refMap5.equals(refMap5alwaysDifferent));
    }

    @Test
    public void testHashingStrategyAddContainsRemove() {
        final long TEST_SEED = 749741621030146103L;
        final int TEST_SIZE = (int) 500e3;

        //those following 3  sets behave indeed the same in the test context:
        final KTypeVTypeCustomHashMap<KType, VType> refMap = KTypeVTypeCustomHashMap
                .newInstance(new KTypeStandardHash<KType>());

        final KTypeVTypeCustomHashMap<KType, VType> refMapIdenticalStrategy = KTypeVTypeCustomHashMap.newInstance(
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

            refMap.put(cast(putValue), vcast(putValue));
            refMapIdenticalStrategy.put(cast(putValue), vcast(putValue));

            Assert.assertEquals(refMap.containsKey(cast(putValue)), refMapIdenticalStrategy.containsKey(cast(putValue)));

            final boolean isToBeRemoved = (prng.nextInt() % 3 == 0);
            putValue = prng.nextInt();

            if (isToBeRemoved) {
                refMap.remove(cast(putValue));
                refMapIdenticalStrategy.remove(cast(putValue));

                Assert.assertFalse(refMap.containsKey(cast(putValue)));
                Assert.assertFalse(refMapIdenticalStrategy.containsKey(cast(putValue)));
            }

            Assert.assertEquals(refMap.containsKey(cast(putValue)), refMapIdenticalStrategy.containsKey(cast(putValue)));

            //test size
            Assert.assertEquals(refMap.size(), refMapIdenticalStrategy.size());
        }
    }

    private KTypeVTypeCustomHashMap<KType, VType> createMapWithRandomData(final int size,
            final KTypeHashingStrategy<? super KType> strategy, final long randomSeed) {
        final Random prng = new Random(randomSeed);

        final KTypeVTypeCustomHashMap<KType, VType> newMap = KTypeVTypeCustomHashMap.newInstance(
                Containers.DEFAULT_EXPECTED_ELEMENTS, HashContainers.DEFAULT_LOAD_FACTOR, strategy);

        for (int i = 0; i < size; i++) {
            newMap.put(cast(prng.nextInt()), vcast(prng.nextInt()));

        }

        return newMap;
    }
}
