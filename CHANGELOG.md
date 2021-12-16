## v5.2.4
December 09 2021

#### Fixed
- Emit PNReconnectedCategory in case of successful manual reconnection.

## v5.2.3
November 17 2021

#### Fixed
- Eviction of OkHttp connection pool after reestablishing connection (affects transactional calls).

## v5.2.2
November 04 2021

#### Fixed
- Reconnect always restarts the subscription connection.

## [v5.2.1](https://github.com/pubnub/java/releases/tag/v5.2.1)
October-06-2021

[Full Changelog](https://github.com/pubnub/java/compare/v5.2.0...v5.2.1)

- 🌟️ Acceptance tests plugged into CI pipeline. 
- 🐛 Meta field exposed correctly in PNToken class. 

## [v5.2.0](https://github.com/pubnub/java/releases/tag/v5.2.0)
September-08-2021

- 🌟️ Extend grantToken method to enable control of Objects API permission. Enhance granularity of permission control to enable permissions per UUID. 

## [v5.1.1](https://github.com/pubnub/java/releases/tag/v5.1.1)
July-13-2021

- 🐛 Update Jackson libraries to avoid known vulnerabilities. 

## [v5.1.0](https://github.com/pubnub/java/releases/tag/v5.1.0)
May-20-2021

- 🌟️ Method grantToken has beed added. It allows generation of signed token with permissions for channels and channel groups. 
- 🐛 UUID is now exposed as PNMembership field which make is accessible from PNMembershipResult argument of SubscribeCallback.membership() method. 

## [v5.0.0](https://github.com/pubnub/java/releases/tag/v5.0.0)
May-12-2021

- 🌟️ Now random initialisation vector used when encryption enabled is now default behaviour. 
- 🐛 There were some non daemon threads running in background preventing VM from exiting. Now they are daemon threads. 

## [v4.36.0](https://github.com/pubnub/java/releases/tag/v4.36.0)
April-08-2021

- 🌟️ New way of controlling Presence by Heartbeat calls for purpose of usage with dedicated server configuration (ACL). This feature can be used only with additional support from PubNub. 

## [v4.33.3](https://github.com/pubnub/java/releases/tag/v4.33.3)
October-21-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.33.2...v4.33.3)

- 🐛 Improved handling of random initialization vector for encrypting messages. 
- 🐛 Restore Android compatibility for Gradle 3.X by removing Stringjoin(). 
- 🐛 Return appropriate error information when payload is too large. 

## [v4.33.2](https://github.com/pubnub/java/releases/tag/v4.33.2)
October-08-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.33.1...v4.33.2)

- 🐛 To improve security of messages, added support for random initialization vector to encrypt and decrypt messages. 

## [v4.33.1](https://github.com/pubnub/java/releases/tag/v4.33.1)
September-24-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.33.0...v4.33.1)

- 🐛 PubNubException now overrides Throwable's `getMessage` so the `status.errorData.throwablemessage` can be properly set. 

## [v4.33.0](https://github.com/pubnub/java/releases/tag/v4.33.0)
September-14-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.32.1...v4.33.0)

- 🌟️ Objects (v2) API exposed to enable metadata management. 
- 🌟️ Enable Objects (v2) related permissions management via Grant method. 

## [v4.32.1](https://github.com/pubnub/java/releases/tag/v4.32.1)
August-24-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.32.0...v4.32.1)

- 🐛 Fix for subscription loop to prevent unexpected disconnections caused by unhandled HTTP statuses. 

## [v4.32.0](https://github.com/pubnub/java/releases/tag/v4.32.0)
August-14-2020

- 🌟️ Allows to upload files to channels, download them with optional encryption support. 

## [v4.31.3](https://github.com/pubnub/java/releases/tag/v4.31.3)
June-17-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.31.2...v4.31.3)

- 🐛 Fix typo in suppressLeaveEvents in PNConfiguration. 

## [v4.31.2](https://github.com/pubnub/java/releases/tag/v4.31.2)
June-12-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.31.1...v4.31.2)

- 🌟 Add "click_action" parameter to PushPayloadHelper in order to pass it to FCM. 

