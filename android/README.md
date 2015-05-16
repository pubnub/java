# Please direct all Support Questions and Concerns to Support@PubNub.com

### YOU MUST HAVE A PUBNUB ACCOUNT TO USE THE API.

http://www.pubnub.com/account

## PubNub 3.7 Real-time Cloud Push API - ANDROID

PubNub is a Massively Scalable Real-time Service for Web and Mobile Games.
This is a cloud-based service for broadcasting Real-time messages
to thousands of web and mobile clients simultaneously.

The PubNub Android client provides a superior alternative to C2DM for broadcasting messages to entire user base.
C2DM is hard to implement, and it imposes artificial limits for you to reach your users quickly.  
C2DM is not recommended as it is a "broadcast" mechanism according to Google.  C2DM is Slow, and limited to 1 message at a time.
Use PubNub Instead!

## Getting started with Pubnub in your app

Checkout the [walkthrough video](http://vimeo.com/95542286) first!

To use Pubnub, simply copy the Pubnub-Android-3.7.x.jar files in to your project's libs directory.

If you are using Android studio, you can declare PubNub as dependency in your build.gradle file
```
dependencies {
    compile 'com.pubnub:pubnub-android:3.7.+'
}
```
or if you want to use debug version of sdk
```
dependencies {
    compile 'com.pubnub:pubnub-android-debug:3.7.+'
}
```
### PubNub Android Sample App

This app demonstrates all PubNub features and functionality.  It can be found in the examples/PubnubExample directory.

### Pubnub Auto-subscribing, Auto-startup App

This is a sample app which subscribes to channel **"hello_world"** on receiving the **android.intent.action.BOOT_COMPLETED** intent.
The user is alerted about events like connect, message received etc. via notifications.

Code for this app can be found in the examples/SubscribeAtBoot directory.

## Building the Pubnub library jar

Pubnub library jar can be built by running following commands. ( Jar is already present in repository )
```
$ ant clean
$ ant
```

Pubnub library jar with debug messages enabled can be built by running following commands
```
$ ant clean
$ ant debug-build
```

##Connection Durability: Reconnecting & Resuming when a connection is lost or changed

As a mobile device may lose its connection under many different circumstances (IP change, roaming, etc), any current in-process subscribe operations can be forced
to reconnect (and optionally catchup where left off) when any change has occured in the network environment. This is done by calling the
**disconnectAndResubscribe()** method on the PubNub instance.

The demo application has examples of setting up a broadcast receiver for the **ConnectivityManager.CONNECTIVITY_ACTION**.
The **onReceive()** method is where it checks for certain (or any) network change conditions, and 
calls **disconnectAndResubscribe()** as needed.

```java
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				pubnub.disconnectAndResubscribe();
			} 

		}, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION  ));		
```

This logic has been purposely left out of the core and documented in the example application, so that if you wish to 
add additional tests for monitoring a potential zombied subscribe connection, it is very easy to add your own custom logic within this
given receiver template.

When **disconnectAndResubscribe()** is called, either via the broadcast receiver or otherwise, it will retry maxRetries times
to reconnect ever retryInterval seconds.  You can set these variables with the **setRetryInterval(int milliseconds)** and **setMaxRetries** methods.

If it **not able** to recover network within the given constraints, it will return an error response of [0, ERROR_MSG_STRING].

**setResumeOnReconnect()** allows you to define the behaviour upon successful reconnect.

If it **is able** to recover network, then the subscribe will "catchup", or resume where it left off before the interruption
if isResumeOnReconnect() is true, otherwise, it will restart and grab only new messages since it reconnected. 


##Client API Summary

###Init
```java
Pubnub pubnub = new Pubnub(
    "demo",  // PUBLISH_KEY   (Optional, supply "" to disable)
    "demo",  // SUBSCRIBE_KEY (Required)
    "",      // SECRET_KEY    (Optional, supply "" to disable)
    "",      // CIPHER_KEY    (Optional, supply "" to disable)
    false    // SSL_ON?
);
```

###Publish
```java

// Setup the argument Hash object to set the message and channel name

Hashtable args = new Hashtable(2);

String message = "Hello PubNub!";
String message = "hello_world";

args.put("message", message);
args.put("channel", channel); 

// Publish It!

pubnub.publish(args, new Callback() {
    public void successCallback(String channel, Object message) {
        notifyUser(message.toString());
    }

    public void errorCallback(String channel, Object message) {
    notifyUser(channel + " : " + message.toString());
    }
});

```

###Subscribe and Presence
```java

// Regular Subscribe

Hashtable args = new Hashtable(1);

args.put("channel", channel);

pubnub.subscribe(args, new Callback() {
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

// Presence

pubnub.presence(channel, new Callback() {
    public void successCallback(String channel, Object message) {
        notifyUser(message.toString());
    }

    public void errorCallback(String channel, Object message) {
        notifyUser(channel + " : " + message.toString());
    }
});
                    
```

###History
```java
pubnub.detailedHistory(channel, 2, new Callback() {
    public void successCallback(String channel, Object message) {
        notifyUser(message.toString());
    }

    public void errorCallback(String channel, Object message) {
        notifyUser(channel + " : " + message.toString());
    }
});
```

###Unsubscribe
```java
String channel = "goodbye_world";
pubnub.unsubscribe(channel);
```

###Time
```java
    // Get server time
    double time = pubnub.time();
    System.out.println("Time : "+time);
```

###UUID
```java
    // Get UUID
    System.out.println("UUID : "+Pubnub.uuid());
```

###Here Now

```java
pubnub.hereNow(channel, new Callback() {
    public void successCallback(String channel, Object message) {
        notifyUser(message.toString());
    }

    public void errorCallback(String channel, Object message) {
        notifyUser(channel + " : " + message.toString());
    }
});
```

# Please direct all Support Questions and Concerns to Support@PubNub.com

