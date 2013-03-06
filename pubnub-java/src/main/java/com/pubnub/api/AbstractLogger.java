package com.pubnub.api;

abstract class AbstractLogger {

    private static boolean LOGGING = false;

    protected abstract void nativeDebug(String s);

    protected abstract void nativeVerbose(String s);

    protected abstract void nativeError(String s);

    protected abstract void nativeInfo(String s);

    public void debug(String s) {
        if (LOGGING)
            nativeDebug(s);
    }

    public void verbose(String s) {
        if (LOGGING)
            nativeVerbose(s);
    }

    public void info(String s) {
        if (LOGGING)
            nativeInfo(s);
    }

    public void error(String s) {
        if (LOGGING)
            nativeError(s);
    }

}
