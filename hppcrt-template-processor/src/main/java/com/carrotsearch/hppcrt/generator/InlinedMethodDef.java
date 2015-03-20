package com.carrotsearch.hppcrt.generator;

import java.util.regex.Pattern;

import org.apache.velocity.exception.ParseErrorException;

public class InlinedMethodDef
{
    public final String callName;
    public final String formattedCallName;

    private String body;

    public final Pattern compiledCallName;

    public InlinedMethodDef(final String callName) {

        this.callName = callName;

        //compile the Pattern
        this.formattedCallName = InlinedMethodDef.reformatCallName(this.callName);
        this.compiledCallName = Pattern.compile(this.formattedCallName, Pattern.MULTILINE | Pattern.DOTALL);

    }

    public String getBody() {
        return this.body;
    }

    public void setBody(final String bod) {
        this.body = bod;
    }

    @Override
    public String toString() {

        return String.format("InlinedMethodDef(callName= '%s', formattedCallName='%s', body='%s')",
                this.callName, this.formattedCallName, this.body);
    }

    @Override
    public int hashCode() {

        return this.formattedCallName.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {

        return this.formattedCallName.equals(((InlinedMethodDef) obj).formattedCallName);
    }

    /**
     * Reformat the call name to a Pattern able
     * to have optional Generic arguments
     * @param callName
     * @return
     */
    public static String reformatCallName(final String callName)
    {
        String reformatted = "";
        //Search if the name is qualified ("this." ," ClassName.")
        final String[] splittedCallName = callName.split("\\.");

        if (splittedCallName.length == 1) {

            //not qualified, form a 2 group regex with an optional generic pattern, ex:
            //"foo" ==> "(<[^>]+>\\s*)?(foo)"
            //also captures the first parenthesis of the function just after callName.
            reformatted = "(<[^>]+>\\s*)?" + "(" + callName + ")(\\()";

        }
        else if (splittedCallName.length == 2) {

            //qualified, form a 3 group regex with a generic pattern, ex :
            //"Intrinsics.newKTypeArray" ==> "(Intrinsics.\\s*)(<[^>]+>\\s*)?(newKTypeArray)"
            //also captures the first parenthesis of the function just after callName.
            reformatted = "(" + splittedCallName[0] + ".\\s*)" + "(<[^>]+>\\s*)?" + "(" + splittedCallName[1] + ")(\\()";

        }
        else {
            //not managed
            throw new ParseErrorException("[ERROR] : Not able to manage this call form: " + callName);
        }

        return reformatted;
    }
}
