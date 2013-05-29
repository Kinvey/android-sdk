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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;


/**
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

//        currentApp.registerReceiver(mHandleMessageReceiver,
//                new IntentFilter(KINVEY_GCM_MESSAGE));

        final String regId = GCMRegistrar.getRegistrationId(currentApp);

        //if we are not registered on GCM, then register
        if (regId.equals("")) {
            //Registration comes back via an intent
            GCMRegistrar.register(currentApp, senderIDs);
        } else if (!GCMRegistrar.isRegisteredOnServer(currentApp)) {
            //registered on GCM but not on Kinvey?
            GCMIntentService.registerWithKinvey(getClient(), regId, true);
        }
        return this;
    }




//    private final BroadcastReceiver mHandleMessageReceiver =
//            new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    String messageType = "";
//                    messageType = intent.getExtras().getString(EXTRA_TYPE);
//                    if (messageType.equals((MESSAGE_REGISTERED))) {
//                        //registered with GCM, now it's time to register with Kinvey
//                        String gcmID = GCMRegistrar.getRegistrationId(context);
//                        registerWithKinvey(gcmID, true);
//                    } else if (messageType.equals(MESSAGE_UNREGISTERED)) {
//                        //unregistered with GCM, not it's time to unregister with Kinvey
//                        String gcmID = GCMRegistrar.getRegistrationId(context);
//                        registerWithKinvey(gcmID, false);
//                    }
//
//                    //All we care about here is register/unregister so we can perform the same action against Kinvey
//                    //So, now we just rewrap the intent and pass it on to the next (optional) receiver
//                    //TODO rebroadcast intent
//
//
//                }
//            };

//    static void notifyReceiverOfRegistrationState(Context context, String message) {
//        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
//        intent.putExtra(EXTRA_TYPE, message);
//        context.sendBroadcast(intent);
//    }
//
//    static void notifyReceiverOfPushMessage(Context context, String message) {
//        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
//        intent.putExtra(EXTRA_TYPE, MESSAGE_FROM_GCM);
//        intent.putExtra(MESSAGE_FROM_GCM, message);
//        context.sendBroadcast(intent);
//    }
//
//    static void notifyReceiverOfDeletion(Context context, int count) {
//        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
//        intent.putExtra(EXTRA_TYPE, MESSAGE_DELETE);
//        intent.putExtra(MESSAGE_DELETE_COUNT, count);
//        context.sendBroadcast(intent);
//    }
//
//    static void notifyReceiverOfError(Context context, String errorMessage) {
//        Intent intent = new Intent(KINVEY_GCM_MESSAGE);
//        intent.putExtra(EXTRA_TYPE, MESSAGE_ERROR);
//        intent.putExtra(MESSAGE_FROM_GCM, errorMessage);
//        context.sendBroadcast(intent);
//    }


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

    @Override
    public boolean isPushEnabled() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
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
//
//    public static class PushConfig extends GenericJson {
//
//        @Key("GCM")
//        private PushConfigField gcm;
//        @Key("GCM_dev")
//        private PushConfigField gcmDev;
//
//        public PushConfig() {
//        }
//
//
//        public PushConfigField getGcm() {
//            return gcm;
//        }
//
//        public void setGcm(PushConfigField gcm) {
//            this.gcm = gcm;
//        }
//
//        public PushConfigField getGcmDev() {
//            return gcmDev;
//        }
//
//        public void setGcmDev(PushConfigField gcmDev) {
//            this.gcmDev = gcmDev;
//        }
//    }
//
//    public static class PushConfigField extends GenericJson {
//        @Key
//        private String[] ids;
//        @Key("notification_key")
//        private String notificationKey;
//
//        public PushConfigField() {
//        }
//
//        public String[] getIds() {
//            return ids;
//        }
//
//        public void setIds(String[] ids) {
//            this.ids = ids;
//        }
//
//        public String getNotificationKey() {
//            return notificationKey;
//        }
//
//        public void setNotificationKey(String notificationKey) {
//            this.notificationKey = notificationKey;
//        }
//    }


}
