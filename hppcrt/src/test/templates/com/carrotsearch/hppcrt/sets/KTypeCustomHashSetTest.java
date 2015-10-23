package com.carrotsearch.hppcrt.sets;

import java.util.*;

import org.junit.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.hash.BitMixer;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit tests for {@link KTypeHashSet}.
 */
/*! ${TemplateOptions.doNotGenerateKType("byte", "char", "short", "int", "long", "float", "double" )} !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeCustomHashSetTest<KType> extends AbstractKTypeHashSetTest<KType>
{

    private static final int STRIDE = 13;

    // Define a specific way for equivalence:

    protected int specialHashCode(final KType object) {

        return BitMixer.mix(cast(castType(object) + KTypeCustomHashSetTest.STRIDE));
    }

    protected boolean specialEquals(final KType o1, final KType o2) {

        return Intrinsics.<KType> equals(cast(castType(o1) + KTypeCustomHashSetTest.STRIDE), cast(castType(o2) + KTypeCustomHashSetTest.STRIDE));
    }

    /**
     * Define a customized HashSetType
     */
    public class KTypeCustomizedHashSet<KType> extends KTypeHashSet<KType>
    {
        @Override
        protected int hashKey(final KType object) {

            return BitMixer.mix(cast(castType(object) + KTypeCustomHashSetTest.STRIDE));
        }

        @Override
        protected boolean equalKeys(final KType o1, final KType o2) {

            return Intrinsics.<KType> equals(cast(castType(o1) + KTypeCustomHashSetTest.STRIDE), cast(castType(o2) + KTypeCustomHashSetTest.STRIDE));
        }

        /**
         * Re-define constructors.
         */
        public KTypeCustomizedHashSet() {
            super();
        }

        public KTypeCustomizedHashSet(final int initialCapacity) {
            super(initialCapacity);
        }

        public KTypeCustomizedHashSet(final int initialCapacity, final double loadFactor) {
            super(initialCapacity, loadFactor);

        }

        public KTypeCustomizedHashSet(final KTypeContainer<KType> container) {
            super(container);
        }

        @Override
        public KTypeCustomizedHashSet<KType> clone() {

            //clone to size() to prevent eventual exponential growth
            final KTypeCustomizedHashSet<KType> cloned = new KTypeCustomizedHashSet<KType>(size(), this.loadFactor);

            //We must NOT clone, because of the independent perturbation seeds
            cloned.addAll(this);

            cloned.allocatedDefaultKey = this.allocatedDefaultKey;

            return cloned;
        }

    }//end class KTypeCustomizedHashSet

    @Override
    protected KTypeSet<KType> createNewSetInstance(final int initialCapacity, final double loadFactor) {

        return new KTypeCustomizedHashSet<KType>(initialCapacity, loadFactor);
    }

    @Override
    protected KType[] getKeys(final KTypeSet<KType> testSet) {

        final KTypeCustomizedHashSet<KType> concreteClass = (KTypeCustomizedHashSet<KType>) (testSet);

        return Intrinsics.<KType[]> cast(concreteClass.keys);
    }

    @Override
    protected boolean isAllocatedDefaultKey(final KTypeSet<KType> testSet) {
        final KTypeCustomizedHashSet<KType> concreteClass = (KTypeCustomizedHashSet<KType>) (testSet);
        return concreteClass.allocatedDefaultKey;
    }

    @Override
    protected KTypeSet<KType> getClone(final KTypeSet<KType> testSet) {
        final KTypeCustomizedHashSet<KType> concreteClass = (KTypeCustomizedHashSet<KType>) (testSet);
        return concreteClass.clone();
    }

    @Override
    protected KTypeSet<KType> getFrom(final KTypeContainer<KType> container) {

        //use the copy constructor
        return new KTypeCustomizedHashSet<KType>(container);
    }

    @Override
    protected KTypeSet<KType> getFrom(final KType... elements) {

        //use the copy constructor
        final KTypeCustomizedHashSet<KType> returnedInstance = new KTypeCustomizedHashSet<KType>();

        for (int i = 0; i < elements.length; i++) {

            returnedInstance.add(elements[i]);
        }

        return returnedInstance;
    }

    @Override
    protected KTypeSet<KType> getFromArray(final KType[] keys) {

        //use the copy constructor
        final KTypeCustomizedHashSet<KType> returnedInstance = new KTypeCustomizedHashSet<KType>();

        for (int i = 0; i < keys.length; i++) {

            returnedInstance.add(keys[i]);
        }

        return returnedInstance;
    }

    @Override
    protected void addFromArray(final KTypeSet<KType> testSet, final KType... keys) {
        final KTypeCustomizedHashSet<KType> concreteClass = (KTypeCustomizedHashSet<KType>) (testSet);

        for (final KType key : keys) {

            concreteClass.add(key);
        }
    }

    @Override
    protected KTypeSet<KType> getCopyConstructor(final KTypeSet<KType> testSet) {

        return new KTypeCustomizedHashSet<KType>(testSet);
    }

    @Override
    int getEntryPoolSize(final KTypeSet<KType> testSet) {
        final KTypeCustomizedHashSet<KType> concreteClass = (KTypeCustomizedHashSet<KType>) (testSet);
        return concreteClass.entryIteratorPool.size();
    }

    @Override
    int getEntryPoolCapacity(final KTypeSet<KType> testSet) {
        final KTypeCustomizedHashSet<KType> concreteClass = (KTypeCustomizedHashSet<KType>) (testSet);
        return concreteClass.entryIteratorPool.capacity();
    }

    //////////////////////////////////////
    /// Implementation-specific tests
    /////////////////////////////////////

    /* */
    @Test
    public void testAddVarArgs()
    {
        final KTypeCustomizedHashSet<KType> testSet = new KTypeCustomizedHashSet<KType>();
        testSet.add(this.keyE, this.key1, this.key2, this.key1, this.keyE);
        Assert.assertEquals(3, testSet.size());
        TestUtils.assertSortedListEquals(testSet.toArray(), this.keyE, this.key1, this.key2);
    }

    /* */
    @Test
    public void testAdd2Elements() {
        final KTypeCustomizedHashSet<KType> testSet = new KTypeCustomizedHashSet<KType>();

        testSet.add(this.key1, this.key1);
        Assert.assertEquals(1, testSet.size());
        Assert.assertEquals(1, testSet.add(this.key1, this.key2));
        Assert.assertEquals(2, testSet.size());
        Assert.assertEquals(1, testSet.add(this.keyE, this.key1));
        Assert.assertEquals(3, testSet.size());
    }

    //
    @Test
    public void testCustomizedCloneEquals() {
        //a) Check that 2 different sets filled the same way with same values and equal strategies.
        //are indeed equal.
        final long TEST_SEED = 23167132166456L;
        final int TEST_SIZE = (int) 100e3;


        final KTypeCustomizedHashSet<KType> refSet = createCustomizedMapWithRandomData(TEST_SIZE, TEST_SEED);
        KTypeCustomizedHashSet<KType> refSet2 = createCustomizedMapWithRandomData(TEST_SIZE, TEST_SEED);

        Assert.assertEquals(refSet, refSet2);

        //b) Clone the above. All sets are now identical.
        KTypeCustomizedHashSet<KType> refSetclone = refSet.clone();
        KTypeCustomizedHashSet<KType> refSet2clone = refSet2.clone();

        Assert.assertEquals(refSet, refSetclone);
        Assert.assertEquals(refSetclone, refSet2);
        Assert.assertEquals(refSet2, refSet2clone);
        Assert.assertEquals(refSet2clone, refSet);

        //cleanup
        refSetclone = null;
        refSet2 = null;
        refSet2clone = null;
        System.gc();

        //c) Create a set nb 3 with same equivalence logic, but they are not equal because one
        // is KTypeCustomizedHashSet, the other another anonymous type:
        final KTypeHashSet<KType> refSet3 = new KTypeHashSet<KType>() {

            @Override
            protected int hashKey(final KType key) {

                return specialHashCode(key);
            }

            @Override
            protected boolean equalKeys(final KType a, final KType b) {

                return specialEquals(a, b);
            };
        };

        //because they do the same thing as above, but with semantically different strategies, ref3 is != ref
        Assert.assertFalse(refSet.equals(refSet3));
        Assert.assertFalse(refSet3.equals(refSet));

        //clone the anonymous type : all types are different !
        final KTypeHashSet<KType> refSet3Cloned = refSet3.clone();

        Assert.assertFalse(refSet3Cloned.equals(refSet3));
        Assert.assertFalse(refSet3.equals(refSet3Cloned));
    }

    @Test
    public void testCustomizedAddContainsRemove() {
        final long TEST_SEED = 749741621030146103L;
        final int TEST_SIZE = (int) 500e3;

        //those following 3  sets behave indeed the same in the test context:
        final KTypeCustomizedHashSet<KType> refSet = new KTypeCustomizedHashSet<KType>();

        final KTypeCustomizedHashSet<KType> refSetIdenticalStrategy = new KTypeCustomizedHashSet<KType>();

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

    private KTypeCustomizedHashSet<KType> createCustomizedMapWithRandomData(final int size, final long randomSeed) {

        final Random prng = new Random(randomSeed);

        final KTypeCustomizedHashSet<KType> newSet = new KTypeCustomizedHashSet<KType>();

        for (int i = 0; i < size; i++) {

            newSet.add(cast(prng.nextInt()));
        }

        return newSet;
    }
}
