package com.carrotsearch.hppcrt;

/**
 * This class represents a specialized {@link ObjectPool} of Iterators of type
 * ITERATOR_TYPE, which are {@link AbstractIterator}s for OBJECT_TYPE objects.
 * All Iterator pools have the same allocation scheme :
 * 
 * <pre>
 * - Initial size is given either by global {@link #configureInitialPoolSize(int)} call,
 *   by system property "HPPC_ITERATOR_POOLSIZE",
 *   or by default, the number of processors.
 * - Grows by chunks of initial size,
 * - When its size gets bigger than MAX_SIZE_GROWTH_FACTOR * initial size,
 *   capacity remains constant while 'initial size'  old objects are disposed of.
 * @param <ITERATOR_TYPE>
 * @param <OBJECT_TYPE>
 */
public class IteratorPool<OBJECT_TYPE, ITERATOR_TYPE extends AbstractIterator<OBJECT_TYPE>> extends ObjectPool<ITERATOR_TYPE>
{
    /**
     * An Iterator pool never get bigger than MAX_SIZE_GROWTH_FACTOR times its
     * original size.
     */
    public static final int MAX_SIZE_GROWTH_FACTOR = 4;
    public static final String POOL_INITIAL_SIZE_PROPERTY = "HPPC_ITERATOR_POOLSIZE";

    private static int INITIAL_SIZE;

    static
    {
        try
        {
            //read a configurable poperty to limit the number of preallocated iterators per pool.
            IteratorPool.INITIAL_SIZE = Integer.parseInt(System.getProperty(IteratorPool.POOL_INITIAL_SIZE_PROPERTY));

            if (IteratorPool.INITIAL_SIZE < 1)
            {
                IteratorPool.INITIAL_SIZE = 1;
            }
        } catch (final Exception e)
        {
            IteratorPool.INITIAL_SIZE = Containers.NB_OF_PROCESSORS;
        }
    } //end static initializer

    private static int LINEAR_GROWTH_SIZE = IteratorPool.INITIAL_SIZE;
    private static int MAX_SIZE = IteratorPool.MAX_SIZE_GROWTH_FACTOR * IteratorPool.LINEAR_GROWTH_SIZE;
    private static int DISCARDING_SIZE = IteratorPool.LINEAR_GROWTH_SIZE;

    public IteratorPool(final ObjectFactory<ITERATOR_TYPE> objFactory) {
        super(objFactory, IteratorPool.INITIAL_SIZE, new ArraySizingStrategy() {

            @Override
            public int grow(final int currentBufferLength, final int elementsCount, final int expectedAdditions) {

                // Add at most Internals.NB_OF_PROCESSORS + expected new iterator instances
                int newSize = Math.max(elementsCount, currentBufferLength) + IteratorPool.LINEAR_GROWTH_SIZE
                        + expectedAdditions;

                if (newSize > IteratorPool.MAX_SIZE) {
                    //discard NB_OF_PROCESSORS objects
                    newSize = -1 * IteratorPool.DISCARDING_SIZE;
                }

                return newSize;
            }
        });
    }

    /**
     * Customized borrow(), that properly attach the borrowed iterator to its
     * corresponding pool and resets it to allow iteration as if the iterator was
     * created for the first time.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ITERATOR_TYPE borrow() {

        //extract an initialized object, never null by construction
        final ITERATOR_TYPE newObject = super.borrow();

        //attach instance to pool
        newObject.setPool((IteratorPool) this);

        //properly reset state of the Iterator for a new iteration
        newObject.resetState();

        //flag this iterator as borrowed, now
        newObject.setBorrowed();

        return newObject;
    }

    /**
     * Configure the initial pool size for all iterators, for all containers.
     * This is primarily destined to be used at application startup.
     * However, calling it at runtime on new and existing containers results
     * a smooth capacity adaptation.
     * @param initialSize
     */
    public static final void configureInitialPoolSize(final int initialSize) {
        IteratorPool.INITIAL_SIZE = initialSize;
        IteratorPool.LINEAR_GROWTH_SIZE = IteratorPool.INITIAL_SIZE;
        IteratorPool.DISCARDING_SIZE = IteratorPool.INITIAL_SIZE;
        IteratorPool.MAX_SIZE = IteratorPool.MAX_SIZE_GROWTH_FACTOR * IteratorPool.LINEAR_GROWTH_SIZE;
    }

    /**
     * Returns the expected max size all iterator pools will be limited to.
     */
    public static final int getMaxPoolSize() {
        return IteratorPool.MAX_SIZE;
    }
}
