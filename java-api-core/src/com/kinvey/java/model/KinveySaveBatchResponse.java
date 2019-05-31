package com.kinvey.java.model;

import com.google.api.client.util.Key;

import java.util.List;

public class KinveySaveBatchResponse<T> extends KinveyErrorResponse {
    @Key
    private List<T> entities;
    @Key
    private List<KinveyBatchInsertError> errors;

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

    public List<KinveyBatchInsertError> getErrors() {
        return errors;
    }

    public void setErrors(List<KinveyBatchInsertError> errors) {
        this.errors = errors;
    }
}
