package com.kinvey.java;

import com.kinvey.java.model.KinveyBatchInsertError;
import com.kinvey.java.model.KinveyPutItemError;

import java.io.IOException;
import java.util.List;

public class KinveySaveBatchException extends IOException {

    private List<KinveyBatchInsertError> postErrors;
    private List<KinveyPutItemError> putErrors;
    private List entities;

    public KinveySaveBatchException(List<KinveyBatchInsertError> postErrors, List<KinveyPutItemError> putErrors, List entities) {
        this.postErrors = postErrors;
        this.putErrors = putErrors;
        this.entities = entities;
    }

    public List getEntities() {
        return entities;
    }

    public void setEntities(List entities) {
        this.entities = entities;
    }

    public List<KinveyBatchInsertError> getErrors() {
        return postErrors;
    }

    public void setErrors(List<KinveyBatchInsertError> errors) {
        this.postErrors = errors;
    }

    public void setPutErrors(List<KinveyPutItemError> putErrors) {
        this.putErrors = putErrors;
    }

    public List<KinveyPutItemError> getPutErrors() {
        return putErrors;
    }

    public boolean haveErrors() {
        return (postErrors != null && !postErrors.isEmpty()) ||
                (putErrors != null && !putErrors.isEmpty());
    }
}
