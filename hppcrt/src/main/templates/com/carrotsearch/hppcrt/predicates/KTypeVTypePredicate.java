package com.carrotsearch.hppcrt.predicates;

/**
 * A predicate that applies to <code>KType</code>, <code>VType</code> pairs.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeVTypePredicate<KType, VType>
{
    boolean apply(KType key, VType value);
}
