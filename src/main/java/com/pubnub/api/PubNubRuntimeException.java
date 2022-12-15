package com.pubnub.api;

import com.google.gson.JsonElement;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import retrofit2.Call;

@Getter
@ToString
public class PubNubRuntimeException extends RuntimeException {
    private String errormsg;
    private PubNubError pubnubError;
    private JsonElement jso;
    private String response;
    private int statusCode;
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private Call affectedCall;

    @Builder
    public PubNubRuntimeException(final String errormsg,
                                  final PubNubError pubnubError,
                                  final JsonElement jso,
                                  final String response,
                                  final int statusCode,
                                  final Call affectedCall,
                                  final Throwable cause) {
        super(cause);
        this.errormsg = errormsg;
        this.pubnubError = pubnubError;
        this.jso = jso;
        this.response = response;
        this.statusCode = statusCode;
        this.affectedCall = affectedCall;
    }

    @Override
    @ToString.Include
    public Throwable getCause() {
        return super.getCause();
    }

    @Override
    public String getMessage() {
        return errormsg;
    }
}
