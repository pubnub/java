package com.pubnub.api.models.server.presence;

import lombok.Data;

import java.util.List;

@Data
public class WhereNowPayload {
    private List<String> channels;
}
