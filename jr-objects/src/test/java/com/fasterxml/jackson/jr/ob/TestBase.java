package com.fasterxml.jackson.jr.ob;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectReadContext;

import junit.framework.TestCase;

public abstract class TestBase extends TestCase
{
    protected enum ABC { A, B, C; }

    protected static class NameBean {
        protected String first, last;

        public NameBean() { }
        public NameBean(String f, String l) {
            first = f;
            last = l;
        }

        public String getFirst() { return first; }
        public String getLast() { return last; }

        public void setFirst(String n) { first = n; }
        public void setLast(String n) { last = n; }
    }
    
    protected void verifyException(Throwable e, String... matches)
    {
        String msg = e.getMessage();
        String lmsg = (msg == null) ? "" : msg.toLowerCase();
        for (String match : matches) {
            String lmatch = match.toLowerCase();
            if (lmsg.indexOf(lmatch) >= 0) {
                return;
            }
        }
        fail("Expected an exception with one of substrings ("+Arrays.asList(matches)+"): got one with message \""+msg+"\"");
    }

    protected JsonParser parserFor(String source) throws IOException {
        return parserFor(JSON.std, source);
    }

    protected JsonParser parserFor(JSON json, String source) throws IOException {
        return json.getStreamingFactory().createParser(ObjectReadContext.empty(), source.toCharArray());
    }

    protected String quote(String str) {
        return "\"" + str + "\"";
    }

    protected String aposToQuotes(String json) {
        return json.replace("'", "\"");
    }
}
