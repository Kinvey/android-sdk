/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
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
    public abstract AbstractPush initialize(Application currentApp);
    public abstract String getPushId();
    public abstract boolean isPushEnabled();
    public abstract void disablePush() throws PushRegistrationException;
    public PushOptions getPushOptions(String pushAppKey, String pushAppSecret, boolean inProduction){return null;}
    public abstract boolean isInProduction();
    public abstract String[] getSenderIDs();


}
