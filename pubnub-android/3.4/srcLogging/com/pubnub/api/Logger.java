package com.pubnub.api;

import android.util.Log;

public class Logger extends AbstractLogger {
	Class _class;
	public Logger(Class _class) {
		super();
		this._class = _class;
	}

	protected void nativeDebug(String s) {
		Log.d(_class.getName(), s);
	}
	protected void nativeVerbose(String s) {
		Log.v(_class.getName(), s);
	}
	protected void nativeError(String s) {
		Log.e(_class.getName(), s);
	}
	protected void nativeInfo(String s) {
		Log.i(_class.getName(), s );
	}
}
