package com.pubnub.api.models.consumer;

import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PNStatus {

    PNStatusCategory category;
    PNErrorData errorData;
    boolean error;

    // boolean automaticallyRetry;

    private int statusCode;
    private PNOperationType operation;
    private boolean TLSEnabled;
    private String uuid;
    private String authKey;
    private String origin;
    private Object clientRequest;

    // send back channel, channel groups that were affected by this operation
    List<String> affectedChannels;
    List<String> affectedChannelGroups;

    /*
    public void retry(){
        // TODO
    }

    public void cancelAutomaticRetry() {
        // TODO
    }
    */

}
