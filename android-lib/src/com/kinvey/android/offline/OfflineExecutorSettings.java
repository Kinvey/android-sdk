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
package com.kinvey.android.offline;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;
import java.util.ArrayList;

import com.kinvey.android.offline.OfflineStore.RequestInfo;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class OfflineExecutorSettings implements Serializable {

    // Preference Constants
    public static final String BATCH_SIZE_PREFERENCE = "batchSize";
    public static final String STAGGER_TIME_PREFERENCE = "staggerTime";
    public static final String REQUIRE_WIFI_PREFERENCE = "requireWIFI";
    public static final String NEEDS_SYNC_PREFERENCE = "needsSync";
    
    //The number of milliseconds between each batch of client requests being executed.
    private long staggerTime = 1000L;
    //a flag indicating if the service should only execute calls on WIFI or if any network will suffice.
    private boolean requireWIFI = false;
    //The size of a batch, indicating how many async requests are executed at the same time.
    private int batchSize = 3;
    //a flag indicating if there is any pending work, currently tied to an OfflineStore.
    private boolean needsSync = false;
    private static OfflineExecutorSettings _instance;

    private SharedPreferences preferences;

    protected Object lock = new Object();

    private OfflineExecutorSettings(Context context){
        preferences = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        staggerTime = preferences.getLong(STAGGER_TIME_PREFERENCE, 1000L);
        requireWIFI = preferences.getBoolean(REQUIRE_WIFI_PREFERENCE,false);
        batchSize = preferences.getInt(BATCH_SIZE_PREFERENCE,3);
        needsSync = preferences.getBoolean(NEEDS_SYNC_PREFERENCE,false);
    }

    public static OfflineExecutorSettings getInstance(Context context) {
        synchronized (OfflineExecutorSettings.class) {
            if (_instance == null) {
                _instance = new OfflineExecutorSettings(context);
            }
        }
        return _instance;
    }

    public long getStaggerTime() {
        synchronized(lock) {
            return staggerTime;
        }
    }

    public void setStaggerTime(long staggerTime) {
        synchronized (lock) {
            this.staggerTime = staggerTime;
        }
    }

    public boolean isRequireWIFI() {
        synchronized (lock) {
            return requireWIFI;
        }
    }

    public void setRequireWIFI(boolean requireWIFI) {
        synchronized (lock) {
            this.requireWIFI = requireWIFI;
        }
    }

    public int getBatchSize() {
        synchronized (lock) {
            return batchSize;
        }
    }

    public void setBatchSize(int batchSize) {
        synchronized (lock) {
            this.batchSize = batchSize;
        }
    }

    public boolean isNeedsSync() {
        synchronized (lock) {
            return needsSync;
        }
    }

    public void setNeedsSync(boolean needsSync) {
        synchronized (lock) {
            this.needsSync = needsSync;
        }
    }

    public void savePreferences() {
        synchronized (lock) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(STAGGER_TIME_PREFERENCE, 1000L);
            editor.putBoolean(REQUIRE_WIFI_PREFERENCE,false);
            editor.putInt(BATCH_SIZE_PREFERENCE,3);
            editor.putBoolean(NEEDS_SYNC_PREFERENCE,false);
            editor.commit();
        }
    }
}
