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
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;


/**
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#push()} convenience method.
 * </p>
 *
 * <p>This class manages GCM Push for the current logged in user.  Use `gcm.enabled=true` in the `kinvey.properties` file to enable GCM.</p>
 *
 * sample usage:
 * <pre>
     kinveyClient.push().initialize(getApplicationContext());
 * </pre>
 *
 *<p>This code snippet will enable push notifications through GCM for the current logged in user.</p>
 *
 *
 * @author edwardf
 * @since 2.2
 */
public class GCMPush extends AbstractPush {

    public static String[] senderIDs = new String[0];
    private static boolean inProduction = false;

    public GCMPush(Client client, boolean inProduction, String... senderIDs) {
        super(client);
        this.senderIDs = senderIDs;
        this.inProduction = inProduction;
    }




    /**
     * Initialize GCM by registering the current user with both GCM as well as your backend at Kinvey.
     *
     * Note these operations are performed asynchronously, however there is no callback.  Instead, updates
     * are delegated to your custom `KinveyGCMService` which will handle any responses.
     *
     * @param currentApp - The current valid application context.
     * @return an instance of GCM push, initialized for the current user.
     */
    @Override
    public GCMPush initialize(Application currentApp) {
        return  this.initialize(null, currentApp);
    }


    /**
     * Initialize GCM by registering the current user with both GCM as well as your backend at Kinvey.
     *
     * Note these operations are performed asynchronously, however there is no callback.  Instead, updates
     * are delegated to your custom `KinveyGCMService` which will handle any responses.
     *
     * @param options - deprecated, pass {@code null}.
     * @param currentApp - The current valid application context.
     * @return an instance of GCM push, initialized for the current user.
     */
    @Override
    public GCMPush initialize(PushOptions options, Application currentApp) {

        //First check runtime and grab current registration ID
        GCMRegistrar.checkDevice(currentApp);
        GCMRegistrar.checkManifest(currentApp);

        final String regId = GCMRegistrar.getRegistrationId(currentApp);

        //if we are not registered on GCM, then register
        if (regId.equals("")) {
            Log.v(Client.TAG, "GCM - Registration Id is empty, about to use GCMRegistrar");
            //Registration comes back via an intent
            GCMRegistrar.register(currentApp, senderIDs);
            Log.v(Client.TAG, "GCM - just registered with the GCMRegistrar");
        } else if (!GCMRegistrar.isRegisteredOnServer(currentApp)) {
            //registered on GCM but not on Kinvey?
            Log.v(Client.TAG , "GCM - not registered on server, about to try!");

            KinveyGCMService.registerWithKinvey(getClient(), regId, true);
            Log.v(Client.TAG , "GCM - just registered with Kinvey");

        }
        return this;
    }







    /**
     * Get the Registration ID from GCM for the Client's current application context.
     *
     * Note if the current user is not registered, the registration ID will be an empty string.
     *
     * @return - the current user's GCM registration ID or an empty string ""
     */
    @Override
    public String getPushId() {
        if (getClient().getContext() == null){
            return "";
        }else{
            return GCMRegistrar.getRegistrationId(getClient().getContext());
        }
    }

    /**
     * Check to see if the current user is registered for GCM.  This checks both with GCM directly as well as with a Kinvey backend.
     *
     * As registration occurs asynchronously, ensure your `KinveyGCMService` has received the onRegister call first.
     *
     * @return true if current user is registered, false if they are not.
     */
    @Override
    public boolean isPushEnabled() {
        if (getClient().getContext() == null){
            return false;
        }
        String gcmID = GCMRegistrar.getRegistrationId(getClient().getContext());
        return (!gcmID.equals("") && getClient().user().containsKey("_push"));
    }

    /**
     * Unregisters the current user with GCM and removes all _push fields from the current user object.
     *
     * Unregistration is asynchronous, so use the `KinveyGCMService` to receive notification when unregistration has completed.
     *
     * @throws PushRegistrationException
     */
    @Override
    public void disablePush() throws PushRegistrationException {
        GCMRegistrar.unregister(getClient().getContext());
        getClient().user().remove("_push");
        getClient().user().update(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
               //no - op, doesn't matter here-- see KinveyGCMService
            }

            @Override
            public void onFailure(Throwable error) {
                //no - op, doesn't matter here-- see KinveyGCMService
            }
        });
    }


    /**
     * Is GCM Push configured for production or a dev environment?
     *
     * @return true if in production mode, false if not
     */
    @Override
    public boolean isInProduction() {
        return inProduction;
    }

    /**
     * Get a list of all sender IDs as an array
     *
     * @return an array of sender IDs
     */
    @Override
    public String[] getSenderIDs() {
        return senderIDs;
    }

    /**
     * This class is used to maintain metadata about the current GCM push configuration in the User collection.
     *
     *
     */
    public static class PushConfig extends GenericJson {

        @Key("GCM")
        private PushConfigField gcm;
        @Key("GCM_dev")
        private PushConfigField gcmDev;

        public PushConfig(){}


        public PushConfigField getGcm() {
            return gcm;
        }

        public void setGcm(PushConfigField gcm) {
            this.gcm = gcm;
        }

        public PushConfigField getGcmDev() {
            return gcmDev;
        }

        public void setGcmDev(PushConfigField gcmDev) {
            this.gcmDev = gcmDev;
        }
    }

    /**
     * Manages ids and notificationKeys for {@code PushConfig}
     *
     */
    public static class PushConfigField extends GenericJson{
        @Key
        private String[] ids;
        @Key("notification_key")
        private String notificationKey;

        public PushConfigField(){}

        public String[] getIds() {
            return ids;
        }

        public void setIds(String[] ids) {
            this.ids = ids;
        }

        public String getNotificationKey() {
            return notificationKey;
        }

        public void setNotificationKey(String notificationKey) {
            this.notificationKey = notificationKey;
        }
    }


}
