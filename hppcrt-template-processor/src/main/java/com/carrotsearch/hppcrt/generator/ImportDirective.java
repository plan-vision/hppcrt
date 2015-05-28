package com.carrotsearch.hppcrt.generator;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Parse;
import org.apache.velocity.runtime.parser.node.Node;

import com.carrotsearch.hppcrt.generator.TemplateOptions.DoNotGenerateTypeException;

/**
 * This class declares a custom "#import" Velocity directive,
 * that does the same thing as "#parse" but /dev/null all outputs,
 * so is litterally just "importing" Velocity definitions into the current location.
 * ex: #import("com/carrotsearch/hppcrt/Intrinsics.java")
 * @author Vincent
 *
 */
public class ImportDirective extends Parse
{
    public ImportDirective() {
        super();
    }

    @Override
    public String getName() {

        return "import";
    }

    @Override
    public boolean render(final InternalContextAdapter context, final Writer writer, final Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        boolean bSuccess = false;

        try {
            // dev/null all outputs...
            bSuccess = super.render(context, TemplateNullWriter.NULL_WRITER, node);
        }
        catch (final MethodInvocationException e) {

            if (e.getCause() instanceof DoNotGenerateTypeException) {

                //do nothing, catch the exception, we have encountered a DoNotGenerateTypeException
                //thrown by TemplateOptions purposefully
                bSuccess = true;
            }
            else {

                //rethrow the beast to stop the thing dead.
                throw e;
            }
        } //end MethodInvocationException

        return bSuccess;
    }
}
