package com.carrotsearch.hppcrt.generator;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class TestInlinedMethodDef
{
    @Test
    public void testInlinedMethodReplacement() {

        ///////////
        /// Intrinsics.<U>equals(e1, e2)
        ///////////

        //A) Create
        InlinedMethodDef testClass = new InlinedMethodDef("Intrinsics.<U>equals(e1, e2)");

        System.out.println(testClass);

        //B) add specializations
        testClass.addSpecialization("<int>==> e1 == e2");
        testClass.addSpecialization("<Object>==> e1.equals(e2)");
        testClass.addSpecialization("<float>==> Float.compare(e1 , e2) == 0");

        //C) try to match it as TemplateProcessor is supposed to:
        Pattern p = testClass.getMethodNameCompiledPattern();

        Matcher m = p.matcher("Intrinsics.<KType> equals(A1 + A2, B1 + B2)");

        boolean result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("Intrinsics", m.group("className"));
        Assert.assertEquals("equals(", m.group("method")); //the first parenthesis is matched also on purspose
        Assert.assertEquals("KType", m.group("generic"));

        //D) Compute inlined replacements
        //= int
        String intReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.INT, Type.FLOAT),
                m.group("generic").split(","),
                Arrays.asList("A1 + A2", "B1 + B2"));

        Assert.assertEquals("((A1 + A2) == (B1 + B2))".replaceAll("\\s*", ""), intReplacement.replaceAll("\\s*", ""));

        //= Float
        intReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.FLOAT, null),
                m.group("generic").split(","),
                Arrays.asList("A1 + A2", "B1 + B2"));

        Assert.assertEquals("(Float.compare((A1 + A2) , (B1 + B2)) == 0)".replaceAll("\\s*", ""), intReplacement.replaceAll("\\s*", ""));

        //= Object
        intReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.GENERIC, null),
                m.group("generic").split(","),
                Arrays.asList("A1 + A2", "B1 + B2"));

        Assert.assertEquals("((A1 + A2).equals((B1 + B2)))".replaceAll("\\s*", ""), intReplacement.replaceAll("\\s*", ""));

        ///////////
        /// REHASH(e1)
        ///////////

        //A) Create
        testClass = new InlinedMethodDef("REHASH(key)");

        System.out.println(testClass);

        //B) add specializations. There is a VType, so it must be declared explicitly or by using (*)
        //i.e matches the current VType.
        testClass.addSpecialization("<int,*>==> Murmurhash3.mix(key)");
        testClass.addSpecialization("<Object,*>==> key == null?0:Murmurhash3.mix(key.hashCode())");
        testClass.addSpecialization("<float,*>==> PhiMix.mix(Float.floatToRawInBits(key))");

        //C) try to match it as TemplateProcessor is supposed to:
        p = testClass.getMethodNameCompiledPattern();

        m = p.matcher("this.REHASH(A1 + A2)");

        result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("REHASH(", m.group("method")); //the first parenthesis is matched also on purspose

        //D) Compute inlined replacement
        intReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.INT, Type.FLOAT),
                new String[0],
                Arrays.asList("A1 + A2"));

        Assert.assertEquals("(Murmurhash3.mix((A1 + A2)))".replaceAll("\\s*", ""), intReplacement.replaceAll("\\s*", ""));

        ///////////
        /// REHASH(e1), double wildcard
        ///////////

        //A) Create
        testClass = new InlinedMethodDef("REHASH(key, perturb)");

        System.out.println(testClass);

        //B) add specializations. There is a VType, so it must be declared explicitly or by using (*)
        //i.e matches the current VType.
        testClass.addSpecialization("<*,*>==> Murmurhash3.mix(key ^ perturb)");

        //C) try to match it as TemplateProcessor is supposed to:
        p = testClass.getMethodNameCompiledPattern();

        m = p.matcher("this.REHASH(A1 + A2)");

        result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("REHASH(", m.group("method")); //the first parenthesis is matched also on purspose

        //D) Compute inlined replacement
        intReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.INT, Type.FLOAT),
                new String[0],
                Arrays.asList("A1 + A2", "pp"));

        Assert.assertEquals("(Murmurhash3.mix((A1 + A2) ^ (pp)))".replaceAll("\\s*", ""), intReplacement.replaceAll("\\s*", ""));

        ///////////
        /// Intrinsics.newArray(0) with a literal
        ///////////

        //A) Create
        testClass = new InlinedMethodDef("Intrinsics.<T>newArray(arraySize)");

        System.out.println(testClass);

        //B) add specializations. There is a VType, so it must be declared explicitly or by using (*)
        //i.e matches the current VType.
        testClass.addSpecialization("<Object>==> (T)new Object[arraySize]");
        testClass.addSpecialization("<byte>==> new byte[arraySize]");

        //C) try to match it as TemplateProcessor is supposed to:
        p = testClass.getMethodNameCompiledPattern();

        m = p.matcher("Intrinsics.<KType>newArray(SS)");

        result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("newArray(", m.group("method")); //the first parenthesis is matched also on purspose

        //D) Compute inlined replacement
        intReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.BYTE, Type.FLOAT),
                m.group("generic").split(","),
                Arrays.asList("0"));

        Assert.assertEquals("(new byte[(0)])".replaceAll("\\s*", ""), intReplacement.replaceAll("\\s*", ""));

        ///////////
        /// KTypeLinkedList.setLinkAfterNodeValue(nodeValue, newAfter) with wildcard only
        ///////////
        //A) Create
        testClass = new InlinedMethodDef("KTypeLinkedList.setLinkAfterNodeValue(nodeValue, newAfter)");

        System.out.println(testClass);

        //B) add specializations. There is a VType, so it must be declared explicitly or by using (*)
        //i.e matches the current VType.
        testClass.addSpecialization("<*>==> newAfter | (nodeValue & 0xFFFFFFFF00000000L)");

        //C) try to match it as TemplateProcessor is supposed to:
        p = testClass.getMethodNameCompiledPattern();

        m = p.matcher("KTypeLinkedList.setLinkAfterNodeValue(A , B)");

        result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("setLinkAfterNodeValue(", m.group("method")); //the first parenthesis is matched also on purspose

        //D) Compute inlined replacement
        intReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.INT, null),
                new String[0],
                Arrays.asList("A1", "B1"));

        Assert.assertEquals("((B1) | ((A1) & 0xFFFFFFFF00000000L))".replaceAll("\\s*", ""), intReplacement.replaceAll("\\s*", ""));

        intReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.GENERIC, null),
                new String[0],
                Arrays.asList("A2", "B2"));

        Assert.assertEquals("((B2) | ((A2) & 0xFFFFFFFF00000000L))".replaceAll("\\s*", ""), intReplacement.replaceAll("\\s*", ""));
    }

    @Test
    public void testInlinedMethodReplacementRecursive() {

        ///////////
        /// is_allocated(slot, keys)
        ///////////

        //A) Create
        InlinedMethodDef testClass = new InlinedMethodDef("is_allocated(slot, keys)");

        System.out.println(testClass);

        //B) add specializations
        testClass.addSpecialization("<*,*>==>!Intrinsics.<KType>isEmpty(keys[slot])");

        //C) try to match it as TemplateProcessor is supposed to:
        Pattern p = testClass.getMethodNameCompiledPattern();

        Matcher m = p.matcher("is_allocated(SLOT, KEYS)");

        boolean result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("is_allocated(", m.group("method")); //the first parenthesis is matched also on purspose

        //D) Compute inlined replacements
        //= Object
        String objReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.GENERIC, Type.GENERIC),
                new String[0],
                Arrays.asList("SS", "KK"));

        final String outerReplacement = "(!Intrinsics.<KType>isEmpty((KK)[(SS)]))";

        Assert.assertEquals(outerReplacement.replaceAll("\\s*", ""), objReplacement.replaceAll("\\s*", ""));

        ///////////
        /// Intrinsics.<KType>isEmpty(keys[slot])
        ///////////

        //A) Create
        testClass = new InlinedMethodDef("Intrinsics.<T>isEmpty(key)");

        System.out.println(testClass);

        //B) add specializations. There is a VType, so it must be declared explicitly or by using (*)
        //i.e matches the current VType.
        testClass.addSpecialization("<Object>==> key == null");
        testClass.addSpecialization("<float>==>Float.floatToIntBits(key) == 0");
        testClass.addSpecialization("<double>==>Double.doubleToLongBits(key) == 0L");
        testClass.addSpecialization("<*> ==> key == 0");

        //C) try to match it as TemplateProcessor is supposed to:
        p = testClass.getMethodNameCompiledPattern();

        //parse recursively the previous replacement
        m = p.matcher(outerReplacement);

        result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("isEmpty(", m.group("method")); //the first parenthesis is matched also on purspose

        //D) Compute inlined replacement
        objReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.GENERIC, Type.GENERIC),
                m.group("generic").split(","),
                Arrays.asList("(KK)[(SS)]"));

        Assert.assertEquals("(((KK)[(SS)]) == null)".replaceAll("\\s*", ""), objReplacement.replaceAll("\\s*", ""));

        objReplacement = testClass.computeInlinedForm(new TemplateOptions(Type.FLOAT, Type.GENERIC),
                m.group("generic").split(","),
                Arrays.asList("(KK)[(SS)]"));

        Assert.assertEquals("(Float.floatToIntBits(((KK)[(SS)])) == 0)".replaceAll("\\s*", ""), objReplacement.replaceAll("\\s*", ""));

    }

    @Test
    public void testCastSimpleReplacement() {

        ///////////
        /// Intrinsics.<T>cast(e) with type = KType
        ///////////

        //A) Create
        final InlinedMethodDef testClass = new InlinedMethodDef("Intrinsics.<T>cast(obj)");

        System.out.println(testClass);

        //B) add specializations
        testClass.addSpecialization("<Object>==> (T)obj");
        testClass.addSpecialization("<Object[]>==> (T)obj");
        testClass.addSpecialization("<*>==> obj");

        //C) try to match it as TemplateProcessor is supposed to:
        Pattern p = testClass.getMethodNameCompiledPattern();

        Matcher m = p.matcher("Intrinsics.<KType> cast(AA)");

        boolean result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("Intrinsics", m.group("className"));
        Assert.assertEquals("cast(", m.group("method")); //the first parenthesis is matched also on purspose
        Assert.assertEquals("KType", m.group("generic"));

        //D) Compute inlined replacements
        //= Object
        String replacement = testClass.computeInlinedForm(new TemplateOptions(Type.GENERIC, null),
                m.group("generic").split(","),
                Arrays.asList("AA"));

        Assert.assertEquals("((KType)(AA))".replaceAll("\\s*", ""), replacement.replaceAll("\\s*", ""));

        //= int
        replacement = testClass.computeInlinedForm(new TemplateOptions(Type.INT, null),
                m.group("generic").split(","),
                Arrays.asList("AA"));

        Assert.assertEquals("((AA))".replaceAll("\\s*", ""), replacement.replaceAll("\\s*", ""));

        ///////////
        /// Intrinsics.<T>cast(e) with T = VType
        ///////////

        //C) try to match it as TemplateProcessor is supposed to:
        p = testClass.getMethodNameCompiledPattern();

        m = p.matcher("Intrinsics.<VType> cast(AA)");

        result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("Intrinsics", m.group("className"));
        Assert.assertEquals("cast(", m.group("method")); //the first parenthesis is matched also on purspose
        Assert.assertEquals("VType", m.group("generic"));

        //D) Compute inlined replacements
        //= Object = VType
        replacement = testClass.computeInlinedForm(new TemplateOptions(Type.DOUBLE, Type.GENERIC),
                m.group("generic").split(","),
                Arrays.asList("AA"));

        Assert.assertEquals("((VType)(AA))".replaceAll("\\s*", ""), replacement.replaceAll("\\s*", ""));

        //= float = VType
        replacement = testClass.computeInlinedForm(new TemplateOptions(Type.GENERIC, Type.FLOAT),
                m.group("generic").split(","),
                Arrays.asList("AA"));

        Assert.assertEquals("((AA))".replaceAll("\\s*", ""), replacement.replaceAll("\\s*", ""));
    }

    @Test
    public void testCastArrayReplacement() {

        ///////////
        /// Intrinsics.<T>cast(e) with T = KType[]
        ///////////

        //A) Create
        final InlinedMethodDef testClass = new InlinedMethodDef("Intrinsics.<T>cast(obj)");

        System.out.println(testClass);

        //B) add specializations
        testClass.addSpecialization("<Object>==> (T)obj");
        testClass.addSpecialization("<Object[]>==> (T)obj");
        testClass.addSpecialization("<*>==> obj");

        //C) try to match it as TemplateProcessor is supposed to:
        Pattern p = testClass.getMethodNameCompiledPattern();

        Matcher m = p.matcher("Intrinsics.<KType[]> cast(this.keys)");

        boolean result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("Intrinsics", m.group("className"));
        Assert.assertEquals("cast(", m.group("method")); //the first parenthesis is matched also on purspose
        Assert.assertEquals("KType[]", m.group("generic"));

        //D) Compute inlined replacements
        //= Object
        String replacement = testClass.computeInlinedForm(new TemplateOptions(Type.GENERIC, null),
                m.group("generic").split(","),
                Arrays.asList("this.keys"));

        Assert.assertEquals("((KType[])(this.keys))".replaceAll("\\s*", ""), replacement.replaceAll("\\s*", ""));

        //= float
        replacement = testClass.computeInlinedForm(new TemplateOptions(Type.FLOAT, null),
                m.group("generic").split(","),
                Arrays.asList("this.keys"));

        Assert.assertEquals("((this.keys))".replaceAll("\\s*", ""), replacement.replaceAll("\\s*", ""));

        ///////////
        /// Intrinsics.<T>cast(e) with T = VType[]
        ///////////

        //C) try to match it as TemplateProcessor is supposed to:
        p = testClass.getMethodNameCompiledPattern();

        m = p.matcher("Intrinsics.<VType[]> cast(this.values)");

        result = m.find();

        Assert.assertTrue(result);

        Assert.assertEquals("Intrinsics", m.group("className"));
        Assert.assertEquals("cast(", m.group("method")); //the first parenthesis is matched also on purspose
        Assert.assertEquals("VType[]", m.group("generic"));

        //D) Compute inlined replacements
        //= Object = VType
        replacement = testClass.computeInlinedForm(new TemplateOptions(Type.INT, Type.GENERIC),
                m.group("generic").split(","),
                Arrays.asList("this.values"));

        Assert.assertEquals("((VType[])(this.values))".replaceAll("\\s*", ""), replacement.replaceAll("\\s*", ""));

        //= float = VType
        replacement = testClass.computeInlinedForm(new TemplateOptions(Type.CHAR, Type.FLOAT),
                m.group("generic").split(","),
                Arrays.asList("this.values"));

        Assert.assertEquals("((this.values))".replaceAll("\\s*", ""), replacement.replaceAll("\\s*", ""));
    }
}
