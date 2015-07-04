package com.carrotsearch.hppcrt;

import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;

public class BufferAllocationExceptionTest extends RandomizedTest
{
    @Test
    public void testWrongMessageFormat()
    {
        try {
            try {

                throw new OutOfMemoryError("Simulate an OMM");
            } catch (final OutOfMemoryError oom) {
                // Expected OOM, re-dispatched into BufferAllocationException
                throw new BufferAllocationException("Wrong format = Cause of the OOM => %d", oom, 0.42); //expect a Integer, give a float !
            }
        } catch (final BufferAllocationException bae) {

            bae.printStackTrace(System.err);
        }
    }

    @Test
    public void testMessageFormatOK()
    {
        try {
            try {

                throw new OutOfMemoryError("Simulate an OMM");
            } catch (final OutOfMemoryError oom) {
                // Expected OOM, re-dispatched into BufferAllocationException
                throw new BufferAllocationException("OK format = Cause of the OOM => %d", oom, 42); //expect a Integer, give an Integer !
            }
        } catch (final BufferAllocationException bae) {

            bae.printStackTrace(System.err);
        }
    }
}
