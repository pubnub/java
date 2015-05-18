package com.pubnub.api;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author PubnubCore
 */
class PubnubUtilCore {

    /**
     * Takes source and delimiter string as inputs and returns splitted string
     * in form of tokens in String array
     *
     * @param source
     *            , input String
     * @param delimiter
     *            , delimiter to split on
     * @return String[] , tokens in and array
     */
    public static String[] splitString(String source, String delimiter) {

        int delimiterCount = 0;
        int index = 0;
        String tmpStr = source;

        String[] splittedList;

        while ((index = tmpStr.indexOf(delimiter)) != -1) {

            tmpStr = tmpStr.substring(index + delimiter.length());
            delimiterCount++;
        }

        splittedList = new String[delimiterCount + 1];

        int counter = 0;
        tmpStr = source;

        do {
            int nextIndex = tmpStr.indexOf(delimiter, index + 1);

            if (nextIndex != -1) {
                splittedList[counter++] = tmpStr.substring(
                                              index + delimiter.length(), nextIndex);
                tmpStr = tmpStr.substring(nextIndex);

            } else {
                splittedList[counter++] = tmpStr.substring(index
                                          + delimiter.length());
                tmpStr = tmpStr.substring(index + 1);
            }
        } while ((index = tmpStr.indexOf(delimiter)) != -1);

        return splittedList;
    }

    /**
     * Takes String[] of tokens, and String delimiter as input and returns
     * joined String
     *
     * @param sourceArray
     *            , input tokens in String array
     * @param delimiter
     *            , delimiter to join on
     * @return String , string of tokens joined by delimiter
     */
    public static String joinString(String[] sourceArray, String delimiter) {

        if (sourceArray == null || delimiter == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < sourceArray.length - 1; i++) {
            sb.append(sourceArray[i]).append(delimiter);
        }
        sb.append(sourceArray[sourceArray.length - 1]);

        return sb.toString();
    }

    /**
     * Returns string keys in a hashtable as array of string
     *
     * @param ht
     *            , Hashtable
     * @return , string array with hash keys string
     */
    public static synchronized String[] hashtableKeysToArray(Hashtable ht) {
        Vector v = new Vector();
        String[] sa = null;
        int count = 0;

        Enumeration e = ht.keys();
        while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            v.addElement(s);
            count++;
        }

        sa = new String[count];
        v.copyInto(sa);
        return sa;

    }

    /**
     * Returns string keys in a hashtable as delimited string
     *
     * @param ht
     *            , Hashtable
     * @param delimiter
     *            , String
     * @param exclude
     *            , exclude channel if present as substring
     * @return , string array with hash keys string
     */
    public static synchronized String hashTableKeysToDelimitedString(
        Hashtable ht, String delimiter, String exclude) {

        StringBuffer sb = new StringBuffer();
        boolean first = true;
        Enumeration e = ht.keys();

        while (e.hasMoreElements()) {

            String s = (String) e.nextElement();

            if (exclude != null) {
                if (s.indexOf(exclude) != -1) {
                    continue;
                }
            }
            if (first) {
                sb.append(s);
                first = false;
            } else {
                sb.append(delimiter).append(s);
            }
        }
        return sb.toString();

    }

    /**
     * Returns string keys in a hashtable as delimited string
     *
     * @param ht
     *            , Hashtable
     * @param delimiter
     *            , String
     * @return , string array with hash keys string
     */
    public static String hashTableKeysToDelimitedString(
        Hashtable ht, String delimiter) {

        return hashTableKeysToDelimitedString(ht, delimiter, null);

    }

    static Hashtable hashtableClone(Hashtable ht) {
        if (ht == null)
            return null;

        Hashtable htresp = new Hashtable();
        Enumeration e = ht.keys();

        while (e.hasMoreElements()) {
            Object element = e.nextElement();
            htresp.put(element, ht.get(element));
        }
        return htresp;
    }

    static Hashtable hashtableClone(Hashtable ht1, Hashtable ht2) {
        if (ht1 == null && ht2 == null)
            return null;

        Hashtable htresp = new Hashtable();

        if (ht1 != null) {
            Enumeration e = ht1.keys();
            while (e.hasMoreElements()) {
                Object element = e.nextElement();
                htresp.put(element, ht1.get(element));
            }
        }
        if (ht2 != null) {
            Enumeration e = ht2.keys();
            while (e.hasMoreElements()) {
                Object element = e.nextElement();
                htresp.put(element, ht2.get(element));
            }
        }
        return htresp;
    }

    static Hashtable hashtableMerge(Hashtable dst, Hashtable src) {
        if (dst == null)
            return src;
        if (src == null)
            return dst;

        Enumeration e = src.keys();

        while (e.hasMoreElements()) {
            Object element = e.nextElement();
            dst.put(element, src.get(element));
        }
        return dst;
    }
    /**
     * Parse Json, change json string to string
     *
     * @param obj JSON data in string format
     *
     * @return JsonElement
     */
    static Object parseJSON(Object obj) {
        if (obj instanceof String) {
            if (((String) obj).endsWith("\"") && ((String) obj).startsWith("\""))
                obj = ((String) obj).substring(1, ((String) obj).length() -1);
        }
        return obj;
    }
}
