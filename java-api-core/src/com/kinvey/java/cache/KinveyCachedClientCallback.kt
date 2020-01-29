package com.kinvey.java.cache

import com.kinvey.java.core.KinveyClientCallback

/**
 *
 * @param <T> The type of Entity that is planned to be fetched
 */
@Deprecated("It is callback for deprecated DataStore type {@link com.kinvey.java.store.StoreType#CACHE}\n" + "  use {@link com.kinvey.java.store.StoreType#AUTO}")
interface KinveyCachedClientCallback<T> : KinveyClientCallback<T> {
    /**
     * Successfull callback that is invoked when that data successfully retreived
     * @param result - fetched Entity
     */
    override fun onSuccess(result: T?)

    /**
     * Error callback that send an application the error occured during fetch operation
     * @param error - the error occured
     */
    override fun onFailure(error: Throwable?)
}