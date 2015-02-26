package com.carrotsearch.hppcrt.strategies;

/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN","BYTE","CHAR","SHORT","INT","LONG","FLOAT", "DOUBLE")} !*/
/**
 * Standard  {@link KTypeHashingStrategy} providing an 'identity'
 * behavior for objects, where they are compared by reference, and their
 * hashCode() is the 'native' one obtained by  {@link System#identityHashCode(Object)}
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public final class KTypeIdentityHash<KType> implements KTypeHashingStrategy<KType>
{

    public KTypeIdentityHash() {
        // nothing
    }

    @Override
    public int computeHashCode(final KType object) {

        return System.identityHashCode(object);
    }

    @Override
    public boolean equals(final KType o1, final KType o2) {

        return o1 == o2;
    }

    @Override
    public boolean equals(final Object o) {

        if (o instanceof KTypeIdentityHash<?>) {

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {

        return System.identityHashCode(KTypeIdentityHash.class);
    }
}
