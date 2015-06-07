package com.carrotsearch.hppcrt.lists;

import java.util.*;

import org.junit.*;

import static com.carrotsearch.hppcrt.TestUtils.*;
import static org.junit.Assert.*;

import com.carrotsearch.hppcrt.*;
import com.carrotsearch.hppcrt.cursors.*;
import com.carrotsearch.hppcrt.predicates.*;
import com.carrotsearch.hppcrt.procedures.*;
import com.carrotsearch.hppcrt.sets.*;
import com.carrotsearch.hppcrt.sorting.*;
import com.carrotsearch.hppcrt.strategies.*;
import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
// ${TemplateOptions.doNotGenerateKType("BOOLEAN")}
/**
 * Unit tests for {@link KTypeLinkedList as KTypeIndexedConatainer}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeLinkedListAsIndexedContainerTest<KType> extends AbstractKTypeIndexedContainerTest<KType>
{

    @Override
    protected KTypeIndexedContainer<KType> createNewInstance(final int initialCapacity) {

        return new KTypeLinkedList<KType>(initialCapacity);
    }

    @Override
    protected KType[] getBuffer(final KTypeIndexedContainer<KType> testList) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        return Intrinsics.<KType[]> cast(concreteClass.buffer);
    }

    @Override
    protected KTypeIndexedContainer<KType> getClone(final KTypeIndexedContainer<KType> testList) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        return concreteClass.clone();
    }

    @Override
    protected KTypeIndexedContainer<KType> getFrom(final KTypeContainer<KType> container) {

        return KTypeLinkedList.from(container);
    }

    @Override
    protected KTypeIndexedContainer<KType> getFrom(final KType... elements) {

        return KTypeLinkedList.from(elements);
    }

    @Override
    protected KTypeIndexedContainer<KType> getFromArray(final KType[] keys) {

        return KTypeLinkedList.from(keys);
    }

    @Override
    protected void addFromArray(final KTypeIndexedContainer<KType> testList, final KType... keys) {

        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        concreteClass.add(keys);
    }

    @Override
    protected KTypeIndexedContainer<KType> getCopyConstructor(final KTypeIndexedContainer<KType> testList) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        return new KTypeLinkedList<KType>(concreteClass);
    }

    @Override
    int getValuePoolSize(final KTypeIndexedContainer<KType> testList) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        return concreteClass.valueIteratorPool.size();
    }

    @Override
    int getValuePoolCapacity(final KTypeIndexedContainer<KType> testList) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        return concreteClass.valueIteratorPool.capacity();
    }

    //////////////////////////////////////
    /// Implementation-specific tests for LinkedLists
    /////////////////////////////////////

    //specific override
    @Override
    @After
    public void checkConsistency()
    {
        if (this.list != null)
        {
            //real data starts after first 2 placeholders
            for (int i = this.list.size() + 2; i < getBuffer(this.list).length; i++) {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> empty() == getBuffer(this.list)[i]);
                /*! #end !*/
            }
        }
    }
}
