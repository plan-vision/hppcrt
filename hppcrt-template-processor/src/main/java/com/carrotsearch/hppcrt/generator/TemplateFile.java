package com.carrotsearch.hppcrt.generator;

import java.nio.file.Path;

class TemplateFile
{
    public final Path path;

    public TemplateFile(final Path path) {
        this.path = path;
    }

    public String getFileName() {
        return this.path.getFileName().toString();
    }
}
