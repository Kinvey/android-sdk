package com.kinvey.android.sync;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.model.AbstractKinveyExceptionsListResponse;
import com.kinvey.java.model.KinveyBatchInsertError;

import java.util.List;

public class KinveyPushBatchResponse extends KinveyPushResponse {

    private List<GenericJson> entities;
    private List<KinveyBatchInsertError> errors;

    public List<GenericJson> getEntities() {
        return entities;
    }

    public void setEntities(List<GenericJson> entities) {
        this.entities = entities;
    }

    public List<KinveyBatchInsertError> getErrors() {
        return errors;
    }

    public void setErrors(List<KinveyBatchInsertError> errors) {
        this.errors = errors;
    }
}
