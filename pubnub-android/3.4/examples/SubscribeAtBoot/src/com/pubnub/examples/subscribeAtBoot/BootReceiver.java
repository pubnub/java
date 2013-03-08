package com.pubnub.examples.subscribeAtBoot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
        Log.i("PubnubService", "PubNub BootReceiver Starting");
        Intent intent = new Intent(context, PubnubService.class);
		context.startService(intent);
		Log.i("PubnubService", "PubNub BootReceiver Started");
	}

}
