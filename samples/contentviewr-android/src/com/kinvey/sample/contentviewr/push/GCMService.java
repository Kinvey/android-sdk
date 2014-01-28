/*
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.kinvey.sample.contentviewr.push;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.kinvey.android.push.KinveyGCMService;
import com.kinvey.sample.contentviewr.R;

/**
 * @author edwardf
 */
public class GCMService extends KinveyGCMService {
    @Override
    public void onMessage(String message) {
        Log.i("PUSH", "MESSAGE RECIEVED!");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(getApplicationContext().getResources().getString(R.string.app_name))
                        .setContentText(message);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());

    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onDelete(int deleteCount) {

    }

    @Override
    public void onRegistered(String gcmID) {

    }

    @Override
    public void onUnregistered(String oldID) {

    }
}
