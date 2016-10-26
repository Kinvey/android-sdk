package com.kinvey.java.cache;

import com.kinvey.java.core.KinveyClientCallback;


public interface KinveyCachedClientCallback<T> extends KinveyClientCallback<T> {

    @Override
    void onSuccess(T result);

    @Override
    void onFailure(Throwable error);
}
