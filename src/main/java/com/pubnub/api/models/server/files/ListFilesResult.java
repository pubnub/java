package com.pubnub.api.models.server.files;

import com.pubnub.api.models.consumer.files.PNUploadedFile;
import lombok.Data;
import lombok.NonNull;

import java.util.Collection;

@Data
public class ListFilesResult {
    private final int count;
    private final String next;
    private final int status;
    @NonNull
    private final Collection<PNUploadedFile> data;
}
