/*
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
package com.kinvey.android.offline;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.kinvey.android.Client;

import java.util.Random;

/**
 * @author edwardf
 * @since 2.0
 */
public class KinveySyncService extends Service {
    //allows clients to bind
    private final IBinder mBinder = new LocalBinder();
    private final String TAG = Client.TAG + " " + this.getClass().getSimpleName();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public KinveySyncService getService() {
            // Return this instance of LocalService so clients can call public methods
            return KinveySyncService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void ping(){
        Log.i(TAG, "ping success!");
    }

//    public void sync();

}
