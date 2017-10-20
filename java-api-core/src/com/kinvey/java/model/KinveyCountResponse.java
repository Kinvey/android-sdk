package com.kinvey.java.model;

import com.google.api.client.util.Key;

public class KinveyCountResponse {
    @Key
    private int count;

    /**
     * @return The number of objects successfully deleted.
     */
    public int getCount() {
        return count;
    }

    public void setCount(int count){
        this.count = count;
    }
}
