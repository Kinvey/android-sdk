package com.kinvey.java.store;

/**
 * Created by yuliya on 2/20/17.
 */

interface KinveyRealtimeCallback<T> {

    void onNext(T next);

    void onError(Exception e);

    void onStatus(KinveyRealtimeStatus status);

}
