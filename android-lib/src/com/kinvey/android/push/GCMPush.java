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
import com.kinvey.android.Client;


/**
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#push()} convenience method.
 * </p>
 *
 * @author edwardf
 * @since 2.2
 */
public class GCMPush extends AbstractPush {
    private GCMPushOptions options;



    public static String[] senderIDs = new String[0];
    private static boolean devMode = false;

    public GCMPush(Client client, boolean devMode, String... senderIDs) {
        super(client);
        this.senderIDs = senderIDs;
        this.devMode = devMode;
    }

    @Override
    public GCMPush initialize(PushOptions options, Application currentApp) {

        this.options = (GCMPushOptions) options;
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
            Log.v(Client.TAG , "GCM - not regsitered on server, about to try!");

            KinveyGCMService.registerWithKinvey(getClient(), regId, true);
            Log.v(Client.TAG , "GCM - just registered with Kinvey");

        }
        return this;
    }







    /**
     * Get the Registration ID from GCM, or an empty String if there is no active Application Context
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

    @Override
    public void disablePush() throws PushRegistrationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PushOptions getPushOptions(String pushAppKey, String pushAppSecret, boolean inProduction) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInDevMode() {
        return devMode;
    }

    @Override
    public String[] getSenderIDs() {
        return senderIDs;
    }



}
