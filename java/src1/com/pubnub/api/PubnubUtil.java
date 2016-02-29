package com.pubnub.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;


/**
 * PubnubUtil class provides utility methods like urlEncode etc
 *
 * @author Pubnub
 *
 */
public class PubnubUtil extends PubnubUtilCore {
    static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String escapeJava(String a) {
//        return StringEscapeUtils.escapeJava(a);
        StringBuilder result = new StringBuilder(a);
        int len = a.length();
        for (int i=0, j=0; i < len; i++, j++) {
            final char in = a.charAt(i);
            final char escaped = escapeJavaChar(in);
            if (escaped != 0) {
                result.insert(j++, '\\');
                result.setCharAt(j, escaped);
            } else {
                int point = a.codePointAt(i);
                j = unicodeEscape(result, point, j);
                if (point > '\uffff') {
                    // multi-char
                    i++;
                }
            }
        }
        return result.toString();
    }

    /**
     * @return 0 if the char need not be escaped,
     *  else the character that should replace `in`
     *  after being escaped with a backslash.
     */
    private static char escapeJavaChar(char in) {
        if (in == '"' || in == '\\') {
            return in;
        }

        // JAVA_CTRL_CHARS_ESCAPE
        if (in == '\b') {
            return 'b';
        } else if (in == '\n') {
            return 'n';
        } else if (in == '\t') {
            return 't';
        } else if (in == '\f') {
            return 'f';
        } else if (in == '\r') {
            return 'r';
        }

        return 0;
    }

    /**
     * Given a char, and its location in a StringBuilder,
     *  replace it with an escaped unicode if necessary
     * @return The new index into StringBuilder
     */
    private static int unicodeEscape(StringBuilder result, int in, int index) {
        // utf8 within [32, 127] are fine
        if (in >= 32 && in <= 127) {
            return index;
        }

        if (in > '\uffff') {
            final char[] surrogatePair = Character.toChars(in);
            final String first = hex(surrogatePair[0]);
            final String second = hex(surrogatePair[1]);

            // ensure we have capacity to replace 2 chars
            // (IE a surrogate pair) with \\u + first + \\u + second
            result.ensureCapacity(result.capacity() + 2
                    + first.length() + second.length());
            result.delete(index, index + 2);
            result.insert(index++, '\\');
            result.insert(index++, 'u');
            result.insert(index, first);
            index += first.length();

            result.insert(index++, '\\');
            result.insert(index++, 'u');
            result.insert(index, second);
            index += second.length() - 1;
        } else {
            // ensure we have capacity to replace 1 char with 6
            result.ensureCapacity(result.capacity() + 5);
            result.deleteCharAt(index);
            result.insert(index++, '\\');
            result.insert(index++, 'u');
            result.insert(index++, HEX_DIGITS[in >> 12 & 15]);
            result.insert(index++, HEX_DIGITS[in >> 8 & 15]);
            result.insert(index++, HEX_DIGITS[in >> 4 & 15]);
            result.insert(index, HEX_DIGITS[in & 15]);
        }

        return index;
    }

    private static String hex(int codepoint) {
        return Integer.toHexString(codepoint).toUpperCase(Locale.ENGLISH);
    }


    public static String stringEscapeSlashes(String s, String a, String b) {
        return s.replace(a, b);
    }
    public static String stringReplaceAll(String s, String a, String b) {
        return s.replaceAll(a, b);
    }
    /**
     * Returns encoded String
     *
     * @param sUrl
     *            , input string
     * @return , encoded string
     */
    public static String pamEncode(String sUrl) {
        /* !'()*~ */

        String encoded = urlEncode(sUrl);
        if (encoded != null) {
            encoded = encoded.replace("*", "%2A").replace("!", "%21").replace("'", "%27").replace("(", "%28")
                    .replace(")", "%29").replace("[", "%5B").replace("]", "%5D").replace("~", "%7E");
        }
        return encoded;

    }

    /**
     * Returns encoded String
     *
     * @param sUrl
     *            , input string
     * @return , encoded string
     */
    public static String urlEncode(String sUrl) {
        try {
            return URLEncoder.encode(sUrl, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Convert input String to JSONObject, JSONArray, or String
     *
     * @param str
     *            JSON data in string format
     *
     * @return JSONArray or JSONObject or String
     */
    static Object stringToJSON(String str) {
        try {
            return new JSONArray(str);
        } catch (JSONException e) {
        }
        try {
            return new JSONObject(str);
        } catch (JSONException ex) {
        }
        try {
            return Integer.parseInt(str);
        } catch (Exception ex) {
        }
        try {
            return Double.parseDouble(str);
        } catch (Exception ex) {
        }
        return str;
    }
}

