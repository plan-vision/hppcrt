package com.carrotsearch.hppcrt.implementations;

import com.carrotsearch.hppcrt.MapImplementation;

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

    FASTUTIL
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new FastUtilMap(size, loadFactor);
        }
    },

    KOLOBOKE
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HftcMap(size, loadFactor);
        }
    },

    GS
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new GsMap(size, loadFactor);
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
    },

    HPPC_OBJ
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

    HPPC_OBJ_STRATEGY
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

    FASTUTIL_OBJ
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new FastUtilObjectMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    KOLOBOKE_OBJ
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HftcObjectMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    GS_OBJ
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new GsObjectMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    MAHOUT_OBJ
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new MahoutObjectMap(size, loadFactor);
        }

        @Override
        public boolean isHashQualityApplicable() {

            return true;
        }
    },

    TROVE_OBJ
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new TroveObjectMap(size, loadFactor);
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
         * Distribution is irrelevant to Object identity (or so we assume...)
         */
        @Override
        public boolean isDistributionApplicable() {

            return false;
        }
    },

    FASTUTIL_IDENTITY
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new FastUtilIdentityMap(size, loadFactor);
        }

        /**
         * Distribution is irrelevant to Object identity (or so we assume...)
         */
        @Override
        public boolean isDistributionApplicable() {

            return false;
        }
    },

    KOLOBOKE_IDENTITY
    {
        @Override
        public MapImplementation<?> getInstance(final int size, final float loadFactor)
        {
            return new HftcIdentityMap(size, loadFactor);
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