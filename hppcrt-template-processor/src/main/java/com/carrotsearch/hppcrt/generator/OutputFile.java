package com.carrotsearch.hppcrt.generator;

import java.nio.file.Path;

class OutputFile
{
    public final Path path;
    public boolean upToDate;

    public OutputFile(final Path target) {
        this.path = target.toAbsolutePath().normalize();
    }
}