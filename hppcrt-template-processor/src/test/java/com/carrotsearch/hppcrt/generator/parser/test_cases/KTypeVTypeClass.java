package com.carrotsearch.hppcrt.generator.parser.test_cases;

import com.carrotsearch.hppcrt.generator.parser.test_cases.subpackage.KTypeVTypeInterfaceImported;

/**
 * A list of KTypes.
 */
public class KTypeVTypeClass<KType, VType>
extends KTypeVTypeSuper<KType, VType>
implements KTypeVTypeInterface<KType, VType>,
KTypeVTypeInterfaceImported<KType, VType>,
Cloneable
{
    KType[] keys;

    public KTypeVTypeClass() {
    }

    public KTypeVTypeClass(final KType[] foo, final VType boo) {
    }

    public KTypeVTypeClass(final KTypeVTypeSuper<KType, VType> foo) {
    }

    public VType foo(final KType key, final VType value) {
        throw new RuntimeException();
    }

    public void foo(final KTypeVTypeSuper<? extends KType, ? extends VType> foo) {
    }
}

class KTypeVTypeSuper<KType, VType>
{
}

interface KTypeVTypeInterface<KType, VType>
{
}