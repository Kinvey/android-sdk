package com.kinvey.android;/*
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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialStore;

import java.io.IOException;

/**
 * Implementation of a {@link com.kinvey.java.auth.CredentialStore} utilizing a private instance of Android's Shared Preferences
 *
 *
 * @author edwardf
 */
public class SharedPrefCredentialStore implements CredentialStore{

    private static final String PREF_STORE = "kinvey_shared_preferences_";
    private static final String PREF_ID = "kinvey_id";
    private static final String PREF_AUTH = "kinvey_auth";

    private Context context;

    public SharedPrefCredentialStore(Context context){
        this.context = context;
    }

    @Override
    public Credential load(String userId) throws IOException {
        SharedPreferences pref = context.getSharedPreferences(PREF_STORE + userId, Activity.MODE_PRIVATE);
        String id = pref.getString(PREF_ID, null);
        String auth = pref.getString(PREF_AUTH, null);
        if (id != null && auth != null){
            return new Credential(id, auth);
        }
        return null;
    }

    @Override
    public void store(String userId, Credential credential) throws IOException {
        SharedPreferences.Editor edit = context.getSharedPreferences(PREF_STORE + userId, Activity.MODE_PRIVATE).edit();
        edit.putString(PREF_ID, userId);
        edit.putString(PREF_AUTH, credential.getAuthToken());
        edit.commit();
    }

    @Override
    public void delete(String userId) {
        SharedPreferences.Editor edit = context.getSharedPreferences(PREF_STORE + userId, Activity.MODE_PRIVATE).edit();
        edit.remove(PREF_ID);
        edit.remove(PREF_AUTH);
        edit.commit();


    }
}
