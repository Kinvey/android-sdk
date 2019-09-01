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
package com.kinvey.android

import android.os.Build

import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.kinvey.java.core.RawJsonFactory

/**
 * This class manages the JSON parser for the Android library, which can be set in `kinvey.properties` as the value of `parser`.
 *
 * @author m0rganic
 * @since 2.0.6
 */
class AndroidJson {

    enum class JSONPARSER {
        GSON,
        JACKSON,
        RAW;

        companion object {
            val options: String
                get() {
                    val values = StringBuilder()
                    for (p in JSONPARSER.values()) {
                        values.append("$p, ")
                    }

                    values.setLength(values.length - 2)

                    return values.toString()
                }
        }
    }

    companion object {

        //TODO(mbickle): make json factory configurable

        /** SDK 3.0 version build number.  */
        private const val HONEYCOMB = 11

        /**
         * Returns a new json factory instance that is compatible with Android SDKs prior to Honeycomb.
         *
         *
         * Prior to Honeycomb, the [com.google.api.client.extensions.android.json.AndroidJsonFactory] implementation
         * didn't exist, and the GSON parser was preferred. However, starting with Honeycomb, the
         * [com.google.api.client.extensions.android.json.AndroidJsonFactory] implementation was added, which is basd
         * on the GSON library
         *
         */
        fun newCompatibleJsonFactory(parser: JSONPARSER): JsonFactory {
            return when (parser) {
                JSONPARSER.JACKSON -> JacksonFactory()
                JSONPARSER.RAW -> RawJsonFactory()
                JSONPARSER.GSON -> AndroidJsonFactory()
                else -> AndroidJsonFactory()
            }
        }
    }
}
