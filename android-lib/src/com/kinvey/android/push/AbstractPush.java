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
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushNotificationBuilder;

import com.kinvey.android.Client;


/**
 * @author mjsalinger
 * @since 2.0
 */
public abstract class AbstractPush {

    public static final String TAG = AbstractPush.class.getCanonicalName();
    public static final String EXTRA_APID = PushManager.EXTRA_APID;
    public static final String EXTRA_ALERT = PushManager.EXTRA_ALERT;
    public static final String EXTRA_PUSH_ID = PushManager.EXTRA_PUSH_ID;
    public static final String EXTRA_NOTIFICATION_ID = PushManager.EXTRA_NOTIFICATION_ID;
    public static final String EXTRA_REGISTRATION_VALID = PushManager.EXTRA_REGISTRATION_VALID;
    public static final String EXTRA_REGISTRATION_ERROR = PushManager.EXTRA_REGISTRATION_ERROR;

    public static final String ACTION_PUSH_RECEIVED = PushManager.ACTION_PUSH_RECEIVED;
    public static final String ACTION_NOTIFICATION_OPENED = PushManager.ACTION_NOTIFICATION_OPENED;
    public static final String ACTION_REGISTRATION_FINISHED = PushManager.ACTION_REGISTRATION_FINISHED;

    private Class<? extends BroadcastReceiver> pushReceiverClass;

    private Client client;

    private AbstractPush() {
    }

    protected AbstractPush(Client client) {
        this.client=client;
    }

    protected Client getClient() {
        return client;
    }

    public Class<? extends BroadcastReceiver> getPushIntentReceiver() {
        return pushReceiverClass;
    }

    public void setIntentReceiver(final Class<? extends BroadcastReceiver> receiver) {
        pushReceiverClass = receiver;
    }

    public void setNotificationBuilder(final PushNotificationBuilder builder) {
        PushManager.shared().setNotificationBuilder(builder);
    }

    public abstract AbstractPush initialize(PushOptions options, Application currentApp);
    public abstract String getPushId();
    public abstract boolean isPushEnabled();
    public abstract void disablePush() throws PushRegistrationException;
    public abstract PushOptions getPushOptions(String pushAppKey, String pushAppSecret, boolean inProduction);

}
