package com.kinvey.java.store;


import com.kinvey.java.Query;
import com.kinvey.java.model.KinveyAbstractReadResponse;

class PullTaskResponse {

    private KinveyAbstractReadResponse kinveyAbstractReadResponse;
    private Query query;

    PullTaskResponse(KinveyAbstractReadResponse kinveyAbstractReadResponse, Query query) {
        this.kinveyAbstractReadResponse = kinveyAbstractReadResponse;
        this.query = query;
    }

    KinveyAbstractReadResponse getKinveyAbstractReadResponse() {
        return kinveyAbstractReadResponse;
    }

    Query getQuery() {
        return query;
    }
}
