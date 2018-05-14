package com.kinvey.java.store;


import com.kinvey.java.Query;
import com.kinvey.java.model.KinveyReadResponse;

class PullTaskResponse {

    private KinveyReadResponse kinveyReadResponse;
    private Query query;

    PullTaskResponse(KinveyReadResponse kinveyReadResponse, Query query) {
        this.kinveyReadResponse = kinveyReadResponse;
        this.query = query;
    }

    KinveyReadResponse getKinveyReadResponse() {
        return kinveyReadResponse;
    }

    Query getQuery() {
        return query;
    }
}
