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
package com.kinvey.samples.citywatch;

import android.app.Application;
import com.google.api.client.http.HttpTransport;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.kinvey.android.Client;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class CityWatchApplication extends Application {

    public static final String TAG = "Kinvey - CityWatch";
    private static final Level LOGGING_LEVEL = Level.FINEST;

    private Client kinveyClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // run the following comamnd to turn on verbose logging:
        //
        // adb shell setprop log.tag.HttpTransport DEBUG
        //
        Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);

        kinveyClient = new Client.Builder(this).build();
    }

    public Client getClient() {
        return kinveyClient;
    }
}
