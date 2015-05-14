package com.carrotsearch.hppcrt.generator.parser;

import java.util.logging.Logger;

import com.carrotsearch.hppcrt.generator.TemplateOptions;

public class InlinesReplacementVisitor extends ReplacementVisitorBase
{
    /**
     * Constructor
     * @param templateOptions
     * @param processor
     */
    public InlinesReplacementVisitor(final TemplateOptions templateOptions, final SignatureProcessor processor) {
        super(templateOptions, processor, Logger.getLogger(InlinesReplacementVisitor.class.getName()));
    }

}
