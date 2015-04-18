package com.carrotsearch.hppcrt;

import com.carrotsearch.hppcrt.KTypeCollection;

/**
 * A set of <code>KType</code>s.
 */
// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeSet<KType> extends KTypeCollection<KType>
{
    /**
     * Adds <code>k</code> to the set.
     * 
     * @return Returns <code>true</code> if this element was not part of the set before. Returns
     * <code>false</code> if an equal element is already part of the set, <b>and leaves the set unchanged. </b>.
     */
    public boolean add(KType k);
}