## [v4.31.1](https://github.com/pubnub/java/releases/tag/v4.31.1)
April-16-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.31.0...v4.31.1)

- 🐛 Fix OkHttp reconnection policy. 

## [v4.31.0](https://github.com/pubnub/java/tree/v4.31.0)
February-25-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.30.0...v4.31.0)

- 🌟️ Implemented Objects Filtering API
- ⭐ Handled more network events to keep the client subscribed.
- ⭐ Improved interaction with classes from org.json*.
- ⭐ Made PNCallback eligible for SAM/lambda conversion.
- ⭐ Deprecated PNPushType.GCM in favor of PNPushType.FCM.

## [v4.30.0](https://github.com/pubnub/java/tree/v4.30.0)
January-23-2020

[Full Changelog](https://github.com/pubnub/java/compare/v4.29.2...v4.30.0)

- 🌟️ Add support for APNS2 Push API.
- 🌟️ Add a utility class to ease creating push payloads.

## [v4.29.2](https://github.com/pubnub/java/tree/v4.29.2)
  December-02-2019

  [Full Changelog](https://github.com/pubnub/java/compare/v4.29.1...v4.29.2)

- ⭐ Disable Okhttp retry on failure


## [v4.29.1](https://github.com/pubnub/java/tree/v4.29.1)
  October-23-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.29.0...v4.29.1)


- ⭐Made the SDK more Kotlin-friendly
- ⭐Categorized canceled requests as such
- ⭐Removed the ‘audit’ method


## [v4.29.0](https://github.com/pubnub/java/tree/v4.29.0)
  October-09-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.28.0...v4.29.0)


- ⭐Implemented the Message Actions API
- ⭐Added includeMeta() to history()
- ⭐Added includeMeta() to fetchMessages()
- ⭐Added includeMessageActions() to fetchMessages()


## [v4.28.0](https://github.com/pubnub/java/tree/v4.28.0)
  October-02-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.27.0...v4.28.0)


- ⭐Added PAMv3 support
- ⭐Added Token manager (TMS)
- ⭐Upgraded grant() and audit() to /v2/ endpoints
- ⭐Implemented the delete permission for grant() requests
- ⭐Implemented the v2 signature to be used for signing most requests


## [v4.27.0](https://github.com/pubnub/java/tree/v4.27.0)
  August-27-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.26.1...v4.27.0)


- ⭐Added Objects API support


## [v4.26.1](https://github.com/pubnub/java/tree/v4.26.1)
  August-14-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.26.0...v4.26.1)


- ⭐Introduced serialization class for Signals API


## [v4.26.0](https://github.com/pubnub/java/tree/v4.26.0)
  August-10-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.25.0...v4.26.0)


- ⭐Implemented Signals API
- ⭐Exposed OkHttp logging interceptor library


## [v4.25.0](https://github.com/pubnub/java/tree/v4.25.0)
  June-10-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.24.0...v4.25.0)


- ⭐Enabled app level grants
- ⭐Implemented custom encoding of the auth key for APIs where it wasn’t encoded automatically
- ⭐Attached state data to Subscribe API and removed it from heartbeats


## [v4.24.0](https://github.com/pubnub/java/tree/v4.24.0)
  May-22-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.23.0...v4.24.0)


- ⭐Enforced a minimum presence timeout value
- ⭐Disabled presence heartbeats by default
- ⭐Exposed Gson dependency


## [v4.23.0](https://github.com/pubnub/java/tree/v4.23.0)
  May-08-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.22.0...v4.23.0)


- ⭐Fixed reconnection logic by allowing it solely for network issues


