package com.carrotsearch.hppcrt.maps;

import java.util.*;

import org.junit.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.hash.BitMixer;
import com.carrotsearch.hppcrt.strategies.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Tests for {@link KTypeVTypeHashMap}.
 */
/*! ${TemplateOptions.doNotGenerateKType("byte", "char", "short", "int", "long", "float", "double" )} !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeVTypeCustomHashMapTest<KType, VType> extends AbstractKTypeVTypeHashMapTest<KType, VType>
{
    private static final int STRIDE = 13;

    // Define a specific way for equivalence:

    protected int specialHashCode(final KType object) {

        return BitMixer.mix(cast(castType(object) + KTypeVTypeCustomHashMapTest.STRIDE));
    }

    protected boolean specialEquals(final KType o1, final KType o2) {

        return Intrinsics.<KType> equals(cast(castType(o1) + KTypeVTypeCustomHashMapTest.STRIDE), cast(castType(o2) + KTypeVTypeCustomHashMapTest.STRIDE));
    }

    /**
     * Define a customized HashMapType
     */
    public class KTypeVTypeCustomizedHashMap<KType, VType> extends KTypeVTypeHashMap<KType, VType>
    {
        @Override
        protected int hashKey(final KType object) {

            return BitMixer.mix(cast(castType(object) + KTypeVTypeCustomHashMapTest.STRIDE));
        }

        @Override
        protected boolean equalKeys(final KType o1, final KType o2) {

            return Intrinsics.<KType> equals(cast(castType(o1) + KTypeVTypeCustomHashMapTest.STRIDE), cast(castType(o2) + KTypeVTypeCustomHashMapTest.STRIDE));
        }

        /**
         * Re-define constructors.
         */
        public KTypeVTypeCustomizedHashMap() {
            super();
        }

        /**
         * Creates a hash map with the given initial capacity, default load factor of
         * {@link HashContainers#DEFAULT_LOAD_FACTOR}.
         * 
         * <p>See class notes about hash distribution importance.</p>
         * 
         * @param initialCapacity Initial capacity (greater than zero and automatically
         *            rounded to the next power of two).
         */
        public KTypeVTypeCustomizedHashMap(final int initialCapacity) {
            super(initialCapacity);
        }

        /**
         * Creates a hash map with the given initial capacity,
         * load factor.
         *
         * @param loadFactor The load factor (greater than zero and smaller than 1).
         */
        public KTypeVTypeCustomizedHashMap(final int initialCapacity, final double loadFactor) {
            super(initialCapacity, loadFactor);

        }

        /**
         * copy constructor
         */
        public KTypeVTypeCustomizedHashMap(final KTypeVTypeAssociativeContainer<KType, VType> container) {
            super(container);

        }

        @Override
        public KTypeVTypeCustomizedHashMap<KType, VType> clone() {

            //clone to size() to prevent some cases of exponential sizes,
            final KTypeVTypeCustomizedHashMap<KType, VType> cloned = new KTypeVTypeCustomizedHashMap<KType, VType>(size(), this.loadFactor);

            //We must NOT clone because of independent perturbations seeds
            cloned.putAll(this);

            cloned.allocatedDefaultKeyValue = this.allocatedDefaultKeyValue;
            cloned.allocatedDefaultKey = this.allocatedDefaultKey;
            cloned.defaultValue = this.defaultValue;

            return cloned;
        }

    }//end class KTypeVTypeCustomizedHashMap

    @Override
    protected KTypeVTypeMap<KType, VType> createNewMapInstance(final int initialCapacity, final double loadFactor) {

        return new KTypeVTypeCustomizedHashMap<KType, VType>(initialCapacity, loadFactor);
    }

    @Override
    protected KType[] getKeys(final KTypeVTypeMap<KType, VType> testMap) {

        final KTypeVTypeCustomizedHashMap<KType, VType> concreteClass = (KTypeVTypeCustomizedHashMap<KType, VType>) (testMap);

        return Intrinsics.<KType[]> cast(concreteClass.keys);
    }

    @Override
    protected VType[] getValues(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomizedHashMap<KType, VType> concreteClass = (KTypeVTypeCustomizedHashMap<KType, VType>) (testMap);

        return Intrinsics.<VType[]> cast(concreteClass.values);
    }

    @Override
    protected boolean isAllocatedDefaultKey(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomizedHashMap<KType, VType> concreteClass = (KTypeVTypeCustomizedHashMap<KType, VType>) (testMap);

        return concreteClass.allocatedDefaultKey;

    }

    @Override
    protected VType getAllocatedDefaultKeyValue(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomizedHashMap<KType, VType> concreteClass = (KTypeVTypeCustomizedHashMap<KType, VType>) (testMap);

        return concreteClass.allocatedDefaultKeyValue;
    }

    @Override
    protected KTypeVTypeMap<KType, VType> getClone(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeCustomizedHashMap<KType, VType> concreteClass = (KTypeVTypeCustomizedHashMap<KType, VType>) (testMap);

        return concreteClass.clone();
    }

    @Override
    protected KTypeVTypeMap<KType, VType> getFrom(final KTypeVTypeMap<KType, VType> testMap) {

        //use the copy constructor of the specialized type
        return new KTypeVTypeCustomizedHashMap<KType, VType>(testMap);
    }

    @Override
    protected KTypeVTypeMap<KType, VType> getFromArrays(final KType[] keys, final VType[] values) {

        Assert.assertEquals(keys.length, values.length);

        //cannot use the original from(), so use the copy constructor and push explicitly.
        final KTypeVTypeCustomizedHashMap<KType, VType> returnedInstance = new KTypeVTypeCustomizedHashMap<KType, VType>();

        for (int i = 0; i < keys.length; i++) {

            returnedInstance.put(keys[i], values[i]);
        }

        return returnedInstance;
    }

    @Override
    protected KTypeVTypeMap<KType, VType> getCopyConstructor(final KTypeVTypeMap<KType, VType> testMap) {

        //use the copy constructor
        return new KTypeVTypeCustomizedHashMap<KType, VType>(testMap);
    }

    @Override
    int getEntryPoolSize(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeHashMap<KType, VType> concreteClass = (KTypeVTypeHashMap<KType, VType>) (testMap);

        return concreteClass.entryIteratorPool.size();
    }

    @Override
    int getKeysPoolSize(final KTypeCollection<KType> keys) {

        final KTypeVTypeHashMap<KType, VType>.KeysCollection concreteClass = (KTypeVTypeHashMap<KType, VType>.KeysCollection) (keys);

        return concreteClass.keyIteratorPool.size();
    }

    @Override
    int getValuesPoolSize(final KTypeCollection<VType> values) {
        final KTypeVTypeHashMap<KType, VType>.ValuesCollection concreteClass = (KTypeVTypeHashMap<KType, VType>.ValuesCollection) (values);

        return concreteClass.valuesIteratorPool.size();
    }

    @Override
    int getEntryPoolCapacity(final KTypeVTypeMap<KType, VType> testMap) {
        final KTypeVTypeHashMap<KType, VType> concreteClass = (KTypeVTypeHashMap<KType, VType>) (testMap);

        return concreteClass.entryIteratorPool.capacity();
    }

    @Override
    int getKeysPoolCapacity(final KTypeCollection<KType> keys) {
        final KTypeVTypeHashMap<KType, VType>.KeysCollection concreteClass = (KTypeVTypeHashMap<KType, VType>.KeysCollection) (keys);

        return concreteClass.keyIteratorPool.capacity();
    }

    @Override
    int getValuesPoolCapacity(final KTypeCollection<VType> values) {
        final KTypeVTypeHashMap<KType, VType>.ValuesCollection concreteClass = (KTypeVTypeHashMap<KType, VType>.ValuesCollection) (values);

        return concreteClass.valuesIteratorPool.capacity();
    }

    //////////////////////////////////////
    /// Implementation-specific tests
    /////////////////////////////////////

    //
    @Test
    public void testCustomizedCloneEquals() {
        //a) Check that 2 different sets filled the same way with same values and equal strategies.
        //are indeed equal.
        final long TEST_SEED = 23167132166456L;
        final int TEST_SIZE = (int) 100e3;


        final KTypeVTypeHashMap<KType, VType> refMap = createCustomizedMapWithRandomData(TEST_SIZE, TEST_SEED);
        KTypeVTypeHashMap<KType, VType> refMap2 = createCustomizedMapWithRandomData(TEST_SIZE, TEST_SEED);

        //Both are constructed with the same KTypeVTypeCustomizedHashMap type, should be equals.
        Assert.assertEquals(refMap, refMap2);

        //b) Clone the above. All sets are now identical.
        KTypeVTypeHashMap<KType, VType> refMapclone = refMap.clone();
        KTypeVTypeHashMap<KType, VType> refMap2clone = refMap2.clone();

        Assert.assertEquals(refMap, refMapclone);
        Assert.assertEquals(refMapclone, refMap2);
        Assert.assertEquals(refMap2, refMap2clone);
        Assert.assertEquals(refMap2clone, refMap);

        //cleanup
        refMapclone = null;
        refMap2 = null;
        refMap2clone = null;
        System.gc();

        //c) Create a refMap3 with same equivalence logic, but they are not equal because one
        // is KTypeVTypeCustomizedHashMap, the other another anonymous type:

        final KTypeVTypeHashMap<KType, VType> refMap3 = new KTypeVTypeHashMap<KType, VType>() {

            @Override
            protected int hashKey(final KType key) {

                return specialHashCode(key);
            }

            @Override
            protected boolean equalKeys(final KType a, final KType b) {

                return specialEquals(a, b);
            }
        };

        Assert.assertFalse(refMap.equals(refMap3));
        Assert.assertFalse(refMap3.equals(refMap));

        //Clone the anonymous type : all types are different !
        final KTypeVTypeHashMap<KType, VType> refMap3Cloned = refMap3.clone();

        Assert.assertFalse(refMap3Cloned.equals(refMap3));
        Assert.assertFalse(refMap3.equals(refMap3Cloned));
    }

    @Test
    public void testCustomizedAddContainsRemove() {

        final long TEST_SEED = 749741621030146103L;
        final int TEST_SIZE = (int) 500e3;

        //those following 3  maps behave indeed the same in the test context:
        final KTypeVTypeCustomizedHashMap<KType, VType> refMap = new KTypeVTypeCustomizedHashMap<KType, VType>();

        final KTypeVTypeCustomizedHashMap<KType, VType> refMapSameType = new KTypeVTypeCustomizedHashMap<KType, VType>();

        //compute the iterations doing multiple operations
        final Random prng = new Random(TEST_SEED);

        for (int i = 0; i < TEST_SIZE; i++) {
            //a) generate a value to put
            int putValue = prng.nextInt();

            refMap.put(cast(putValue), vcast(putValue));
            refMapSameType.put(cast(putValue), vcast(putValue));

            Assert.assertEquals(refMap.containsKey(cast(putValue)), refMapSameType.containsKey(cast(putValue)));

            final boolean isToBeRemoved = (prng.nextInt() % 3 == 0);
            putValue = prng.nextInt();

            if (isToBeRemoved) {
                refMap.remove(cast(putValue));
                refMapSameType.remove(cast(putValue));

                Assert.assertFalse(refMap.containsKey(cast(putValue)));
                Assert.assertFalse(refMapSameType.containsKey(cast(putValue)));
            }

            Assert.assertEquals(refMap.containsKey(cast(putValue)), refMapSameType.containsKey(cast(putValue)));

            //test size
            Assert.assertEquals(refMap.size(), refMapSameType.size());
        }
    }

    private KTypeVTypeCustomizedHashMap<KType, VType> createCustomizedMapWithRandomData(final int size, final long randomSeed) {
        final Random prng = new Random(randomSeed);

        final KTypeVTypeCustomizedHashMap<KType, VType> newMap = new KTypeVTypeCustomizedHashMap<KType, VType>();

        for (int i = 0; i < size; i++) {
            newMap.put(cast(prng.nextInt()), vcast(prng.nextInt()));

        }

        return newMap;
    }
}
