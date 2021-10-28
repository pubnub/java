# PubNub Java-based SDKs for Java / Android

[![Build Status](https://travis-ci.com/pubnub/java.svg?branch=master)](https://travis-ci.com/pubnub/java)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/164fd518c314417e896b3de494ab75df)](https://www.codacy.com/app/PubNub/java?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=pubnub/java&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/164fd518c314417e896b3de494ab75df)](https://www.codacy.com/app/PubNub/java?utm_source=github.com&utm_medium=referral&utm_content=pubnub/java&utm_campaign=Badge_Coverage)
[![Download](https://api.bintray.com/packages/bintray/jcenter/com.pubnub%3Apubnub-gson/images/download.svg)](https://bintray.com/bintray/jcenter/com.pubnub%3Apubnub-gson/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/com.pubnub/pubnub-gson.svg)]()

This is the official PubNub Java SDK repository.

PubNub takes care of the infrastructure and APIs needed for the realtime communication layer of your application. Work on your app's logic and let PubNub handle sending and receiving data across the world in less than 100ms.

## Get keys

You will need the publish and subscribe keys to authenticate your app. Get your keys from the [Admin Portal](https://dashboard.pubnub.com/login).

## Configure PubNub

1. Integrate the Java SDK into your project:

   * for Maven, add the following dependency in your `pom.xml`:
     ```xml
     <dependency>
       <groupId>com.pubnub</groupId>
       <artifactId>pubnub-gson</artifactId>
       <version>5.2.1</version>
     </dependency>
     ```

   * for Gradle, add the following dependency in your `gradle.build`:
     ```groovy
     compile group: 'com.pubnub', name: 'pubnub-gson', version: '5.2.1'
     ```

2. Configure your keys:

    ```java
    PNConfiguration pnConfiguration = new PNConfiguration();
    pnConfiguration.setSubscribeKey("mySubscribeKey");
    pnConfiguration.setPublishKey("myPublishKey");
    pnConfiguration.setUuid("myUniqueUUID");
    
    PubNub pubnub = new PubNub(pnConfiguration);
    ```

## Add event listeners

```java
// SubscribeCallback is an Abstract Java class. It requires that you implement all Abstract methods of the parent class even if you don't need all the handler methods.

pubnub.addListener(new SubscribeCallback() {
    // PubNub status
    @Override
    public void status(PubNub pubnub, PNStatus status) {
        switch (status.getOperation()) {
            // combine unsubscribe and subscribe handling for ease of use
            case PNSubscribeOperation:
            case PNUnsubscribeOperation:
                // Note: subscribe statuses never have traditional errors,
                // just categories to represent different issues or successes
                // that occur as part of subscribe
                switch (status.getCategory()) {
                    case PNConnectedCategory:
                        // No error or issue whatsoever.
                    case PNReconnectedCategory:
                        // Subscribe temporarily failed but reconnected.
                        // There is no longer any issue.
                    case PNDisconnectedCategory:
                        // No error in unsubscribing from everything.
                    case PNUnexpectedDisconnectCategory:
                        // Usually an issue with the internet connection.
                        // This is an error: handle appropriately.
                    case PNAccessDeniedCategory:
                        // PAM does not allow this client to subscribe to this
                        // channel and channel group configuration. This is
                        // another explicit error.
                    default:
                        // You can directly specify more errors by creating
                        // explicit cases for other error categories of
                        // `PNStatusCategory` such as `PNTimeoutCategory` or
                        // `PNMalformedFilterExpressionCategory` or
                        // `PNDecryptionErrorCategory`.
                }

            case PNHeartbeatOperation:
                // Heartbeat operations can in fact have errors,
                // so it's important to check first for an error.
                // For more information on how to configure heartbeat notifications
                // through the status PNObjectEventListener callback, refer to
                // /docs/android-java/api-reference-configuration#configuration_basic_usage
                if (status.isError()) {
                    // There was an error with the heartbeat operation, handle here
                } else {
                    // heartbeat operation was successful
                }
            default: {
                // Encountered unknown status type
            }
        }
    }

    // Messages
    @Override
    public void message(PubNub pubnub, PNMessageResult message) {
        String messagePublisher = message.getPublisher();
        System.out.println("Message publisher: " + messagePublisher);
        System.out.println("Message Payload: " + message.getMessage());
        System.out.println("Message Subscription: " + message.getSubscription());
        System.out.println("Message Channel: " + message.getChannel());
        System.out.println("Message timetoken: " + message.getTimetoken());
    }

    // Presence
    @Override
    public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
        System.out.println("Presence Event: " + presence.getEvent());
        // Can be join, leave, state-change or timeout

        System.out.println("Presence Channel: " + presence.getChannel());
        // The channel to which the message was published

        System.out.println("Presence Occupancy: " + presence.getOccupancy());
        // Number of users subscribed to the channel

        System.out.println("Presence State: " + presence.getState());
        // User state

        System.out.println("Presence UUID: " + presence.getUuid());
        // UUID to which this event is related

        presence.getJoin();
        // List of users that have joined the channel (if event is 'interval')

        presence.getLeave();
        // List of users that have left the channel (if event is 'interval')

        presence.getTimeout();
        // List of users that have timed-out off the channel (if event is 'interval')

        presence.getHereNowRefresh();
        // Indicates to the client that it should call 'hereNow()' to get the
        // complete list of users present in the channel.
    }

    // Signals
    @Override
    public void signal(PubNub pubnub, PNSignalResult pnSignalResult) {
        System.out.println("Signal publisher: " + signal.getPublisher());
        System.out.println("Signal payload: " + signal.getMessage());
        System.out.println("Signal subscription: " + signal.getSubscription());
        System.out.println("Signal channel: " + signal.getChannel());
        System.out.println("Signal timetoken: " + signal.getTimetoken());
    }

    // Message actions
    @Override
    public void messageAction(PubNub pubnub, PNMessageActionResult pnActionResult) {
        PNMessageAction pnMessageAction = pnActionResult.getAction();
        System.out.println("Message action type: " + pnMessageAction.getType());
        System.out.println("Message action value: " + pnMessageAction.getValue());
        System.out.println("Message action uuid: " + pnMessageAction.getUuid());
        System.out.println("Message action actionTimetoken: " + pnMessageAction.getActionTimetoken());
        System.out.println("Message action messageTimetoken: " + pnMessageAction.getMessageTimetoken());]
        System.out.println("Message action subscription: " + pnActionResult.getSubscription());
        System.out.println("Message action channel: " + pnActionResult.getChannel());
        System.out.println("Message action timetoken: " + pnActionResult.getTimetoken());
    }

    // Files
    @Override
    public void file(PubNub pubnub, PNFileEventResult pnFileEventResult) {
        System.out.println("File channel: " + pnFileEventResult.getChannel());
        System.out.println("File publisher: " + pnFileEventResult.getPublisher());
        System.out.println("File message: " + pnFileEventResult.getMessage());
        System.out.println("File timetoken: " + pnFileEventResult.getTimetoken());
        System.out.println("File file.id: " + pnFileEventResult.getFile().getId());
        System.out.println("File file.name: " + pnFileEventResult.getFile().getName());
        System.out.println("File file.url: " + pnFileEventResult.getFile().getUrl());
    }
});
```

## Publish/subscribe

```java
pubnub.publish().channel(channelName)
  .message(messageJsonObject)
  .async((result, publishStatus) -> {
    if (!publishStatus.isError()) {
        // Message successfully published to specified channel.
    } else { // Request processing failed.
        // Handle message publish error
        // Check 'category' property to find out
        // issues because of which the request failed.
        // Request can be resent using: [status retry];
    }
});
```

## Documentation

* [API reference for Java ](https://www.pubnub.com/docs/java-se-java/pubnub-java-sdk)
* [API reference for Android](https://www.pubnub.com/docs/android-java/pubnub-java-sdk)

## Support

If you **need help** or have a **general question**, contact support@pubnub.com.
