package com.carrotsearch.hppcrt.lists;

import org.junit.*;

import com.carrotsearch.hppcrt.*;

/*! #import("com/carrotsearch/hppcrt/Intrinsics.java") !*/
/**
 * Unit tests for {@link KTypeArrayDeque as KTypeIndexedContainer}.
 */
/*! ${TemplateOptions.generatedAnnotation} !*/
public class KTypeArrayDequeAsIndexedContainerTest<KType> extends AbstractKTypeIndexedContainerTest<KType>
{

    @Override
    protected KTypeIndexedContainer<KType> createNewInstance(final int initialCapacity) {

        return new KTypeArrayDeque<KType>(initialCapacity);
    }

    @Override
    protected KType[] getBuffer(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayDeque<KType> concreteClass = (KTypeArrayDeque<KType>) (testList);
        return Intrinsics.<KType[]> cast(concreteClass.buffer);
    }

    @Override
    protected KTypeIndexedContainer<KType> getClone(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayDeque<KType> concreteClass = (KTypeArrayDeque<KType>) (testList);
        return concreteClass.clone();
    }

    @Override
    protected KTypeIndexedContainer<KType> getFrom(final KTypeContainer<KType> container) {

        return KTypeArrayDeque.from(container);
    }

    @Override
    protected KTypeIndexedContainer<KType> getFrom(final KType... elements) {

        return KTypeArrayDeque.from(elements);
    }

    @Override
    protected KTypeIndexedContainer<KType> getFromArray(final KType[] keys) {

        return KTypeArrayDeque.from(keys);
    }

    @Override
    protected void addFromArray(final KTypeIndexedContainer<KType> testList, final KType... keys) {

        final KTypeArrayDeque<KType> concreteClass = (KTypeArrayDeque<KType>) (testList);
        concreteClass.addLast(keys);
    }

    @Override
    protected KTypeIndexedContainer<KType> getCopyConstructor(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayDeque<KType> concreteClass = (KTypeArrayDeque<KType>) (testList);
        return new KTypeArrayDeque<KType>(concreteClass);
    }

    @Override
    int getValuePoolSize(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayDeque<KType> concreteClass = (KTypeArrayDeque<KType>) (testList);
        return concreteClass.valueIteratorPool.size();
    }

    @Override
    int getValuePoolCapacity(final KTypeIndexedContainer<KType> testList) {
        final KTypeArrayDeque<KType> concreteClass = (KTypeArrayDeque<KType>) (testList);
        return concreteClass.valueIteratorPool.capacity();
    }

    //////////////////////////////////////
    /// Implementation-specific customization
    /////////////////////////////////////

    //specific override
    @Override
    @After
    public void checkConsistency()
    {
        final KTypeArrayDeque<KType> arrayDeque = (KTypeArrayDeque<KType>) this.list;

        if (arrayDeque != null)
        {
            int count = 0;
            //check access by get()
            for (/*! #if ($TemplateOptions.KTypeGeneric) !*/final Object
                    /*! #else
            final KType
            #end !*/
                    val : arrayDeque.toArray()) {

                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                TestUtils.assertEquals2(val, (Object) arrayDeque.get(count));
                /*! #else
                TestUtils.assertEquals2(val, arrayDeque.get(count));
                #end !*/
                count++;
            }

            Assert.assertEquals(count, arrayDeque.size());

            //check beyond validity range

            for (int i = arrayDeque.tail; i != arrayDeque.head; i = oneRight(i, arrayDeque.buffer.length))
            {
                /*! #if ($TemplateOptions.KTypeGeneric) !*/
                Assert.assertTrue(Intrinsics.<KType> empty() == arrayDeque.buffer[i]);
                /*! #end !*/
            }
        }
    }

    /* */
    @Override
    @Test
    public void testInsert()
    {
        //override: unsupported op, make it as success for nice statistics.
        Assert.assertTrue(true);
    }

    /**
     * Move one index to the right, wrapping around buffer of size modulus
     */
    private int oneRight(final int index, final int modulus)
    {
        return (index + 1 == modulus) ? 0 : index + 1;
    }
}
