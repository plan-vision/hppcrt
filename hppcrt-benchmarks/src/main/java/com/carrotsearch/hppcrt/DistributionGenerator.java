package com.carrotsearch.hppcrt;

import java.util.Arrays;
import java.util.Random;

import com.carrotsearch.hppcrt.lists.IntArrayList;

public class DistributionGenerator
{
    public abstract class Generator
    {
        public int[] prepare(final int size)
        {
            final int[] t = new int[size];

            for (int i = 0; i < size; i++)
            {
                t[i] = getNext();
            }
            return t;
        }

        public abstract int getNext();
    }

    private final Random prng;
    private final int targetSize;
    private final long initValue;

    //////////////////////////////////
    //Generator kinds, enum-like patterns
    /////////////////////////////////

    /**
     * Generate Random numbers between [initValue; initValue + targetSize]
     */
    public final Generator RANDOM;

    /**
     * Generate LINEAR increasing numbers by steps of 3,  between [initValue; initValue + ( 3* targetSize)]
     * so a set of roughly targetSize values
     */
    public final Generator LINEAR;

    /**
     * Generate increasing numbers by uniformly random steps of [1,2,3],  between [initValue; initValue + ( 3* targetSize)]
     * making a set of roughly targetSize values
     */
    public final Generator RAND_INCREMENT;

    /**
     * Generate LINEAR decreasing numbers by steps of 3,  between [ initValue + ( 3* targetSize) ; initValue]
     * so a set of roughly targetSize values
     */
    public final Generator LINEAR_DECREMENT;

    /**
     * Generate decreasing numbers by uniformly random steps of [1,2,3],  between [initValue; initValue + ( 3* targetSize)]
     * making a set of roughly targetSize values
     */
    public final Generator RAND_DECREMENT;

    /**
     * Linear increments on 12 high bits first, then on lower bits.
     * (target size is N/A)
     */
    public final Generator HIGHBITS;

    /**
     * List of generators
     * @param args
     */
    public final Generator[] GENERATORS;

    /**
     * Constructor
     * @param targetSize
     * @param randomGene
     */
    public DistributionGenerator(final long initialValue, final int targetSize, final Random randomGene)
    {
        this.targetSize = targetSize;
        this.prng = randomGene;
        this.initValue = initialValue;

        //We must construct the generators here after  DistributionGenerator init:
        this.RANDOM = new Generator() {

            long counter = 0;

            @Override
            public int getNext()
            {
                this.counter++;
                return (int) (DistributionGenerator.this.prng.nextInt(DistributionGenerator.this.targetSize) + DistributionGenerator.this.initValue);
            }

            @Override
            public String toString() {

                return "Generator(RANDOM, counter = " + this.counter + ")";
            }
        };

        this.LINEAR = new Generator() {

            long counter = 0;

            @Override
            public int getNext()
            {
                final int value = (int) (DistributionGenerator.this.initValue + (3 * this.counter));

                this.counter = (this.counter + 1) % DistributionGenerator.this.targetSize;

                return value;
            }

            @Override
            public String toString() {

                return "Generator(LINEAR, counter = " + this.counter + ")";
            }
        };

        this.RAND_INCREMENT = new Generator() {

            long counter = 0;
            long startValue = DistributionGenerator.this.initValue;

            @Override
            public int getNext()
            {
                this.startValue += DistributionGenerator.this.prng.nextInt(3) + 1;

                if (this.counter >= DistributionGenerator.this.targetSize) {
                    this.counter = 0;
                    this.startValue = DistributionGenerator.this.initValue;
                }
                else {
                    this.counter++;
                }

                return (int) this.startValue;
            }

            @Override
            public String toString() {

                return "Generator(RAND_INCREMENT, counter = " + this.counter + ")";
            }
        };

        this.LINEAR_DECREMENT = new Generator() {

            long counter = 0;

            long startValue = DistributionGenerator.this.initValue + (3 * DistributionGenerator.this.targetSize);

            @Override
            public int getNext()
            {
                final int value = (int) (this.startValue - 3 * this.counter);

                this.counter = (this.counter + 1) % DistributionGenerator.this.targetSize;

                return value;
            }

            @Override
            public String toString() {

                return "Generator(LINEAR_DECREMENT, counter = " + this.counter + ")";
            }
        };

        this.RAND_DECREMENT = new Generator() {

            long counter = 0;

            long startValue = DistributionGenerator.this.initValue + (3 * DistributionGenerator.this.targetSize);

            @Override
            public int getNext()
            {
                this.startValue -= (DistributionGenerator.this.prng.nextInt(3) + 1);

                if (this.counter >= DistributionGenerator.this.targetSize) {
                    this.counter = 0;
                    this.startValue = DistributionGenerator.this.initValue + (3 * DistributionGenerator.this.targetSize);
                }
                else {
                    this.counter++;
                }

                return (int) this.startValue;
            }

            @Override
            public String toString() {

                return "Generator(RAND_DECREMENT, counter = " + this.counter + ")";
            }
        };

        this.HIGHBITS = new Generator() {

            long counter = 0;

            @Override
            public int getNext()
            {
                final long value = (this.counter << (32 - 12)) | (this.counter >>> 12);
                this.counter++;

                return (int) value;
            }

            @Override
            public String toString() {

                return "Generator(HIGHBITS, counter = " + this.counter + ")";
            }
        };

        this.GENERATORS = new Generator[] { this.RANDOM, this.LINEAR, this.RAND_INCREMENT, this.LINEAR_DECREMENT, this.RAND_DECREMENT, this.HIGHBITS };
    }

    private static void testGenerators(final long initValue, final int size, final long prngSeed, final int nbValuesToGenerate) {

        System.out.println("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(String.format(">>>> Testing DistributionGenerator : initValue=%d, targetSize=%d, Random seed =%d", initValue, size, prngSeed));
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        final DistributionGenerator testDistrib = new DistributionGenerator(initValue, size, new Random(prngSeed));
        final DistributionGenerator testDistribPrepare = new DistributionGenerator(initValue, size, new Random(prngSeed));

        for (int jj = 0; jj < testDistrib.GENERATORS.length; jj++) {

            final Generator gene = testDistrib.GENERATORS[jj];

            System.out.println(">>>> Test generator :" + gene.toString());

            final IntArrayList generatedValues = new IntArrayList();

            for (int ii = 0; ii < nbValuesToGenerate; ii++) {

                generatedValues.add(gene.getNext());
            }

            System.out.println(">>>> Test iterated getNext(), size = " + generatedValues.size() + " :\n" + generatedValues.toString());

            //test bulk:
            final int[] prepared = testDistribPrepare.GENERATORS[jj].prepare(nbValuesToGenerate);
            System.out.println(">>>> Test bulk prepare(), size = " + prepared.length + " :\n" + Arrays.toString(prepared));
            System.out.println("");

        } //end for
    }

    /////////////////////////////////
    //main for tests
    /////////////////////////////////

    public static void main(final String[] args) {

        DistributionGenerator.testGenerators(-100, 200, 1234789L, 500);
        DistributionGenerator.testGenerators((long) (Integer.MIN_VALUE * 0.5), Integer.MAX_VALUE - 10, 79914664L, 500);

    }
}
