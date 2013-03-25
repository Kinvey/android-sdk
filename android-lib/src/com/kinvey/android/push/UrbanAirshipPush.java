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
package com.kinvey.android.push;

import android.app.Application;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;

import java.io.IOException;

import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.Client;
import com.kinvey.java.core.KinveyClientCallback;

/**
 * Implementation of {@link com.kinvey.android.push.AbstractPush} for Urban Airship.
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#push()} convenience method.
 * </p>
 *
 * <p>
 * This API is used to enable push for a specific device, and to launch push when starting your Android application.  The
 * initialize method of this class *must* be called in the onCreate method of your Android Application class.  Push becomes
 * enabled when linking the user via the {@link com.kinvey.android.AsyncUser#registerPush()} method.
 * </p>
 *
 * <p>
 * Sample Usage:
 * <pre>
    PushOptions options = kinveyClient.push().getPushOptions(pushAppKey, pushAppSecret, true);
    kinveyClient.push().initialize(options, new KinveyClientCallback<Void>( {
        public void onFailure(Throwable t) { ... }
        public void onSuccess(Void v) { ... }
    });
 * </pre>
 * </p>
 * @author mjsalinger
 * @since 2.0
 */
public class UrbanAirshipPush extends AbstractPush {
    private UrbanAirshipPushOptions pushOptions;


    public UrbanAirshipPush(Client client) {
        super(client);
    }

    /**
     * Method to initialize push for a specific application.
     *
     * <p>This method is used to initialize push for the first time, and to enable
     * push on each application start.  This method *must* be called in the Application instance's onCreate method.
     * The method accepts an object of PushOptions which contain the PushAppKey and PushAppSecret obtained from the Kinvey
     * Developer Console.
     * </p>
     *
     * @param options Object of type {@link PushOptions}
     * @param currentApp Instance of the current {@link Application}
     * @return
     */
    @Override
    public UrbanAirshipPush initialize(PushOptions options, Application currentApp) {
        this.pushOptions = (UrbanAirshipPushOptions) options;
        final AirshipConfigOptions uaOpts = new AirshipConfigOptions();

        uaOpts.developmentAppKey = pushOptions.getPushAppKey();
        uaOpts.developmentAppSecret = pushOptions.getPushAppSecret();
        uaOpts.productionAppKey = pushOptions.getPushAppKey();
        uaOpts.productionAppSecret = pushOptions.getPushAppSecret();

        uaOpts.inProduction = pushOptions.isInProduction();
        uaOpts.gcmSender = pushOptions.getAPIKey();
        uaOpts.transport = pushOptions.getTransportType();

        UAirship.takeOff(currentApp, uaOpts);
        PushManager.shared().setIntentReceiver(getPushIntentReceiver() == null ? PushReceiver.class : getPushIntentReceiver());
        PushManager.enablePush();
        return this;
    }

    /**
     * Asynchronous wrapper to initialize push for a specific application.
     *
     * <p>This method creates an asynchronous request to initialize push for the first time, and to enable
     * push on each application start.  This method *must* be called in the Application instance's onCreate method.
     * The method accepts an object of PushOptions which contain the PushAppKey and PushAppSecret obtained from the Kinvey
     * Developer Console.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
        PushOptions options = kinveyClient.push().getPushOptions(pushAppKey, pushAppSecret, true);
        kinveyClient.push().initialize(options, new KinveyClientCallback<Void>( {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(Void v) { ... }
        });
     * </pre>
     * </p>
     *
     * @param options Object of type {@link PushOptions}
     * @param currentApp Instance of the current {@link Application}
     * @return
     */
    public <T> void initialize(PushOptions options, Application currentApp, KinveyClientCallback<T> callback) {
        new Initialize(options, currentApp, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    @Override
    public String getPushId() {
        PushPreferences preferences = PushManager.shared().getPreferences();
        return preferences.getPushId();
    }

    @Override
    public boolean isPushEnabled() {
        if (!UAirship.shared().isFlying() || PushManager.shared() == null) {
            return false;
        }

        return PushManager.shared().getPreferences().isPushEnabled();
    }

    /**
     * Disables push for the application
     */
    public void disablePush() {
        PushManager.disablePush();
    }

    /**
     * Asynchronous wrapper to disable push for the application
     *
     * @param callback KinveyClientCallback
     * @param <T>
     */
    public <T> void disablePush(KinveyClientCallback<T> callback) {
        new DisablePush(callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    @Override
    public UrbanAirshipPushOptions getPushOptions(String pushAppKey, String pushAppSecret, boolean inProduction) {
        return new UrbanAirshipPushOptions(pushAppKey, pushAppSecret, inProduction, null, UrbanAirshipPushOptions.TransportType.HELIUM);
    }

    private class DisablePush extends AsyncClientRequest<Void> {


        private DisablePush(KinveyClientCallback callback) {
            super(callback);
        }

        protected Void executeAsync() throws IOException {
            UrbanAirshipPush.this.disablePush();
            return null;
        }
    }

    private class Initialize extends AsyncClientRequest<Void> {

        private final Application currentApp;
        private final PushOptions options;

        private Initialize(PushOptions options, Application currentApp, KinveyClientCallback callback) {
            super(callback);
            this.currentApp = currentApp;
            this.options = options;
        }

        protected Void executeAsync() throws IOException {
            UrbanAirshipPush.this.initialize(pushOptions, currentApp);
            return null;
        }
    }
}
