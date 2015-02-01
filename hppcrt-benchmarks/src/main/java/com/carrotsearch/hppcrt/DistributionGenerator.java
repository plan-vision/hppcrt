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
    private final int initValue;

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
     * Generate LINEAR decreasing numbers by steps of 3,  between [ initValue + ( 3* targetSize) ; initValue]
     * so a set of roughly targetSize values
     */
    public final Generator LINEAR_DECREMENT;

    /**
     * Linear increments on 12 high bits first, then on lower bits.
     * (target size is not used)
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
    public DistributionGenerator(final int initialValue, final int targetSize, final Random randomGene)
    {
        this.targetSize = targetSize;
        this.prng = randomGene;
        this.initValue = initialValue;

        //We must construct the generators here after  DistributionGenerator init:
        this.RANDOM = new Generator() {

            int counter = 0;

            @Override
            public int getNext()
            {
                this.counter++;
                return DistributionGenerator.this.prng.nextInt(DistributionGenerator.this.targetSize) + DistributionGenerator.this.initValue;
            }

            @Override
            public String toString() {

                return "Generator(RANDOM, counter = " + this.counter + ")";
            }
        };

        this.LINEAR = new Generator() {

            int counter = 0;

            @Override
            public int getNext()
            {
                final int value = DistributionGenerator.this.initValue + (3 * this.counter);

                this.counter = (this.counter + 1) % DistributionGenerator.this.targetSize;

                return value;
            }

            @Override
            public String toString() {

                return "Generator(LINEAR, counter = " + this.counter + ")";
            }
        };

        this.LINEAR_DECREMENT = new Generator() {

            int counter = 0;

            int startValue = DistributionGenerator.this.initValue + (3 * DistributionGenerator.this.targetSize);

            @Override
            public int getNext()
            {
                final int value = this.startValue - 3 * this.counter;

                this.counter = (this.counter + 1) % DistributionGenerator.this.targetSize;

                return value;
            }

            @Override
            public String toString() {

                return "Generator(LINEAR_DECREMENT, counter = " + this.counter + ")";
            }
        };

        this.HIGHBITS = new Generator() {

            int counter = 0;

            @Override
            public int getNext()
            {
                final int value = (this.counter << (32 - 12)) | (this.counter >>> 12);
                this.counter++;

                return value;
            }

            @Override
            public String toString() {

                return "Generator(HIGHBITS, counter = " + this.counter + ")";
            }
        };

        this.GENERATORS = new Generator[] { this.RANDOM, this.LINEAR, this.LINEAR_DECREMENT, this.HIGHBITS };
    }

    /////////////////////////////////
    //main for tests
    /////////////////////////////////

    public static void main(final String[] args) {

        final DistributionGenerator testDistrib = new DistributionGenerator(-100, 200, new XorShiftRandom(1234789));
        final DistributionGenerator testDistribPrepare = new DistributionGenerator(-100, 200, new XorShiftRandom(1234789));

        for (int jj = 0; jj < testDistrib.GENERATORS.length; jj++) {

            final Generator gene = testDistrib.GENERATORS[jj];

            System.out.println(">>>> Test generator :" + gene.toString());

            final IntArrayList generatedValues = new IntArrayList();

            for (int ii = 0; ii < 200; ii++) {

                generatedValues.add(gene.getNext());
            }

            System.out.println(">>>> Test iterated getNext(), size = " + generatedValues.size() + " :\n" + generatedValues.toString());

            //test bulk:
            final int[] prepared = testDistribPrepare.GENERATORS[jj].prepare(200);
            System.out.println(">>>> Test bulk prepare(), size = " + prepared.length + " :\n" + Arrays.toString(prepared));
            System.out.println("");

        } //end for
    }
}
