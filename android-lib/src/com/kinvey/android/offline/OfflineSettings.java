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
import android.util.Log;
import com.kinvey.android.Client;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * @author mjsalinger
 * @since 2.0
 */
public class OfflineSettings implements Serializable {

    // Preference Constants
    public static final String BATCH_SIZE_PREFERENCE = "batchSize";
    public static final String STAGGER_TIME_PREFERENCE = "staggerTime";
    public static final String REQUIRE_WIFI_PREFERENCE = "requireWIFI";
    public static final String NEEDS_SYNC_PREFERENCE = "needsSync";
    public static final String COLLECTION_NAME_SET = "colectionNameSet";
    public static final String PSUEDO_LOCK = "psuedoLock";
    
    //The number of milliseconds between each batch of client requests being executed.
    private long staggerTime = 1000L;
    //a flag indicating if the service should only execute calls on WIFI or if any network will suffice.
    private boolean requireWIFI = false;
    //The size of a batch, indicating how many async requests are executed at the same time.
    private int batchSize = 3;
    //a flag indicating if there is any pending work, currently tied to an OfflineStore.
    private boolean needsSync = false;
    //a set of collections that have offlinestores
    private Set<String> collectionSet = new HashSet<String>();
    //a psuedo lock, indicating if service should be allowed to run indefinately or not
    private boolean psuedoLock = false;

    private static OfflineSettings _instance;

    private SharedPreferences preferences;

    protected Object lock = new Object();

    private OfflineSettings(Context context){
        preferences = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        staggerTime = preferences.getLong(STAGGER_TIME_PREFERENCE, 1000L);
        requireWIFI = preferences.getBoolean(REQUIRE_WIFI_PREFERENCE, false);
        batchSize = preferences.getInt(BATCH_SIZE_PREFERENCE, 3);
        needsSync = preferences.getBoolean(NEEDS_SYNC_PREFERENCE, false);
        collectionSet = preferences.getStringSet(COLLECTION_NAME_SET, new HashSet<String>());
        psuedoLock = preferences.getBoolean(PSUEDO_LOCK, false);
        savePreferences();
    }

    public static OfflineSettings getInstance(Context context) {
        synchronized (OfflineSettings.class) {
            if (_instance == null) {
                _instance = new OfflineSettings(context);
            }
        }
        return _instance;
    }

    public long getStaggerTime() {
        synchronized(lock) {
            return staggerTime;
        }
    }

    public OfflineSettings setStaggerTime(long staggerTime) {
        synchronized (lock) {
            this.staggerTime = staggerTime;
        }
        return this;
    }

    public boolean isRequireWIFI() {
        synchronized (lock) {
            return requireWIFI;
        }
    }

    public OfflineSettings setRequireWIFI(boolean requireWIFI) {
        synchronized (lock) {
            this.requireWIFI = requireWIFI;
        }
        return this;
    }

    public int getBatchSize() {
        synchronized (lock) {
            return batchSize;
        }
    }

    public OfflineSettings setBatchSize(int batchSize) {
        synchronized (lock) {
            this.batchSize = batchSize;
        }
        return this;
    }

    public boolean isNeedsSync() {
        synchronized (lock) {
            return needsSync;
        }
    }

    public OfflineSettings setNeedsSync(boolean needsSync) {
        synchronized (lock) {
            this.needsSync = needsSync;
        }
        return this;
    }

    public boolean isPsuedoLock(){
        synchronized (lock){
            return psuedoLock;
        }
    }

    public OfflineSettings setPsuedoLock(boolean lockIt){
        synchronized (lock){
            this.psuedoLock = lockIt;
        }
        return this;

    }

    public void savePreferences() {
        synchronized (lock) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(STAGGER_TIME_PREFERENCE, staggerTime);
            editor.putBoolean(REQUIRE_WIFI_PREFERENCE, requireWIFI);
            editor.putInt(BATCH_SIZE_PREFERENCE, batchSize);
            editor.putBoolean(NEEDS_SYNC_PREFERENCE, needsSync);
            editor.putStringSet(COLLECTION_NAME_SET, collectionSet);
            editor.putBoolean(PSUEDO_LOCK, psuedoLock);
            Log.v(Client.TAG,  "saving preferences for collection count: " + collectionSet.size());
            editor.commit();
        }
    }

    public Set<String> getCollectionSet() {
        synchronized (lock){
            return collectionSet;
        }
    }

    public OfflineSettings setCollectionSet(Set<String> collectionSet) {
        synchronized (lock){
            this.collectionSet = collectionSet;
        }
        return this;
    }
}