## [v4.22.0](https://github.com/pubnub/java/tree/v4.22.0)
  April-26-2019


  [Full Changelog](https://github.com/pubnub/java/compare/v4.21.0...v4.22.0)


- ⭐Implemented Message Counts API
- ⭐Performed a major update of 3rd party libraries (e.g. Retrofit, OkHttp, Gson)
- ⭐Refactored and updated unit tests
- ⭐Replaced compile with implementation for 3rd party libraries.


## [v4.21.0](https://github.com/pubnub/java/tree/v4.21.0)
  October-30-2018


  [Full Changelog](https://github.com/pubnub/java/compare/v4.20.0...v4.21.0)


- ⭐Implemented a feature where you can add optional query params to every request
- ⭐Updated developer setup documentation
- ⭐Improved code checkstyle rules


## [v4.20.0](https://github.com/pubnub/java/tree/v4.20.0)
  September-04-2018


  [Full Changelog](https://github.com/pubnub/java/compare/v4.19.0...v4.20.0)


- ⭐Fix a bug where the global-here-now response was incorrectly interpreted


## [v4.19.0](https://github.com/pubnub/java/tree/v4.20.0)
  April-04-2018


  [Full Changelog](https://github.com/pubnub/java/compare/v4.18.0...v4.19.0)


- ⭐Fix an issue where end of channel history was interpreted as an error



## [v4.18.0](https://github.com/pubnub/java/tree/v4.18.0)
  January-11-2018


  [Full Changelog](https://github.com/pubnub/java/compare/v4.17.0...v4.18.0)


- ⭐lock down okHTTP version to support latest android version



## [v4.17.0](https://github.com/pubnub/java/tree/v4.17.0)
  December-19-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.16.0...v4.17.0)


- ⭐allow SDK to only send heartbeats without subscribing to the data channel.



## [v4.16.0](https://github.com/pubnub/java/tree/v4.16.0)
  November-21-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.15.0...v4.16.0)


- ⭐allow setting setMaximumConnections to open more connections to PubNub



## [v4.15.0](https://github.com/pubnub/java/tree/v4.15.0)
  November-17-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.14.0...v4.15.0)


- ⭐update gson dependency



- 🐛make listeners thread safe


- 🐛close hanging threads on shutdown


## [v4.14.0](https://github.com/pubnub/java/tree/v4.14.0)
  October-25-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.13.0...v4.14.0)


- ⭐add support to supress leave events



## [v4.13.0](https://github.com/pubnub/java/tree/v4.13.0)
  October-23-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.12.0...v4.13.0)


- ⭐do not execute subscribe on empty string channel, channel groups


- ⭐stop heartbeat loop if an error shows up.



## [v4.12.0](https://github.com/pubnub/java/tree/v4.12.0)
  October-05-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.11.0...v4.12.0)



- 🐛fix worker thread unloading.
- 🌟prevent concurrent modification of listeners.




## [v4.11.0](https://github.com/pubnub/java/tree/v4.11.0)
  October-05-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.10.0...v4.11.0)



- 🐛fix retrofit unloading.


## [v4.10.0](https://github.com/pubnub/java/tree/v4.10.0)
  September-17-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.9.1...v4.10.0)

- 🌟rework the loading of services to load the classes once.




## [v4.9.1](https://github.com/pubnub/java/tree/v4.9.1)
  August-14-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.9.0...v4.9.1)

- 🌟patch-up to the deduping algorithm




## [v4.9.0](https://github.com/pubnub/java/tree/v4.9.0)
  August-14-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.8.0...v4.9.0)

- 🌟Internal deduping mechanism when devices cross regions (dedupOnSubscribe).




## [v4.8.0](https://github.com/pubnub/java/tree/v4.8.0)
  August-08-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.7.0...v4.8.0)

- 🌟Allow certificate pinning via setCertificatePinner on PNConfiguration


- 🌟Allow disabling of heartbeat by setting the interval to 0.


- 🌟GAE fixes.




## [v4.7.0](https://github.com/pubnub/java/tree/v4.7.0)
  July-20-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.6.5...v4.7.0)

- 🌟Allow injection of httpLoggingInterceptor for extra logging monitoring..




## [v4.6.5](https://github.com/pubnub/java/tree/v4.6.5)
  June-28-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.6.4...v4.6.5)



- 🐛adjust queue exceeded notifications to be greater or equal of.


## [v4.6.4](https://github.com/pubnub/java/tree/v4.6.4)
  June-10-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.6.3...v4.6.4)



- 🐛gracefully handle disabled history


## [v4.6.3](https://github.com/pubnub/java/tree/v4.6.3)
  June-03-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.6.2...v4.6.3)

