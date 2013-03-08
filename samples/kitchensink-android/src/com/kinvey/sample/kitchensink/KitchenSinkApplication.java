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
package com.kinvey.sample.kitchensink;

import android.app.Application;

import com.kinvey.android.Client;
import com.kinvey.android.push.PushOptions;
import com.kinvey.android.push.UrbanAirshipPushOptions;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class KitchenSinkApplication extends Application {

    private Client myClient;

    // NOTE: When configuring push notifications you have to change the android package name of this app
    private String pushAppKey = "your_push_key";
    private String pushAppSecret = "your_push_secret";

    @Override
    public void onCreate() {
        super.onCreate();
        myClient = new Client.Builder(this.getApplicationContext()).build();
    }

    public void registerPush() {
        PushOptions options = myClient.push().getPushOptions(pushAppKey, pushAppSecret,
                false);
      //  myClient.push().setIntentReceiver(com.kinvey.sample.kitchensink.push.CustomPushReceiver.class);
        myClient.push().initialize(options, this);

    }

    public void setsClient(Client myClient) {
        this.myClient = myClient;
    }

    public Client getClient(){
        return this.myClient;
    }
}
