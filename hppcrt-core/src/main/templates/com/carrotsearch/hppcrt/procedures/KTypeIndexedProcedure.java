package com.carrotsearch.hppcrt.procedures;

/**
 * A procedure that applies to indexed <code>KType</code> objects.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeIndexedProcedure<KType>
{
    void apply(int index, KType value);
}
