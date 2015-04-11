package com.carrotsearch.hppcrt.generator;

import org.junit.Test;

public class TestTemplateOptions
{
    @Test
    public void testInlineKType() {

        final TemplateOptions testInstance = new TemplateOptions(Type.GENERIC, null);

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "alloc[slot] != -1");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "Intrinsics.equalsKTypeDefault(keys[slot])");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "Intrinsics.equalsKTypeDefault(keys[slot]= keys / slot + alloc)");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "alloc[slot]");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "slot[alloc]");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());

        testInstance.inlineKType("is_allocated",
                "(alloc, slot, keys)",
                "slot[slot[keys[keys[alloc]]]]");

        System.out.println(testInstance.inlineKTypeDefinitions.toString());
    }

}