- 🌟on interval events, pass hereNowRefresh to indicate if a here_now fetch is needed.




## [v4.6.2](https://github.com/pubnub/java/tree/v4.6.2)
  April-13-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.6.1...v4.6.2)

- 🌟set a name for Subscription Manager Consumer Thead.




## [v4.6.1](https://github.com/pubnub/java/tree/v4.6.1)
  April-06-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.6.0...v4.6.1)



- 🐛SDK crash in Android with Airplane Mode
- 🌟add deltas on interval action.




## [v4.6.0](https://github.com/pubnub/java/tree/v4.6.0)
  March-14-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.5.0...v4.6.0)

- 🌟To distinguish UUID's that were generated by our SDK, we appended `pn-` before the UUID to signal that it's a randomly generated UUID.


- 🌟Allow the passing of okHttp connection spec via setConnectionSpec



- ⭐Bump retrofit to 2.2.0



## [v4.5.0](https://github.com/pubnub/java/tree/v4.5.0)
  February-15-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.4.4...v4.5.0)

- 🌟add .toString methods to all public facing models and POJOs




## [v4.4.4](https://github.com/pubnub/java/tree/v4.4.4)
  February-06-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.4.3...v4.4.4)

- 🌟Add support to configure host name verifier.




## [v4.4.3](https://github.com/pubnub/java/tree/v4.4.3)
  February-02-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.4.2...v4.4.3)

- 🌟Add support to configure custom certificate pinning via SSLSocketFactory and X509 configuration objects.




## [v4.4.2](https://github.com/pubnub/java/tree/v4.4.2)
  January-31-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.4.1...v4.4.2)



- 🐛SDK was not sending the user metadata on Message Callback


## [v4.4.1](https://github.com/pubnub/java/tree/v4.4.1)
  January-25-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.4.0...v4.4.1)



- 🐛SDK did not honor the exhaustion of reconnections, it will now disconnect once max retries happened


## [v4.4.0](https://github.com/pubnub/java/tree/v4.4.0)
  January-24-2017


  [Full Changelog](https://github.com/pubnub/java/compare/v4.3.1...v4.4.0)


- ⭐Support for maximum reconnection attempts


- ⭐Populate affectedChannel and affectedChannelGroups


- ⭐Support for GAE


- ⭐Emit pnconnected when adding / removing channels.



## [v4.3.1](https://github.com/pubnub/java/tree/v4.3.1)
  December-22-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.3.0...v4.3.1)


- ⭐support for key-level grant.



## [v4.3.0](https://github.com/pubnub/java/tree/v4.3.0)
  December-14-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.2.3...v4.3.0)


- ⭐JSON parser is switched to GSON, new artifact on nexus as pubnub-gson


- ⭐GetState, setState return a JsonElement instead of a plain object.



## [v4.2.3](https://github.com/pubnub/java/tree/v4.2.3)


  [Full Changelog](https://github.com/pubnub/java/compare/v4.2.2...v4.2.3)


- ⭐Swapping out logger for slf4japi and removing final methods



## [v4.2.2](https://github.com/pubnub/java/tree/v4.2.2)
  December-09-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.2.1...v4.2.2)


- ⭐remove final identifiers from the public facing API.



## [v4.2.1](https://github.com/pubnub/java/tree/v4.2.1)
  November-23-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.2.0...v4.2.1)


- ⭐include publisher UUID on incoming message


- ⭐allow to set custom TTL on a publish



## [v4.2.0](https://github.com/pubnub/java/tree/v4.2.0)
  October-25-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.1.0...v4.2.0)


- ⭐Signatures are generated for all requests with secret key to ensure secure transmission of data


- ⭐support for alerting of queue exceeded (PNRequestMessageCountExceededCategory)


- ⭐signaling to okhttp to stop the queues on termination.



## [v4.1.0](https://github.com/pubnub/java/tree/v4.1.0)
  October-12-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.14...v4.1.0)


- ⭐destory now correctly forces the producer thread to shut down; stop is now deprecated for disconnect


- ⭐support for sending instance id for presence detection (disabled by default)


