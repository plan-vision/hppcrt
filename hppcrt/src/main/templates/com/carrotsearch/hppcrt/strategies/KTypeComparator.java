package com.carrotsearch.hppcrt.strategies;

import java.util.Comparator;

/**
 * Interface to support custom comparisons of <code>KType</code>s,
 * as replacement of either natural ordering or Comparable objects.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypeComparator<KType> /*! #if ($TemplateOptions.KTypeGeneric) !*/extends Comparator<KType> /*! #end !*/
{
    /**
     * Defines the relative ordering of e1 and e2:
     * @return 0 if e1 is "equal" to e2, or else < 0 if e1 is "smaller" than e2, or else  > 0 if e1 is "bigger" than e2
     */
    /*! #if ($TemplateOptions.KTypeGeneric) !*/
    @Override
    /*! #end !*/
    int compare(KType e1, KType e2);
}