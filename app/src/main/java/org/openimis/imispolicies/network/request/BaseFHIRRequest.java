package org.openimis.imispolicies.network.request;


import androidx.annotation.NonNull;

public abstract class BaseFHIRRequest extends BaseRequest {

    protected BaseFHIRRequest(@NonNull String endpoint) {
        super("api/api_fhir_r4/" + endpoint+"/");
    }
}
