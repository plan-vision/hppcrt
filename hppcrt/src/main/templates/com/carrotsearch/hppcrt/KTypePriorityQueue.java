package com.carrotsearch.hppcrt;


/**
 * A Priority queue of <code>KType</code>s.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public interface KTypePriorityQueue<KType> extends KTypeCollection<KType>
{
    /**
     * Add a <code>k</code> element in the priority queue
     * @param k
     */
    void add(KType k);

    /**
     * Retrieve, but not remove, the top element of the queue,
     * i.e. the min element with respect to the comparison criteria
     * (implementation defined)
     * of the queue. Returns the default value if empty.
     */
    KType top();

    /**
     * Retrieve, and remove the top element of the queue,
     * i.e. the min element with respect to the comparison criteria
     * (implementation defined) Returns the default value if empty.
     */
    KType popTop();

    /**
     * Update priorities of all the elements of the queue, to re-establish the correct priorities
     * towards the comparison criteria.
     */
    void updatePriorities();

    /**
     * Update the priority of the {@link #top()} element, to re-establish its actual priority
     * towards the comparison criteria when it may have changed such that it is no longer the
     *  min element with respect to the comparison criteria.
     */
    void updateTopPriority();

    /**
     * Returns the "default value" value used
     * in methods returning "default value"
     */
    KType getDefaultValue();

    /**
     * Set the "default value" value to be used
     * in methods returning "default value"
     */
    void setDefaultValue(final KType defaultValue);

}
