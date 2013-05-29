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
import com.google.android.gcm.GCMRegistrar;

import static com.kinvey.android.push.GCMIntentService.*;

/**
 * @author edwardf
 * @since 2.0
 */
public abstract class AbstractGCMReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String messageType = "";
        messageType = intent.getExtras().getString(EXTRA_TYPE);
        if (messageType.equals((MESSAGE_REGISTERED))) {
            String gcmID = GCMRegistrar.getRegistrationId(context);
            onRegister(gcmID);
        } else if (messageType.equals(MESSAGE_UNREGISTERED)) {
            onUnregister();
        } else if (messageType.equals(MESSAGE_DELETE)) {
            int deleteCount = intent.getIntExtra(MESSAGE_DELETE_COUNT, 0);
            onDelete(deleteCount);
        } else if (messageType.equals(MESSAGE_ERROR)) {
            String error = intent.getStringExtra(MESSAGE_FROM_GCM);
            onError(error);
        } else if (messageType.equals(MESSAGE_FROM_GCM)) {
            String message = intent.getStringExtra(MESSAGE_FROM_GCM);
            onMessage(message);
        }
    }

    public abstract void onMessage(String message);
    public abstract void onError(String error);
    public abstract void onDelete(int deleteCount);
    public abstract void onRegister(String gcmID);
    public abstract void onUnregister();



}
