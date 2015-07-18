package com.carrotsearch.hppcrt;

import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carrotsearch.hppcrt.hash.MurmurHash3;

public final class Containers
{
    public final static int NB_OF_PROCESSORS = Runtime.getRuntime().availableProcessors();

    /**
     * The default number of expected elements for containers.
     * (power-of-two to accommodate special containers also)
     */
    public final static int DEFAULT_EXPECTED_ELEMENTS = 1 << 3;

    /**
     * External initial seed value. We do not care about multiple assignments so
     * not volatile.
     * 
     * @see #randomSeed64()
     */
    private static String testsSeedProperty;

    /**
     * Unique marker for {@link #testsSeedProperty}.
     */
    private final static String NOT_AVAILABLE = "";

    /**
     * No instances.
     */
    private Containers() {
        //nothing
    }

    /**
     * Provides a (possibly) random initial seed for randomized stuff.
     * 
     * If <code>tests.seed</code> property is available and accessible, the
     * returned value will be derived from the value of that property and will be
     * constant to ensure reproducibility in presence of the randomized testing
     * package.
     * 
     * @see "https://github.com/carrotsearch/randomizedtesting"
     */
    public static long randomSeed64() {

        if (Containers.testsSeedProperty == null) {
            try {
                Containers.testsSeedProperty = java.security.AccessController.doPrivileged(new PrivilegedAction<String>() {
                    @Override
                    public String run() {
                        return System.getProperty("tests.seed", Containers.NOT_AVAILABLE);
                    }
                });
            } catch (final SecurityException e) {
                // If failed on security exception, don't panic.
                Containers.testsSeedProperty = Containers.NOT_AVAILABLE;
                Logger.getLogger(Containers.class.getName()).log(Level.INFO,
                        "Failed to read 'tests.seed' property for initial random seed.", e);
            }
        }

        long initialSeed;
        if (!Containers.testsSeedProperty.equals(Containers.NOT_AVAILABLE)) {
            initialSeed = Containers.testsSeedProperty.hashCode();
        } else {
            // Mix something that is changing over time (nanoTime)
            // ... with something that is thread-local and relatively unique
            //     even for very short time-spans (new Object's address from a TLAB).
            initialSeed = System.nanoTime() ^ System.identityHashCode(new Object());
        }
        return MurmurHash3.mix64(initialSeed);
    }
}
