package com.pubnub.examples.subscribeAtBoot;

import android.os.Handler;
import android.os.Message;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PubnubService extends Service {

    String channel = "hello_world";
    Pubnub pubnub = new Pubnub("demo", "demo", false);

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String pnMsg = msg.obj.toString();

            final Toast toast = Toast.makeText(getApplicationContext(), pnMsg, Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 200);

        }
    };

    private void notifyUser(Object message) {

        Message msg = handler.obtainMessage();

        try {
            final String obj = (String) message;
            msg.obj = obj;
            handler.sendMessage(msg);
            Log.i("Received msg : ", obj.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "PubnubService created...", Toast.LENGTH_LONG).show();
        Log.i("PUBNUB", "PubnubService created...");
        try {
            pubnub.subscribe(new String[]{channel}, new Callback() {
                public void connectCallback(String channel) {
                    notifyUser("CONNECT on channel:" + channel);
                }

                public void disconnectCallback(String channel) {
                    notifyUser("DISCONNECT on channel:" + channel);
                }

                public void reconnectCallback(String channel) {
                    notifyUser("RECONNECT on channel:" + channel);
                }

                public void successCallback(String channel, Object message) {
                    notifyUser(channel + " " + message.toString());
                }

                public void errorCallback(String channel, Object message) {
                    notifyUser(channel + " " + message.toString());
                }
            });
        } catch (PubnubException e) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "PubnubService destroyed...", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
