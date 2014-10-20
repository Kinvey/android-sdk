/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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
package com.kinvey.android;

import android.os.Build;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.kinvey.java.core.RawJsonFactory;

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
    public static JsonFactory newCompatibleJsonFactory(JSONPARSER parser) {
        switch (parser){
            case GSON:
                return (Build.VERSION.SDK_INT >= HONEYCOMB) ? new AndroidJsonFactory() : new GsonFactory();
            case JACKSON:
                return new JacksonFactory();
            case RAW:
                return new RawJsonFactory();
            default:
                return (Build.VERSION.SDK_INT >= HONEYCOMB) ? new AndroidJsonFactory() : new GsonFactory();
        }

    }


    public enum JSONPARSER {
        GSON,
        JACKSON,
        RAW;

        public static String getOptions(){
            StringBuilder values = new StringBuilder();
            for (JSONPARSER p : JSONPARSER.values()){
                values.append(p + ", ");
            }

            values.setLength(values.length() - 2);

            return values.toString();
        }
    }



}
