package org.openimis.imispolicies.network.request;

public class GetMasterDataExportRequest extends BaseGetBinaryRequest {

    public GetMasterDataExportRequest() {
        super("api/tools/extracts/download_master_data");
    }
}
