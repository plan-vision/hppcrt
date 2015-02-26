package com.carrotsearch.hppcrt.strategies;

/*! ${TemplateOptions.doNotGenerateKType("BOOLEAN")} !*/
/**
 * Interface to support custom hashing strategies in Hash containers using <code>KType</code>s
 * keys, as replacement of the original equals() and hashCode() methods.
 * 
 * @param <T>
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeHashingStrategy<KType>
{

    /**
     * Compute the hash code for the specific object.
     * #if ($TemplateOptions.KTypeGeneric)
     * Handling of <code>null</code> objects is up to the implementation.
     * #end
     * @param object
     * @return the computed hash value.
     */
    int computeHashCode(KType object);

    /**
     * Compares the Object o1 and o2 for equality.
     * #if ($TemplateOptions.KTypeGeneric)
     * Handling of <code>null</code> objects is up to the implementation.
     * #end
     * @param o1
     * @param o2
     * @return true for equality.
     */
    boolean equals(KType o1, KType o2);
}
