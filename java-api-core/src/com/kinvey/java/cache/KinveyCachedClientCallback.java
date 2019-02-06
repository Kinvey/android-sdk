package com.kinvey.java.cache;

import com.kinvey.java.core.KinveyClientCallback;

/**
 *
 * @param <T> The type of Entity that is planned to be fetched
 * @deprecated
 */
@Deprecated
public interface KinveyCachedClientCallback<T> extends KinveyClientCallback<T> {

    /**
     * Successfull callback that is invoked when that data successfully retreived
     * @param result - fetched Entity
     */
    @Override
    void onSuccess(T result);

    /**
     * Error callback that send an application the error occured during fetch operation
     * @param error - the error occured
     */
    @Override
    void onFailure(Throwable error);
}
