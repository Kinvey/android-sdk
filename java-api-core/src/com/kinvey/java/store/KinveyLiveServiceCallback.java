package com.kinvey.java.store;

/**
 * Created by yuliya on 2/20/17.
 */

interface KinveyLiveServiceCallback<T> {

    void onNext(T next);

    void onError(Exception e);

    void onStatus(KinveyLiveServiceStatus status);

}
