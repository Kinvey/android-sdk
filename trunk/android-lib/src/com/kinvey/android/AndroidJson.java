/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
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
