package com.pubnub.api.models.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Envelope<T> {
    int status;
    String message;
    String service;
    T payload;
    int occupancy;
    Object uuids;
    String action;

    boolean error;

}
