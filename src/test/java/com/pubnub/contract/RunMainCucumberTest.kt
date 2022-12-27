package com.pubnub.contract

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
        features = ["src/test/resources"],
        tags = "not @skip and not @na=java and not @beta",
        plugin = ["pretty", "summary", "junit:build/reports/cucumber-reports/main.xml"]
)
class RunMainCucumberTest

@RunWith(Cucumber::class)
@CucumberOptions(
        features = ["../service-contract-mock/contract/features/access"],
        tags = "not @skip and not @na=java and @beta",
        plugin = ["pretty", "summary", "junit:build/reports/cucumber-reports/beta.xml"]
)
class RunBetaCucumberTest
