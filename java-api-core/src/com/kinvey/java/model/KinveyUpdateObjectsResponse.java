package com.kinvey.java.model;

import com.google.api.client.util.Key;

import java.util.List;

public class KinveyUpdateObjectsResponse<T> extends KinveyErrorResponse {

    @Key
    private List<T> entities;

    @Key
    private List<KinveyPutItemError> errors;

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

    public List<KinveyPutItemError> getErrors() {
        return errors;
    }

    public void setErrors(List<KinveyPutItemError> errors) {
        this.errors = errors;
    }

    public boolean haveErrors() {
        return errors != null && !errors.isEmpty();
    }
}
