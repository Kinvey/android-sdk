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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.kinvey.android.Client;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class PushReceiver extends BroadcastReceiver {
    private static final String TAG = "PushReceiver";

    /**
     * Broadcasts the PUSH-related message to the registered application receiver
     *
     * @param {@link Context}
     * @param {@link Intent} Contains PUSH message payload
     * @param action
     *            : Type of PUSH event (KCSPush.ACTION_PUSH_RECEIVED, KCSPush.ACTION_NOTIFICATION_OPENED, or
     *            KCSPush.ACTION_REGISTRATION_FINISHED)
     */
    private void broadcastToClient(final Context context, final Intent intent, final String action) {
        Client myClient = new Client.Builder(context).build();
        final Class<? extends BroadcastReceiver> receiverClass = myClient.push()
                .getPushIntentReceiver();
        if (receiverClass != null) {
            final Intent broadcast = new Intent(context, receiverClass);
            broadcast.setAction(action);
            broadcast.putExtras(intent.getExtras());
            context.sendBroadcast(broadcast);
        }
    }

    /**
     * Log the values sent in the payload's "extra" dictionary for debugging purposes
     *
     * @param intent
     *            A PushManager.ACTION_NOTIFICATION_OPENED or ACTION_PUSH_RECEIVED intent.
     */
    private void logPushExtras(final Intent intent) {
        final Set<String> keys = intent.getExtras().keySet();
        for (final String key : keys) {

            // ignore standard C2DM extra keys
            final List<String> ignoredKeys = Arrays.asList("collapse_key",// c2dm collapse key
                    "from",// c2dm sender
                    AbstractPush.EXTRA_NOTIFICATION_ID,// int id of generated notification (ACTION_PUSH_RECEIVED only)
                    AbstractPush.EXTRA_PUSH_ID,// internal UA push_legacy id
                    AbstractPush.EXTRA_ALERT);// ignore alert
            if (ignoredKeys.contains(key)) {
                continue;
            }
            Log.i(TAG, "push Notification Extra: [" + key + " : " + intent.getStringExtra(key)
                    + "]");
        }
    }

    /**
     * Called on receipt of broadcast. Initiates logging of PUSH info and broadcast of PUSH to application receiver
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "Received intent: " + intent.toString());
        final String action = intent.getAction();

        if (action.equals(AbstractPush.ACTION_PUSH_RECEIVED)) {

            final int id = intent.getIntExtra(AbstractPush.EXTRA_NOTIFICATION_ID, 0);

            Log.i(TAG,
                    "Received push_legacy notification. Alert: "
                            + intent.getStringExtra(AbstractPush.EXTRA_ALERT) + " [NotificationID=" + id
                            + "]");

            logPushExtras(intent);
            // call registered receiver
            broadcastToClient(context, intent, AbstractPush.ACTION_PUSH_RECEIVED);

        } else if (action.equals(AbstractPush.ACTION_NOTIFICATION_OPENED)) {

            broadcastToClient(context, intent, AbstractPush.ACTION_NOTIFICATION_OPENED);

        } else if (action.equals(AbstractPush.ACTION_REGISTRATION_FINISHED)) {
            Log.i(TAG, "Registration complete. APID:" + intent.getStringExtra(AbstractPush.EXTRA_APID)
                    + ". Valid: " + intent.getBooleanExtra(AbstractPush.EXTRA_REGISTRATION_VALID, false));

            // associate APID with current user if it exists

           // Push.linkPushIdToCurrentUser();
            broadcastToClient(context, intent, AbstractPush.ACTION_REGISTRATION_FINISHED);
        }
    }
}
