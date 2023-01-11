package com.pubnub.api.models.consumer;

import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Builder(toBuilder = true)
@Getter
@ToString
public class PNStatus {

    private PNStatusCategory category;
    private PNErrorData errorData;
    private boolean error;

    private int statusCode;
    private PNOperationType operation;

    private boolean tlsEnabled;

    private String uuid;
    private String authKey;
    private String origin;
    private Object clientRequest;

    // send back channel, channel groups that were affected by this operation
    @Nullable
    private List<String> affectedChannels;
    @Nullable
    private List<String> affectedChannelGroups;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private RemoteAction<?> executedEndpoint;


    public void retry() {
        executedEndpoint.retry();
    }

}
