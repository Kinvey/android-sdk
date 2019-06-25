package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;

import java.util.List;

public class KinveySyncSaveBatchResponse extends GenericJson {

    private List<GenericJson> entityList;
    private List<KinveyBatchInsertError> errors;

    public KinveySyncSaveBatchResponse(List<GenericJson> entityList, List<KinveyBatchInsertError> errors) {
        this.entityList = entityList;
        this.errors = errors;
    }

    public List<GenericJson> getEntityList() {
        return entityList;
    }

    public void setEntityList(List<GenericJson> entityList) {
        this.entityList = entityList;
    }

    public List<KinveyBatchInsertError> getErrors() {
        return errors;
    }

    public void setErrors(List<KinveyBatchInsertError> errors) {
        this.errors = errors;
    }
}
