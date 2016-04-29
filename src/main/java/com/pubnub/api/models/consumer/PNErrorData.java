package com.pubnub.api.models.consumer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PNErrorData {

    String information;
    Exception throwable;

}
