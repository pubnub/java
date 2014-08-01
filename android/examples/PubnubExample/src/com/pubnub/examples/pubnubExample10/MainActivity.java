package com.pubnub.examples.pubnubExample10;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.pubnub.examples.pubnubExample10.R;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends Activity {

    Pubnub pubnub;
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    Context context;
    public static String SENDER_ID;
    public static String REG_ID;
    private static final String APP_VERSION = "3.6.1";

    String PUBLISH_KEY = "demo";
    String SUBSCRIBE_KEY = "demo";
    String CIPHER_KEY = "";
    String SECRET_KEY = "demo";
    String ORIGIN = "pubsub";
    String AUTH_KEY;
    String UUID;
    Boolean SSL = false;


    static final String TAG = "Register Activity";

    private void notifyUser(Object message) {
        try {
            if (message instanceof JSONObject) {
                final JSONObject obj = (JSONObject) message;
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), obj.toString(),
                                Toast.LENGTH_LONG).show();

                        Log.i("Received msg : ", String.valueOf(obj));
                    }
                });

            } else if (message instanceof String) {
                final String obj = (String) message;
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), obj,
                                Toast.LENGTH_LONG).show();
                        Log.i("Received msg : ", obj.toString());
                    }
                });

            } else if (message instanceof JSONArray) {
                final JSONArray obj = (JSONArray) message;
                this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), obj.toString(),
                                Toast.LENGTH_LONG).show();
                        Log.i("Received msg : ", obj.toString());
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(
                "PUBNUB_DEV_CONSOLE", Context.MODE_PRIVATE);
        init();
        setContentView(R.layout.usage);
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                pubnub.disconnectAndResubscribe();

            }

        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        LinearLayout clicklayout = (LinearLayout) findViewById(R.id.clicklayout);
        clicklayout.setOnClickListener(new LinearLayout.OnClickListener() {
            public void onClick(View v) {
                openOptionsMenu();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.option1:
                subscribe();
                return true;

            case R.id.option2:
                publish();
                return true;

            case R.id.option3:
                presence();
                return true;

            case R.id.option4:
                history();
                return true;

            case R.id.option5:
                hereNow();
                return true;

            case R.id.option6:
                unsubscribe();
                return true;

            case R.id.option7:
                presenceUnsubscribe();
                return true;

            case R.id.option8:
                time();
                return true;

            case R.id.option9:
                disconnectAndResubscribe();
                return true;

            case R.id.option10:
                disconnectAndResubscribeWithTimetoken();
                return true;

            case R.id.option11:
                toggleResumeOnReconnect();
                return true;

            case R.id.option12:
                setMaxRetries();
                return true;

            case R.id.option13:
                setRetryInterval();
                return true;

            case R.id.option14:
                setSubscribeTimeout();
                return true;

            case R.id.option15:
                setNonSubscribeTimeout();
                return true;

            case R.id.option16:
                setWindowInterval();
                return true;
            case R.id.option17:
                setOrigin();
                return true;
            case R.id.option18:
                setDomain();
                return true;
            case R.id.option19:
                toggleCacheBusting();
                return true;
            case R.id.option20:
                setHeartbeat();
                return true;
            case R.id.option21:
                setHeartbeatInterval();
                return true;
            case R.id.option22:
                setUUID();
                return true;
            case R.id.option23:
                setState();
                return true;
            case R.id.option24:
                setAuthKey();
                return true;
            case R.id.option25:
                getInitData();
                return true;
            case R.id.option26:
                setSenderId();
                return true;
            case R.id.option27:
                gcmRegister();
                return true;
            case R.id.option28:
                gcmUnregister();
                return true;
            case R.id.option29:
                gcmAddChannel();
                return true;
            case R.id.option30:
                gcmRemoveChannel();
                return true;
            case R.id.option31:
                gcmRemoveAllChannels();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setAuthKey() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Auth Key");
        builder.setMessage("Enter Auth Key");
        final EditText edAuthKey = new EditText(this);
        edAuthKey.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(edAuthKey);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        AUTH_KEY = edAuthKey.getEditableText().toString();
                        saveCredentials();
                        init();
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setSenderId() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Sender Id");
        builder.setMessage("Enter Sender Id");
        final EditText edSenderId = new EditText(this);
        edSenderId.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(edSenderId);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        SENDER_ID = edSenderId.getEditableText().toString();
                        int appVersion = getAppVersion(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("REG_ID", "");
                        editor.putInt(APP_VERSION, appVersion);
                        editor.commit();
                        saveCredentials();
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void _getSsl(String publish_key, String subscribe_key, String secret_key, String cipher_key) {
        PUBLISH_KEY = publish_key;
        SUBSCRIBE_KEY = subscribe_key;
        SECRET_KEY = secret_key;
        CIPHER_KEY = cipher_key;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SSL Config");
        builder.setMessage("Enable SSL ?");
        builder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        SSL = true;
                        saveCredentials();
                        init();
                    }

                });
        builder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        SSL = false;
                        saveCredentials();
                        init();
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void _getCipherKey(final String publish_key, final String subscribe_key, final String secret_key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Cipher Key");
        builder.setMessage("Enter Cipher Key");
        final EditText edValue = new EditText(this);
        edValue.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(edValue);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        _getSsl(publish_key, subscribe_key, secret_key, edValue.getEditableText().toString());
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void _getSecretKey(final String publish_key, final String subscribe_key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Secret Key");
        builder.setMessage("Enter Secret Key");
        final EditText edValue = new EditText(this);
        edValue.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(edValue);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        _getCipherKey(publish_key, subscribe_key, edValue.getEditableText().toString());
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void _getSubscribeKey(final String publish_key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Subscribe Key");
        builder.setMessage("Enter Subscribe Key");
        final EditText edValue = new EditText(this);
        edValue.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(edValue);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        _getSecretKey(publish_key, edValue.getEditableText().toString());
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void _getPublishKey() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Publish Key");
        builder.setMessage("Enter Publish Key");
        final EditText edValue = new EditText(this);
        edValue.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(edValue);
        builder.setPositiveButton("Set",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        _getSubscribeKey(edValue.getEditableText().toString());
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void getInitData() {
        _getPublishKey();
    }

    private void init() {

        Map<String, String> map = getCredentials();

//        PUBLISH_KEY = map.get("PUBLISH_KEY");
//        SUBSCRIBE_KEY = map.get("SUBSCRIBE_KEY");
//        SECRET_KEY = map.get("SECRET_KEY");
//        CIPHER_KEY = map.get("CIPHER_KEY");
//        SSL = (map.get("SSL") == "true")?true:false;
//        SENDER_ID = map.get("SENDER_ID");
//        AUTH_KEY = map.get("AUTH_KEY");
//        ORIGIN = map.get("ORIGIN");
//        REG_ID = map.get("REG_ID");

        // The following hardcodes this demo app to run against our beta environment and config.

        PUBLISH_KEY = "demo-36";
        SUBSCRIBE_KEY = "demo-36";
        SECRET_KEY = "demo-36";
        CIPHER_KEY = map.get("CIPHER_KEY");
        SSL = (map.get("SSL") == "true")?true:false;
        SENDER_ID = map.get("SENDER_ID");
        AUTH_KEY = map.get("AUTH_KEY");
        ORIGIN = "gcm-beta";
        REG_ID = map.get("REG_ID");
        SENDER_ID = "506053237730";



        pubnub = new Pubnub(
                PUBLISH_KEY,
                SUBSCRIBE_KEY,
                SECRET_KEY,
                CIPHER_KEY,
                SSL
        );
        pubnub.setCacheBusting(false);
        pubnub.setOrigin(ORIGIN);
        pubnub.setAuthKey(AUTH_KEY);

        // A SENDER_ID corresponds with a Server API Key with GCM.
        // The above Sender ID (506053237730) corresponds to this Server API Key:
        // AIzaSyBNHRBzCKW9oUtTItl9qmLEVmRgG4SBys4

        // If you use the PubNub demo-36 API keys, we've already associated it on the server-side,
        // you can use this Sender ID in your demo app without needing to config anything server-side (with Google or PubNub)

        // If you want to use your own keys, you can use this SenderID,
        // But you will need to upload AIzaSyBNHRBzCKW9oUtTItl9qmLEVmRgG4SBys4 as your
        // GCM API Key to your Web Portal

        // Or, you use your own PN keyset, replace the above SENDER_ID with your own Sender ID, and upload to the web
        // portal your own associated Server API Key

        // More info on this process here: http://developer.android.com/google/gcm/gs.html

    }

    private void saveCredentials() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("PUBLISH_KEY", PUBLISH_KEY);
        editor.putString("SUBSCRIBE_KEY", SUBSCRIBE_KEY);
        editor.putString("SECRET_KEY", SECRET_KEY);
        editor.putString("AUTH_KEY", AUTH_KEY);
        editor.putString("CIPHER_KEY", CIPHER_KEY);
        editor.putString("ORIGIN", ORIGIN);
        editor.putString("UUID", UUID);
        editor.putString("SSL", SSL.toString());
        editor.putString("SENDER_ID", SENDER_ID);
        editor.commit();
    }

    private Map<String, String> getCredentials() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("PUBLISH_KEY", prefs.getString("PUBLISH_KEY", "demo"));
        map.put("SUBSCRIBE_KEY", prefs.getString("SUBSCRIBE_KEY", "demo"));
        map.put("SECRET_KEY", prefs.getString("SECRET_KEY", "demo"));
        map.put("CIPHER_KEY", prefs.getString("CIPHER_KEY", ""));
        map.put("AUTH_KEY", prefs.getString("AUTH_KEY", null));
        map.put("ORIGIN", prefs.getString("ORIGIN", "pubsub"));
        map.put("UUID", prefs.getString("UUID", null));
        map.put("SSL", prefs.getString("SSL", "false"));
        map.put("SENDER_ID", prefs.getString("SENDER_ID", null));
        return map;
    }

    private void gcmRemoveAllChannels() {
        if (TextUtils.isEmpty(REG_ID)) {
            Toast.makeText(getApplicationContext(),
                    "GCM Registration id not set. Register to GCM and try again.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        pubnub.removeAllPushNotificationsForDeviceRegistrationId(REG_ID, new Callback() {
            @Override
            public void successCallback(String channel,
                                        Object message) {
                notifyUser("GCM REMOVE ALL : " + message);
            }

            @Override
            public void errorCallback(String channel,
                                      PubnubError error) {
                notifyUser("GCM REMOVE ALL : " + error);
            }
        });

    }

    private void gcmRemoveChannel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Channel from GCM");
        builder.setMessage("Enter Channel Name");
        final EditText edChannelName = new EditText(this);
        edChannelName.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(edChannelName);
        builder.setPositiveButton("Remove",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(REG_ID)) {
                            Toast.makeText(getApplicationContext(),
                                    "GCM Registration id not set. Register to GCM and try again.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        String channel = edChannelName.getText().toString();
                        pubnub.disablePushNotificationsOnChannel(channel, REG_ID, new Callback() {
                            @Override
                            public void successCallback(String channel,
                                                        Object message) {
                                notifyUser("GCM REMOVE : " + message);
                            }

                            @Override
                            public void errorCallback(String channel,
                                                      PubnubError error) {
                                notifyUser("GCM REMOVE : " + error);
                            }
                        });
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void gcmAddChannel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Channel to GCM");
        builder.setMessage("Enter Channel Name");
        final EditText edChannelName = new EditText(this);
        edChannelName.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(edChannelName);
        builder.setPositiveButton("Add",
                new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (TextUtils.isEmpty(REG_ID)) {
                      Toast.makeText(getApplicationContext(),
                              "GCM Registration id not set. Register to GCM and try again.",
                              Toast.LENGTH_LONG).show();
                      return ;
                }
                String channel = edChannelName.getText().toString();
                pubnub.enablePushNotificationsOnChannel(channel, REG_ID, new Callback() {
                    @Override
                    public void successCallback(String channel,
                    Object message) {
                        notifyUser("GCM ADD : " + message);
                    }
                    @Override
                    public void errorCallback(String channel,
                    PubnubError error) {
                        notifyUser("GCM ADD : " + error);
                    }
                });
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    private void gcmUnregister() {
        //TODO add unregister code
    }


    private String gcmRegister() {

        context = getApplicationContext();
        gcm = GoogleCloudMessaging.getInstance(this);

        if (TextUtils.isEmpty(SENDER_ID)) {
            Toast.makeText(getApplicationContext(),
                    "GCM Sender ID not set.",
                    Toast.LENGTH_LONG).show();
            return null;
        }

        REG_ID = getRegistrationId(context);

        if (TextUtils.isEmpty(REG_ID)) {

            registerInBackground();

            Log.d("RegisterActivity",
                    "registerGCM - successfully registered with GCM server - regId: "
                            + REG_ID);
        } else {
            Toast.makeText(getApplicationContext(),
                    "RegId already available. RegId: " + REG_ID,
                    Toast.LENGTH_LONG).show();
        }
        return REG_ID;
    }

    private String getRegistrationId(Context context) {
        String registrationId = prefs.getString("REG_ID", "");
        if (registrationId.length() <= 0) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            Log.d("RegisterActivity",
                    "I never expected this! Going down, going down!" + e);
            throw new RuntimeException(e);
        }
    }

    private void registerInBackground() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    REG_ID = gcm.register(SENDER_ID);
                    Log.d("RegisterActivity", "registerInBackground - regId: "
                            + REG_ID);
                    msg = "Device registered, registration ID=" + REG_ID;

                    storeRegistrationId(context, REG_ID);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.d("RegisterActivity", "Error: " + msg);
                }
                Log.d("RegisterActivity", "AsyncTask completed: " + msg);
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(getApplicationContext(),
                        "Registered with GCM Server." + msg, Toast.LENGTH_LONG)
                        .show();
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId) {
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("REG_ID", regId);
        editor.putInt(APP_VERSION, appVersion);
        editor.commit();
    }

    private void setHeartbeatInterval() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Presence Heartbeat Interval");
        builder.setMessage("Enter heartbeat value in seconds");
        final EditText edTimeout = new EditText(this);
        edTimeout.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(edTimeout);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pubnub.setHeartbeatInterval(Integer.parseInt(edTimeout.getText().toString()));
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void setHeartbeat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Presence Heartbeat");
        builder.setMessage("Enter heartbeat value in seconds");
        final EditText edTimeout = new EditText(this);
        edTimeout.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(edTimeout);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pubnub.setHeartbeat(Integer.parseInt(edTimeout.getText().toString()));
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void toggleCacheBusting() {
        pubnub.setCacheBusting(pubnub.getCacheBusting() ? false : true);
        notifyUser("CACHE BUSTING : " + pubnub.getCacheBusting());

    }

    private void setOrigin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Origin");
        builder.setMessage("Enter Origin");
        final EditText edTimetoken = new EditText(this);
        builder.setView(edTimetoken);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ORIGIN = edTimetoken.getText().toString();
                        saveCredentials();
                        pubnub.setOrigin(ORIGIN);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setDomain() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Domain");
        builder.setMessage("Enter Domain");
        final EditText edTimetoken = new EditText(this);
        builder.setView(edTimetoken);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pubnub.setDomain(
                                edTimetoken.getText().toString());
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setUUID() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set UUID");
        builder.setMessage("Enter UUID");
        final EditText edTimetoken = new EditText(this);
        builder.setView(edTimetoken);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UUID = edTimetoken.getText().toString();
                        saveCredentials();
                        pubnub.setUUID(UUID);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setNonSubscribeTimeout() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Non Subscribe Timeout");
        builder.setMessage("Enter timeout value in milliseconds");
        final EditText edTimeout = new EditText(this);
        edTimeout.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(edTimeout);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pubnub.setNonSubscribeTimeout(Integer
                                .parseInt(edTimeout.getText().toString()));
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void subscribe() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Subscribe");
        builder.setMessage("Enter channel name");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Subscribe",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String channel = input.getText().toString();

                        try {
                            pubnub.subscribe(channel, new Callback() {
                                @Override
                                public void connectCallback(String channel,
                                                            Object message) {
                                    notifyUser("SUBSCRIBE : CONNECT on channel:"
                                            + channel
                                            + " : "
                                            + message.getClass()
                                            + " : "
                                            + message.toString());
                                }

                                @Override
                                public void disconnectCallback(String channel,
                                                               Object message) {
                                    notifyUser("SUBSCRIBE : DISCONNECT on channel:"
                                            + channel
                                            + " : "
                                            + message.getClass()
                                            + " : "
                                            + message.toString());
                                }

                                @Override
                                public void reconnectCallback(String channel,
                                                              Object message) {
                                    notifyUser("SUBSCRIBE : RECONNECT on channel:"
                                            + channel
                                            + " : "
                                            + message.getClass()
                                            + " : "
                                            + message.toString());
                                }

                                @Override
                                public void successCallback(String channel,
                                                            Object message) {
                                    notifyUser("SUBSCRIBE : " + channel + " : "
                                            + message.getClass() + " : "
                                            + message.toString());
                                }

                                @Override
                                public void errorCallback(String channel,
                                                          PubnubError error) {
                                    notifyUser("SUBSCRIBE : ERROR on channel "
                                            + channel + " : "
                                            + error.toString());
                                }
                            });

                        } catch (Exception e) {

                        }
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void _state(final String channel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set STATE");
        builder.setMessage("Enter state (JSON Object)");
        final EditText etMessage = new EditText(this);
        builder.setView(etMessage);
        builder.setPositiveButton("Submit",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Callback setMetaDataCallback = new Callback() {
                            @Override
                            public void successCallback(String channel,
                                                        Object message) {
                                notifyUser("SET STATE : " + message);
                            }

                            @Override
                            public void errorCallback(String channel,
                                                      PubnubError error) {
                                notifyUser("SET STATE : " + error);
                            }
                        };

                        String message = etMessage.getText().toString();
                        JSONObject js = null;
                        try {
                            js = new JSONObject(message);
                            pubnub.setState(channel, pubnub.getUUID(), js, setMetaDataCallback);
                            return;
                        } catch (Exception e) {
                        }

                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void _publish(final String channel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Publish");
        builder.setMessage("Enter message");
        final EditText etMessage = new EditText(this);
        builder.setView(etMessage);
        builder.setPositiveButton("Publish",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Callback publishCallback = new Callback() {
                            @Override
                            public void successCallback(String channel,
                                                        Object message) {
                                notifyUser("PUBLISH : " + message);
                            }

                            @Override
                            public void errorCallback(String channel,
                                                      PubnubError error) {
                                notifyUser("PUBLISH : " + error);
                            }
                        };

                        String message = etMessage.getText().toString();

                        try {
                            Integer i = Integer.parseInt(message);
                            pubnub.publish(channel, i, publishCallback);
                            return;
                        } catch (Exception e) {
                        }

                        try {
                            Double d = Double.parseDouble(message);
                            pubnub.publish(channel, d, publishCallback);
                            return;
                        } catch (Exception e) {
                        }


                        try {
                            JSONArray js = new JSONArray(message);
                            pubnub.publish(channel, js, publishCallback);
                            return;
                        } catch (Exception e) {
                        }

                        try {
                            JSONObject js = new JSONObject(message);
                            pubnub.publish(channel, js, publishCallback);
                            return;
                        } catch (Exception e) {
                        }

                        pubnub.publish(channel, message, publishCallback);
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void publish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Publish ");
        builder.setMessage("Enter channel name");
        final EditText etChannel = new EditText(this);
        builder.setView(etChannel);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _publish(etChannel.getText().toString());
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setState() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set State ");
        builder.setMessage("Enter channel name");
        final EditText etChannel = new EditText(this);
        builder.setView(etChannel);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _state(etChannel.getText().toString());
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void presence() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Presence");
        builder.setMessage("Enter channel name");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Subscribe For Presence",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String channel = input.getText().toString();

                        try {
                            pubnub.presence(channel, new Callback() {
                                @Override
                                public void successCallback(String channel,
                                                            Object message) {
                                    notifyUser("PRESENCE : " + channel + " : "
                                            + message.getClass() + " : "
                                            + message.toString());
                                }

                                @Override
                                public void errorCallback(String channel,
                                                          PubnubError error) {
                                    notifyUser("PRESENCE : ERROR on channel "
                                            + channel + " : "
                                            + error.toString());
                                }
                            });

                        } catch (Exception e) {

                        }
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void history() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("History");
        builder.setMessage("Enter channel name");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Get history",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String channel = input.getText().toString();
                        pubnub.history(channel, 2, new Callback() {
                            @Override
                            public void successCallback(String channel,
                                                        Object message) {
                                notifyUser("HISTORY : " + message);
                            }

                            @Override
                            public void errorCallback(String channel,
                                                      PubnubError error) {
                                notifyUser("HISTORY : " + error);
                            }
                        });
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void hereNow() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Here Now");
        builder.setMessage("Enter channel name");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Get Here Now",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String channel = input.getText().toString();
                        pubnub.hereNow(channel, new Callback() {
                            @Override
                            public void successCallback(String channel,
                                                        Object message) {
                                notifyUser("HERE NOW : " + message);
                            }

                            @Override
                            public void errorCallback(String channel,
                                                      PubnubError error) {
                                notifyUser("HERE NOW : " + error);
                            }
                        });
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void unsubscribe() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unsubscribe");
        builder.setMessage("Enter channel name");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Unsubscribe",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String channel = input.getText().toString();
                        pubnub.unsubscribe(channel);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void presenceUnsubscribe() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unsubscribe Presence");
        builder.setMessage("Enter channel name");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("Unsubscribe Presence",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String channel = input.getText().toString();
                        pubnub.unsubscribePresence(channel);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void time() {
        pubnub.time(new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("TIME : " + message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("TIME : " + error);
            }
        });

    }

    private void disconnectAndResubscribe() {
        pubnub.disconnectAndResubscribe();

    }

    private void setSubscribeTimeout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Subscribe Timeout");
        builder.setMessage("Enter timeout value in milliseconds");
        final EditText edTimeout = new EditText(this);
        edTimeout.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(edTimeout);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pubnub.setSubscribeTimeout(Integer.parseInt(edTimeout
                                .getText().toString()));
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void setRetryInterval() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Retry Interval");
        builder.setMessage("Enter retry interval in milliseconds");
        final EditText edInterval = new EditText(this);
        edInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(edInterval);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pubnub.setRetryInterval(Integer.parseInt(edInterval
                                .getText().toString()));
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setWindowInterval() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Window Interval");
        builder.setMessage("Enter Window interval in milliseconds");
        final EditText edInterval = new EditText(this);
        edInterval.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(edInterval);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pubnub.setWindowInterval(Integer.parseInt(edInterval
                                .getText().toString()));
                    }

                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void toggleResumeOnReconnect() {
        pubnub.setResumeOnReconnect(pubnub.isResumeOnReconnect() ? false : true);
        notifyUser("RESUME ON RECONNECT : " + pubnub.isResumeOnReconnect());
    }

    private void setMaxRetries() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Max Retries");
        builder.setMessage("Enter Max Retries");
        final EditText edRetries = new EditText(this);
        edRetries.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(edRetries);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pubnub.setMaxRetries(Integer.parseInt(edRetries
                                .getText().toString()));
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void disconnectAndResubscribeWithTimetoken() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Disconnect and Resubscribe with timetoken");
        builder.setMessage("Enter Timetoken");
        final EditText edTimetoken = new EditText(this);
        builder.setView(edTimetoken);
        builder.setPositiveButton("Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pubnub.disconnectAndResubscribeWithTimetoken(
                                edTimetoken.getText().toString());
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
