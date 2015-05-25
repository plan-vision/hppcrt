package com.carrotsearch.hppcrt.generator.parser;

import com.carrotsearch.hppcrt.generator.Type;
import com.google.common.base.Preconditions;

public final class TypeBound
{
    private final String originalBound;
    private final Type targetType;

    public TypeBound(final Type targetType, final String originalBound) {
        this.targetType = targetType;
        this.originalBound = originalBound;
    }

    public Type templateBound() {

        Preconditions.checkNotNull(this.targetType, "Target not a template bound: " + this.originalBound);
        return this.targetType;
    }

    public boolean isTemplateType() {
        return this.targetType != null;
    }

    public String originalBound() {
        return this.originalBound;
    }

    public String getBoxedType() {
        return templateBound().getBoxedType();
    }

    @Override
    public String toString() {

        return "{originalBound='" + this.originalBound + "', targetType='" + this.targetType + "'}";
    }
} //end static class TypeBound