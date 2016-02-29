package com.pubnub.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author dhleong
 */
public class PubnubUtilTest {
    @Test
    public void escapeSimple() {
        assertEquals("well \\\"hello\\\" there",
                PubnubUtil.escapeJava("well \"hello\" there"));

        assertEquals("this\\\\that",
                PubnubUtil.escapeJava("this\\that"));
    }

    @Test
    public void escapeSequences() {
        assertEquals("\\t",
                PubnubUtil.escapeJava("\t"));

        assertEquals("\\t\\b\\f\\n\\r",
                PubnubUtil.escapeJava("\t\b\f\n\r"));

        assertEquals("\\tt\\bb\\ff\\nn\\rr",
                PubnubUtil.escapeJava("\tt\bb\ff\nn\rr"));
    }

    @Test
    public void escapeUnicode() {
        assertEquals("\\u001F", // 31, < 32
                PubnubUtil.escapeJava("\u001f"));
        assertEquals("\\u0080", // 128, > 128
                PubnubUtil.escapeJava("\u0080"));

        // but these are fine:
        assertEquals("\u0020", // 32
                PubnubUtil.escapeJava("\u0020"));
        assertEquals("\u007f", // 127
                PubnubUtil.escapeJava("\u007f"));
    }

    @Test
    public void escapeSurrogatePairs() {
        assertEquals("\\uD83D\\uDE00",
                PubnubUtil.escapeJava("\uD83D\uDE00"));
    }

    @Test
    public void escapeUnicodeMulti() {
        assertEquals("\\u001F\\u0080\\u0081a\\u0082hi\\u0083",
                PubnubUtil.escapeJava("\u001F\u0080\u0081a\u0082hi\u0083"));
        
        assertEquals("\\uD83D\\uDE00a\\u0082hi\\uD83D\\uDE00bye\\u0083",
                PubnubUtil.escapeJava("\uD83D\uDE00a\u0082hi\uD83D\uDE00bye\u0083"));
    }
}


