package com.kinvey.java.model;

import com.google.api.client.util.Key;

public class KinveyCountResponse extends AbstractKinveyHeadersResponse {

    @Key
    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count){
        this.count = count;
    }
}
