package com.kinvey.java.model;


public class KinveyPullResponse extends AbstractKinveyExceptionsListResponse {

    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
