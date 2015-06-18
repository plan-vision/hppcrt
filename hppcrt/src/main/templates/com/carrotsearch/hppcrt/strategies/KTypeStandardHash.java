package com.carrotsearch.hppcrt.strategies;

import com.carrotsearch.hppcrt.Intrinsics;
import com.carrotsearch.hppcrt.hash.BitMixer;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/*! ${TemplateOptions.doNotGenerateKType("boolean", "byte", "char", "short", "float", "double" )} !*/
/**
 * Standard  {@link KTypeHashingStrategy} for <code>KType</code>s, providing the same behavior as equals()/ hashCode()
 * gives for objects.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class KTypeStandardHash<KType> implements KTypeHashingStrategy<KType>
{

    public KTypeStandardHash() {
        // nothing
    }

    @Override
    public int computeHashCode(final KType object) {

        return BitMixer.mix(object);
    }

    @Override
    public boolean equals(final KType o1, final KType o2) {

        return Intrinsics.<KType> equals(o1, o2);
    }

    @Override
    public boolean equals(final Object o) {

        if (o instanceof KTypeStandardHash<?>) {

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {

        return System.identityHashCode(KTypeStandardHash.class);
    }
}
