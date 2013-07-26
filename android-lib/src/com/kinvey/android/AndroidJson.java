/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android;

import android.os.Build;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

/**
 * @author m0rganic
 * @since 2.0.6
 */
class AndroidJson {

    //TODO(mbickle): make json factory configurable

    /** SDK 3.0 version build number. */
    private static final int HONEYCOMB = 11;

    /**
     * Returns a new json factory instance that is compatible with Android SDKs prior to Honeycomb.
     * <p>
     * Prior to Honeycomb, the {@link com.google.api.client.extensions.android.json.AndroidJsonFactory} implementation
     * didn't exist, and the GSON parser was preferred. However, starting with Honeycomb, the
     * {@link com.google.api.client.extensions.android.json.AndroidJsonFactory} implementation was added, which is basd
     * on the GSON library
     * </p>
     */
    public static JsonFactory newCompatibleJsonFactory() {
        return (Build.VERSION.SDK_INT >= HONEYCOMB) ? new AndroidJsonFactory() : new GsonFactory();
    }

}
