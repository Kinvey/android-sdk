/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android.callback;

import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyDeleteResponse;

import com.kinvey.android.Client;

/**
 * This class provides callbacks for an creation of the client.
 * <p>
 *  Creating a client requires disc operations which are performed asynchronously.
 * </p>
 *
 * @author mjsalinger
 * @since 2.0
 */
public interface KinveyClientBuilderCallback extends KinveyClientCallback<Client> {

    public void onSuccess(Client result);

    public void onFailure(Throwable error);
}