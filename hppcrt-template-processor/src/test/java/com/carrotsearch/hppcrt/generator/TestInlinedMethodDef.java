package com.carrotsearch.hppcrt.generator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestInlinedMethodDef
{
    @Test
    public void testMethodPattern() {

        Matcher matcher = Pattern.compile("(Intrinsics.\\s*)(<[^>]+>\\s*)?(newKTypeArray)(\\()",
                Pattern.MULTILINE | Pattern.DOTALL).matcher("Intrinsics. <KType[]>newKTypeArray(toto)");

        matcher.find();

        System.out.println(matcher.toString());

        matcher = Pattern.compile("(Intrinsics.\\s*)(<[^>]+>\\s*)?(newKTypeArray)(\\()",
                Pattern.MULTILINE | Pattern.DOTALL).matcher("Intrinsics.<KType[]> newKTypeArray(size); \n");

        matcher.find();

        System.out.println(matcher.toString());
    }
}
