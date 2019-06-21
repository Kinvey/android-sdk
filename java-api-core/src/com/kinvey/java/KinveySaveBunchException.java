package com.kinvey.java;

import com.kinvey.java.model.KinveyBatchInsertError;

import java.io.IOException;
import java.util.List;

public class KinveySaveBunchException extends IOException {

    private List<KinveyBatchInsertError> errors;
    private List entities;

    public KinveySaveBunchException(List<KinveyBatchInsertError> errors, List entities) {
        this.errors = errors;
        this.entities = entities;
    }

    public List<KinveyBatchInsertError> getErrors() {
        return errors;
    }

    public void setErrors(List<KinveyBatchInsertError> errors) {
        this.errors = errors;
    }

    public List getEntities() {
        return entities;
    }

    public void setEntities(List entities) {
        this.entities = entities;
    }
}
