package com.carrotsearch.hppcrt.lists;

import java.util.Iterator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.carrotsearch.hppcrt.Intrinsics;
import com.carrotsearch.hppcrt.KTypeContainer;
import com.carrotsearch.hppcrt.KTypeDeque;
import com.carrotsearch.hppcrt.TestUtils;
import com.carrotsearch.hppcrt.cursors.KTypeCursor;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit tests for {@link KTypeLinkedList as KTypeDeque}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeLinkedListAsDequeTest<KType> extends AbstractKTypeDequeTest<KType>
{
    @Override
    protected KTypeDeque<KType> createNewInstance(final int initialCapacity) {

        return new KTypeLinkedList<KType>(initialCapacity);
    }

    @Override
    protected void addFromArray(final KTypeDeque<KType> testList, final KType... keys) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        concreteClass.addLast(keys);

    }

    @Override
    protected void addFromContainer(final KTypeDeque<KType> testList, final KTypeContainer<KType> container) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        concreteClass.addLast(container);

    }

    @Override
    protected KType[] getBuffer(final KTypeDeque<KType> testList) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        return Intrinsics.<KType[]> cast(concreteClass.buffer);
    }

    @Override
    protected int getDescendingValuePoolSize(final KTypeDeque<KType> testList) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        return concreteClass.descendingValueIteratorPool.size();
    }

    @Override
    protected int getDescendingValuePoolCapacity(final KTypeDeque<KType> testList) {
        final KTypeLinkedList<KType> concreteClass = (KTypeLinkedList<KType>) (testList);
        return concreteClass.descendingValueIteratorPool.capacity();
    }

    //specific override
    @After
    public void checkConsistency()
    {
        final KTypeLinkedList<KType> linkedList = (KTypeLinkedList<KType>) this.deque;

        if (this.deque != null)
        {
            int count = 0;
            //check access by get()
            for (/*! #if ($TemplateOptions.KTypeGeneric) !*/final Object
                    /*! #else
            final KType
            #end !*/
                    val : linkedList.toArray()) {

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                TestUtils.assertEquals2(val, (Object) linkedList.get(count));
                /*! #else
                TestUtils.assertEquals2(val, linkedList.get(count));
                #end !*/
                count++;
            }

            Assert.assertEquals(count, linkedList.size());

            //check beyond validity range
            //real data starts after first 2 placeholders
            for (int i = this.deque.size() + 2; i < getBuffer(this.deque).length; i++) {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> empty() == getBuffer(this.deque)[i]);
                /*! #end !*/
            }
        }
    }

    /* */
    @Override
    @Test
    public void testDescendingIterable()
    {
        //alias
        final KTypeLinkedList<KType> linkedList = (KTypeLinkedList<KType>) this.deque;

        addFromContainer(this.deque, this.sequence);

        int index = this.sequence.size() - 1;
        for (final Iterator<KTypeCursor<KType>> i = this.deque.descendingIterator(); i.hasNext();)
        {
            final KTypeCursor<KType> cursor = i.next();
            TestUtils.assertEquals2(this.sequence.buffer[index], cursor.value);
            //linked list: index of cursor matches index of get(), NOT internal buffer !
            TestUtils.assertEquals2(linkedList.get(cursor.index), cursor.value);
            index--;
        }
        Assert.assertEquals(-1, index);

        this.deque.clear();
        Assert.assertFalse(this.deque.descendingIterator().hasNext());
    }
}
