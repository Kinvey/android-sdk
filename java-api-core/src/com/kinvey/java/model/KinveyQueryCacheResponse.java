package com.kinvey.java.model;

import java.util.List;

/**
 * Created by yuliya on 03/06/18.
 */

public class KinveyQueryCacheResponse<T> extends KinveyAbstractResponse {

    private List<T> deleted;

    private List<T> changed;

    private String requestTime;

    public List<T> getDeleted() {
        return deleted;
    }

    public void setDeleted(List<T> deleted) {
        this.deleted = deleted;
    }

    public List<T> getChanged() {
        return changed;
    }

    public void setChanged(List<T> changed) {
        this.changed = changed;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }
}
