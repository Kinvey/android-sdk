package com.kinvey.android.async;

import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.store.AsyncDataStore;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 *  This implementation of an AsyncClientRequest is used to wrap the core app data API.
 *  It provides the ability to execute a given method with a given arguments using reflection.
 *
 */
public class AsyncRequest<T> extends AsyncClientRequest<T> {

    private Object scope;
    Method mMethod;
    Object[] args;


    public AsyncRequest(Object scope, Method method, KinveyClientCallback callback, Object... args) {
        super(callback);
        this.scope = scope;
        this.mMethod = method;
        this.args = args;
    }

    @Override
    public T executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
        T ret = (T)mMethod.invoke(scope, args);
        return ret;
    }
}
