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
/**
 * Unit tests for {@link KTypeLinkedList as KTypeIndexedContainer}.
 */
//${TemplateOptions.doNotGenerateKType("BOOLEAN")}
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
            int count = 0;
            //check access by get()
            for (/*! #if ($TemplateOptions.KTypeGeneric) !*/final Object
                    /*! #else
            final KType
            #end !*/
                    val : this.list.toArray()) {

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                TestUtils.assertEquals2(val, (Object) this.list.get(count));
                /*! #else
                TestUtils.assertEquals2(val, this.list.get(count));
                #end !*/
                count++;
            }

            Assert.assertEquals(count, this.list.size());

            //check beyond validity range

            //real data starts after first 2 placeholders
            for (int i = this.list.size() + 2; i < getBuffer(this.list).length; i++) {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> empty() == getBuffer(this.list)[i]);
                /*! #end !*/
            }
        }
    }

    /* */
    @Override
    @Test
    public void testIterable()
    {
        for (final int val : this.sequence) {

            this.list.add(cast(val));
        }

        int count = 0;

        final Integer[] seqBuffer = this.sequence.toArray(new Integer[this.sequence.size()]);

        for (final KTypeCursor<KType> cursor : this.list)
        {
            TestUtils.assertEquals2((int) (seqBuffer[count]), castType(cursor.value));
            //linked list: index in cursor matches index of get() method, NOT internal buffer !
            TestUtils.assertEquals2(this.list.get(cursor.index), cursor.value);
            count++;

        }
        Assert.assertEquals(count, this.list.size());
        Assert.assertEquals(count, this.sequence.size());

        count = 0;
        this.list.clear();
        for (@SuppressWarnings("unused")
        final KTypeCursor<KType> cursor : this.list)
        {
            count++;
        }
        Assert.assertEquals(0, count);
    }
}
