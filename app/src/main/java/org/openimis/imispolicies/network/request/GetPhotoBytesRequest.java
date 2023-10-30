package org.openimis.imispolicies.network.request;

import androidx.annotation.NonNull;

public class GetPhotoBytesRequest extends BaseGetBinaryRequest {

    public GetPhotoBytesRequest(
            @NonNull String photoName
    ) {
        super("Images/Updated/" + photoName);
    }
}
