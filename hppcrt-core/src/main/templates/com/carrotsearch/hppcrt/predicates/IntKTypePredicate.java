package com.carrotsearch.hppcrt.predicates;

/**
 * A predicate that applies to <code>int</code>, <code>KType</code> pairs.
 * This is indeed a placeholder for template compilation,
 * and will indeed be replaced by a (int, VType) instantiation
 * of KTypeVTypePredicate
 */
/*! ($TemplateOptions.doNotGenerate()) !*/
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface IntKTypePredicate<KType>
{
    boolean apply(int key, KType value);
}
