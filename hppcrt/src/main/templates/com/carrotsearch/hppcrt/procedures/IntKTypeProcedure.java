package com.carrotsearch.hppcrt.procedures;

/**
 * A procedure that applies to <code>int</code>, <code>KType</code> pairs.
 * This is indeed a placeholder for template compilation,
 * and will indeed be replaced by a (int, VType) instantiation
 * of KTypeVTypeProcedure
 */
/*! ($TemplateOptions.doNotGenerate()) !*/
public interface IntKTypeProcedure<KType>
{
    void apply(int key, KType value);
}
