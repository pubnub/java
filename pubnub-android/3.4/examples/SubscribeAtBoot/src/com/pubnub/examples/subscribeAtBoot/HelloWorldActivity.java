package com.pubnub.examples.subscribeAtBoot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class HelloWorldActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In new versions of Android, the service may not be activated unless an
        // associated activity is run at least once. This empty activity serves
        // that purpose

        Intent serviceIntent = new Intent(this, PubnubService.class);
        startService(serviceIntent);

        Log.i("HelloWorldActivity", "PubNub Activity Started!");

    }
}