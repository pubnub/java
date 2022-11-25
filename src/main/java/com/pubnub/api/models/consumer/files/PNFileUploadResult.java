package com.pubnub.api.models.consumer.files;

import lombok.Data;
import lombok.NonNull;

@Data
public class PNFileUploadResult {
    private final long timetoken;
    private final int status;
    @NonNull
    private final PNBaseFile file;
}
