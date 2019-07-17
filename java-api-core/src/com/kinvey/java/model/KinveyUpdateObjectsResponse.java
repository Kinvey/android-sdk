package com.kinvey.java.model;

import com.google.api.client.util.Key;

import java.util.List;

public class KinveyUpdateObjectsResponse<T> extends KinveyErrorResponse {

    @Key
    private List<T> entities;

    @Key
    private List<KinveyUpdateSingleItemError> errors;

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

    public List<KinveyUpdateSingleItemError> getErrors() {
        return errors;
    }

    public void setErrors(List<KinveyUpdateSingleItemError> errors) {
        this.errors = errors;
    }

    public boolean haveErrors() {
        return errors != null && !errors.isEmpty();
    }
}
