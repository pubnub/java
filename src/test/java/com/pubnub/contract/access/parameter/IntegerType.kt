package com.pubnub.contract.access.parameter

import io.cucumber.java.ParameterType

@ParameterType("'(.*)'")
fun integer(string: String) = string.toInt()