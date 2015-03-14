package com.carrotsearch.hppcrt.jmh;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import com.carrotsearch.hppcrt.BitUtil;
import com.carrotsearch.hppcrt.DistributionGenerator;
import com.carrotsearch.hppcrt.XorShiftRandom;
import com.carrotsearch.hppcrt.implementations.Implementations;
import com.carrotsearch.hppcrt.implementations.MapImplementation;
import com.carrotsearch.hppcrt.implementations.MapImplementation.ComparableInt;
import com.carrotsearch.hppcrt.implementations.MapImplementation.HASH_QUALITY;
import com.carrotsearch.hppcrt.lists.IntArrayList;
import com.carrotsearch.hppcrt.sets.IntOpenHashSet;
import com.carrotsearch.hppcrt.sets.ObjectOpenIdentityHashSet;

/**
 * Benchmark putting a given number of integers / Objects into a hashmap.
 * also the base class for all the other Hash benchmarks.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BenchmarkHashMapBase
{

    public enum Distribution
    {
        RANDOM, RAND_LINEAR, HIGHBITS;
    }

    @Param({

        "6000000"
    })
    public int targetSize;

    @Param({
            "0.75"
    })
    public float loadFactor;

    @Param
    public Distribution distribution;

    @Param
    public HASH_QUALITY hash_quality;

    @Param
    public Implementations implementation;

    protected MapImplementation<?> impl;

    protected MapImplementation<?> impl2;

    public static final float HPPC_LOAD_FACTOR_ABSOLUTE_ERROR = 0.05f;

    /**
     * List of ints values to push
     * to fill the map up to its fill factor
     */
    protected int[] pushedKeys;

    public Random prng;

    public class DoNotExecuteBenchmarkException extends RuntimeException
    {
        //
        private static final long serialVersionUID = 1L;

        public DoNotExecuteBenchmarkException() {

            super(String.format("The following parameters combination is skipped :" +
                    " targetSize=%d, loadFactor=%f, distrib=%s, hash quality=%s, implem=%s",
                    BenchmarkHashMapBase.this.targetSize, BenchmarkHashMapBase.this.loadFactor, BenchmarkHashMapBase.this.distribution,
                    BenchmarkHashMapBase.this.hash_quality, BenchmarkHashMapBase.this.implementation));
        }

    }

    /**
     * Base method to setup and generate a series of keys for all benchmarks and derivatives
     * @throws Exception
     */
    protected void setUpCommon() throws Exception
    {
        skipForbiddenCombinations();

        this.prng = new XorShiftRandom(0x11223344);

        //suppose our target load factor is this.loadFactor
        //compute the final size to allocate to reach knowing that the table is indeed sized to a power of 2.
        final int finalArraySize = BitUtil.nextHighestPowerOfTwo((int) (this.targetSize / this.loadFactor));

        //to be sure to NOT reallocate, and have the correct load factor, take a margin !
        final int nbElementsToPush = (int) ((finalArraySize * this.loadFactor) - 32);

        // Our tested implementation, uses preallocation
        this.impl = this.implementation.getInstance(nbElementsToPush, this.loadFactor);

        this.impl2 = this.implementation.getInstance(nbElementsToPush, this.loadFactor);

        DistributionGenerator gene;

        //Generate a dry run into a HashSet until the size has reached nbElementsToPush
        Map<ComparableInt, Integer> dryRunHashSet = null;

        if (this.impl.isIdentityMap()) {

            //all instances are unique anyway, use an IdentityMap to compute
            dryRunHashSet = new IdentityHashMap<ComparableInt, Integer>(nbElementsToPush);
        }
        else {
            dryRunHashSet = new HashMap<ComparableInt, Integer>(nbElementsToPush);
        }

        final IntArrayList keysListToPush = new IntArrayList(nbElementsToPush);

        switch (this.distribution)
        {
            case RANDOM:
                // truly random int in the whole range
                gene = new DistributionGenerator((long) (Integer.MIN_VALUE * 0.5), Integer.MAX_VALUE - 10, this.prng);
                break;
            case RAND_LINEAR:
                //Randomly increasing values in [- nbElementsToPush; 2 * nbElementsToPush]
                gene = new DistributionGenerator(-nbElementsToPush, 3 * nbElementsToPush, this.prng);
                break;
            case HIGHBITS:
                gene = new DistributionGenerator(Integer.MIN_VALUE + 10, Integer.MAX_VALUE, this.prng);

                break;
            default:
                throw new RuntimeException();
        }

        while (dryRunHashSet.size() < nbElementsToPush) {

            int currentKey = -1;

            switch (this.distribution)
            {
                case RANDOM:
                    currentKey = gene.RANDOM.getNext();
                    break;
                case RAND_LINEAR:
                    currentKey = gene.RAND_INCREMENT.getNext();
                    break;
                case HIGHBITS:
                    currentKey = gene.HIGHBITS.getNext();
                    break;
                default:
                    throw new RuntimeException();
            }

            dryRunHashSet.put(new ComparableInt(currentKey, HASH_QUALITY.NORMAL), 0);
            keysListToPush.add(currentKey);

        } //end while

        //Check that HPPC would indeed reach the target load factor, test using a IntSet and Identity
        double effectiveLoadFactor = 0.0;

        if (this.impl.isIdentityMap()) {

            final ObjectOpenIdentityHashSet<ComparableInt> testIdentityFactor = new ObjectOpenIdentityHashSet<ComparableInt>(nbElementsToPush);

            for (final Entry<ComparableInt, Integer> curr : dryRunHashSet.entrySet()) {

                testIdentityFactor.add(curr.getKey());
            }

            effectiveLoadFactor = testIdentityFactor.size() / (double) testIdentityFactor.keys.length;

        }
        else {
            final IntOpenHashSet testSetFactor = new IntOpenHashSet(nbElementsToPush);

            testSetFactor.addAll(keysListToPush);

            effectiveLoadFactor = testSetFactor.size() / (double) testSetFactor.keys.length;
        }

        final double loadFactorError = Math.abs(this.loadFactor - effectiveLoadFactor);

        //At that point, this.keysListToPush is a sequence of values that will
        //be able to fill this.impl up to its targeted this.size and its DEFAULT_LOAD_FACTOR.
        this.pushedKeys = keysListToPush.toArray();

        System.out.println(String.format("Target size = %d, Pushed keys = %d ==>  Expected final size = %d, HPPC effective load factor = %f",
                this.targetSize,
                this.pushedKeys.length,
                dryRunHashSet.size(),
                effectiveLoadFactor));

        //Check
        if (loadFactorError > BenchmarkHashMapBase.HPPC_LOAD_FACTOR_ABSOLUTE_ERROR) {

            throw new RuntimeException("Wrong target fill factor reached, != " + this.loadFactor);
        }
    }

    /**
     * Call this to skip execution of some benchmarks categories
     */
    protected void skipForbiddenCombinations() {

        //FIXME: Only executing HPPC tests, don't forget to re-enable
        if (!this.implementation.toString().contains("HPPC")) {

            throw new DoNotExecuteBenchmarkException();
        }
        
        //1-1)skip senseless benchmark combinations : BAD hash is only valid for some types
        if (this.hash_quality == HASH_QUALITY.BAD &&
                !this.implementation.isHashQualityApplicable()) {

            throw new DoNotExecuteBenchmarkException();
        }

        //1-2)skip senseless benchmark combinations 2 : if Distribution is irrelevant, use only the Random one !
        if (!this.implementation.isDistributionApplicable() && this.distribution != Distribution.RANDOM) {
            throw new DoNotExecuteBenchmarkException();
        }

        //1-3) skip HIGHBITS + BAD hash combinations, too long to execute:
        if (this.hash_quality == HASH_QUALITY.BAD &&
                this.distribution == Distribution.HIGHBITS) {

            throw new DoNotExecuteBenchmarkException();
        }

        //1-4) skip RANDOM + BAD hash, since RANDOM is full Random,
        //setting BAD is still random anyway...
        if (this.hash_quality == HASH_QUALITY.BAD &&
                this.distribution == Distribution.RANDOM) {

            throw new DoNotExecuteBenchmarkException();
        }
    }
}