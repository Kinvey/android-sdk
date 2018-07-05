package com.kinvey.java.model;


public class KinveyDeltaSetCountResponse extends KinveyCountResponse {

    private String lastRequestTime;

    public String getLastRequestTime() {
        return lastRequestTime;
    }

    public void setLastRequestTime(String lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }
}
