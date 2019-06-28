package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.List;

public class KinveySyncSaveBatchResponse<T> extends GenericJson {

    private List<T> entityList;
    private List<KinveyBatchInsertError> errors;

    public KinveySyncSaveBatchResponse(List<T> entityList, List<KinveyBatchInsertError> errors) {
        this.entityList = entityList;
        this.errors = errors;
    }

    public List<T> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<T> entityList) {
        this.entityList = entityList;
    }

    public List<KinveyBatchInsertError> getErrors() {
        return errors;
    }

    public void setErrors(List<KinveyBatchInsertError> errors) {
        this.errors = errors;
    }
}
