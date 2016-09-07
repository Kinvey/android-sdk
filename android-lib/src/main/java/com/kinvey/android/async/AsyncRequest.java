/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 *
 */

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


    public AsyncRequest(Object scope, Method method, KinveyClientCallback<T> callback, Object... args) {
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