- ⭐support for sending request id to burst cache (enabled by default)


- ⭐proxy support via the native proxy configurator class.



## [v4.0.14](https://github.com/pubnub/java/tree/v4.0.14)
  September-20-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.13...v4.0.14)


- ⭐on PAM error, populate the affectedChannel or affectedChannelGroup to signal which channels are failing



## [v4.0.13](https://github.com/pubnub/java/tree/v4.0.13)
  September-14-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.12...v4.0.13)


- ⭐populate jso with the error.



## [v4.0.12](https://github.com/pubnub/java/tree/v4.0.12)
  September-13-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.11...v4.0.12)



- 🐛fixing parsing of origination payload within the psv2 enevelope


## [v4.0.11](https://github.com/pubnub/java/tree/v4.0.11)
  September-09-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.10...v4.0.11)


- ⭐bumping build process for gradle 3 / merging documentation into the repo and test adjustments



## [v4.0.10](https://github.com/pubnub/java/tree/v4.0.10)
  September-07-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.9...v4.0.10)


- ⭐adding channel / channelGroup fields when a message / presence event comes in.



## [v4.0.9](https://github.com/pubnub/java/tree/v4.0.9)
  August-24-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.8...v4.0.9)


- ⭐adjustments for handling pn_other and decryption


- ⭐retrofit version bumps.



## [v4.0.8](https://github.com/pubnub/java/tree/v4.0.8)
  August-16-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.7...v4.0.8)

- 🌟added unsubscribeAll, getSubscribedChannels, getSubscribedChannelGroups


- 🌟SDK will establish secure connections by default


- 🌟added support for exponential backoff reconnection policies




## [v4.0.7](https://github.com/pubnub/java/tree/v4.0.7)
  August-11-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.6...v4.0.7)


- ⭐reduce overlap on error handling when returning exceptions.



## [v4.0.6](https://github.com/pubnub/java/tree/v4.0.6)
  July-18-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.5...v4.0.6)


- ⭐send heartbeat presence value when subscribing



## [v4.0.5](https://github.com/pubnub/java/tree/v4.0.5)
  July-07-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.4...v4.0.5)


- ⭐unified retrofit handling to lower amount of instances and sync'd the state methods.



## [v4.0.4](https://github.com/pubnub/java/tree/v4.0.4)
  June-24-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.3...v4.0.4)



- 🐛setting State for other UUID's is now supported.


## [v4.0.3](https://github.com/pubnub/java/tree/v4.0.3)
  June-15-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.2...v4.0.3)

- 🌟fire() method and no-replicaton options.




## [v4.0.2](https://github.com/pubnub/java/tree/v4.0.2)
  June-15-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.1...v4.0.2)



- 🐛fix to the version fetching.


## [v4.0.1](https://github.com/pubnub/java/tree/v4.0.1)
  June-06-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.0...v4.0.1)



- 🐛adjustment of the subscribe loop to alleviate duplicate dispatches.


## [v4.0.0](https://github.com/pubnub/java/tree/v4.0.0)
  June-03-2016


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.0-beta4...v4.0.0)



- 🐛first GA.


## [v4.0.0-beta4](https://github.com/pubnub/java/tree/v4.0.0-beta4)


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.0-beta3...v4.0.0-beta4)


- ⭐reconnects and minor adjustments.



## [v4.0.0-beta3](https://github.com/pubnub/java/tree/v4.0.0-beta3)


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.0-beta2...v4.0.0-beta3)



- 🐛fixing state not coming on the subscriber callback.


- 🐛adjustments to URL encoding on publish, subscribe, set-state operations to avoid double encoding with retrofit.


## [v4.0.0-beta2](https://github.com/pubnub/java/tree/v4.0.0-beta2)


  [Full Changelog](https://github.com/pubnub/java/compare/v4.0.0-beta1...v4.0.0-beta2)


- ⭐reworking of message queue.


- ⭐checkstyle, findbugs.


- ⭐reworking error notifications.



## [v4.0.0-beta1](https://github.com/pubnub/java/tree/v4.0.0-beta1)




- ⭐initial beta1.
