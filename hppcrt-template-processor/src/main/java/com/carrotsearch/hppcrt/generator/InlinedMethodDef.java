package com.carrotsearch.hppcrt.generator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.exception.ParseErrorException;

public class InlinedMethodDef
{
    private static final Pattern JAVA_IDENTIFIER_PATTERN = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*",
            Pattern.MULTILINE | Pattern.DOTALL);

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

    /**
     * Converts the human readable arguments listed in argsArray[] as equivalent %1%s, %2%s...etc positional arguments in the method body methodBodyStr
     * @param methodBodyStr
     * @param argsArray
     * @return
     */
    public static String reformatArguments(final String methodBodyStr, final String[] argsArray)
    {
        int argPosition = 0;
        boolean argumentIsFound = false;

        final StringBuilder sb = new StringBuilder();

        final StringBuilder currentBody = new StringBuilder(methodBodyStr);

        //for each of the arguments
        for (int i = 0; i < argsArray.length; i++)
        {
            argumentIsFound = false;

            sb.setLength(0);

            while (true)
            {
                final Matcher m = InlinedMethodDef.JAVA_IDENTIFIER_PATTERN.matcher(currentBody);

                if (m.find())
                {
                    //copy from the start of the (remaining) method body to start of the current find :
                    sb.append(currentBody, 0, m.start());

                    //this java identifier is known, replace
                    if (m.group().equals(argsArray[i].trim()))
                    {
                        if (!argumentIsFound)
                        {
                            argPosition++;
                            //first time this argument is encountered, increment position count
                            argumentIsFound = true;
                        }

                        //append replacement : use parenthesis to safe containement of any form of valid expression given as argument
                        sb.append("(%");
                        sb.append(argPosition);
                        sb.append("$s)");
                    }
                    else
                    {
                        //else append verbatim
                        sb.append(m.group());
                    }

                    //Truncate currentBody to only keep the remaining string, after the current find :
                    currentBody.delete(0, m.end());

                } //end if find
                else
                {
                    //append verbatim
                    sb.append(currentBody);
                    break;
                }
            } //end while

            //re-run for the next argument with the whole previous computation
            currentBody.setLength(0);
            currentBody.append(sb);

            //if a particular argument do not exist in method body, we must skip its position anyway.
            if (!argumentIsFound) {

                argPosition++;
            }
        }  //end for each arguments

        return currentBody.toString();
    }
}
