package com.carrotsearch.hppcrt.implementations;

/**
 * 
 */
public enum Implementations
{
    HPPC_INT_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HppcIntIntMap(size, loadFactor);
        }
    },

    FASTUTIL_INT_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new FastUtilIntIntMap(size, loadFactor);
        }
    },

    KOLOBOKE_INT_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new KolobokeIntIntMap(size, loadFactor);
        }
    },

    GS_INT_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new GsIntIntMap(size, loadFactor);
        }
    },

    MAHOUT_INT_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new MahoutIntIntMap(size, loadFactor);
        }
    },

    TROVE_INT_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new TroveIntIntMap(size, loadFactor);
        }
    },

    JAVOLUTION_INT_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new JavolutionIntIntMap(size, loadFactor);
        }
    },

    JAVA_INT_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new JavaIntIntMap(size, loadFactor);
        }
    },

    HPPC_OBJ_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HppcObjectIntMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    HPPC_OBJ_INT_STRATEGY
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HppcObjectIntCustomMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    FASTUTIL_OBJ_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new FastUtilObjectIntMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    KOLOBOKE_OBJ_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new KolobokeObjectIntMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    GS_OBJ_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new GsObjectIntMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    MAHOUT_OBJ_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new MahoutObjectIntMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    TROVE_OBJ_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new TroveObjectIntMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    HPPC_IDENTITY_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HppcIdentityIntMap(size, loadFactor);
        }

        /**
         * Distribution is irrelevant to Object identity (or so we assume...)
         */
        @Override
        public boolean isDistributionApplicable() {

            return false;
        }
    },

    FASTUTIL_IDENTITY_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new FastUtilIdentityIntMap(size, loadFactor);
        }

        /**
         * Distribution is irrelevant to Object identity (or so we assume...)
         */
        @Override
        public boolean isDistributionApplicable() {

            return false;
        }
    },

    KOLOBOKE_IDENTITY_INT
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new KolobokeIdentityIntMap(size, loadFactor);
        }

        /**
         * Distribution is irrelevant to Object identity (or so we assume...)
         */
        @Override
        public boolean isDistributionApplicable() {

            return false;
        }
    };

    /////////////////////////////////////////
    /////////////////////////////////////////

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