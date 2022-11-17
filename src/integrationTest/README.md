## Running integration test

Java SDK source contains source set with integration tests that can be run against real PubNub application. 
In order to run integration tests make sure the dedicated App is created on [PubNub's Admin Portal](https://admin.pubnub.com/).
To launch integration tests use Gradle's `integrationTest` task. 
Before doing this make sure that correct Subscribe Key is configured in `test.properties` file in project's root directory.
See contents of `test.properties.example` for more details. 

Parameters from `test.properties` file can be overriden by setting corresponding environment variables. Example: 
```$bash
> export subKey=<SUBSCRIBE_KEY>
> ./gradlew build integrationTest
```
In current version those tests are not plugged into CI pipeline and they need to be launched on demand.
