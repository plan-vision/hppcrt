package com.carrotsearch.hppcrt;

/**
 * 
 */
public enum Implementations
{
    HPPC
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HppcMap(size, loadFactor);
        }
    },

    HPPC_OBJECT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HppcObjectMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    HPPC_STRATEGY
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HppcCustomMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    HPPC_IDENTITY
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HppcIdentityMap(size, loadFactor);
        }

        /**
         * By default, Different distributions are to be used
         * (Template method)
         * @return
         */
        @Override
        public boolean isDistributionApplicable() {

            return false;
        }
    },

    FASTUTIL
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new FastUtilMap(size, loadFactor);
        }
    },

    FASTUTIL_OBJECT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new FastUtilObjectMap(size, loadFactor);
        }
    },

    MAHOUT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new MahoutMap(size, loadFactor);
        }
    },

    TROVE
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new TroveMap(size, loadFactor);
        }
    },

    JAVA
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new JavaMap(size, loadFactor);
        }
    };

    public abstract MapImplementation<?> getInstance(int size, float loadFactor);

    /**
     * By default, Hash quality tests are irrelevant
     * (Template method)
     * @return
     */
    public boolean isHashQualityApplicable() {

        return false;
    }

    /**
     * By default, Different distributions are to be used
     * (Template method)
     * @return
     */
    public boolean isDistributionApplicable() {

        return true;
    }
}