package com.pubnub.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import retrofit2.Call;

@Builder
@Getter
public class PubNubException extends Exception {
    private String errormsg = "";
    private PubNubError pubnubError;
    private JsonNode jso;
    private String response;
    private int statusCode;

    @Getter(AccessLevel.NONE)
    private Call affectedCall;
}