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
package com.kinvey.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import com.google.common.base.Preconditions;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import com.kinvey.java.auth.Credential;
import com.kinvey.java.auth.CredentialStore;

/**
 * @author mjsalinger
 * @since 2.0
 */
class AndroidCredentialStore implements CredentialStore {
    private static final String TAG = AndroidCredentialStore.class.getSimpleName();

    SharedPreferences preferences;
    HashMap<String, Credential> credentials;
    Context appContext;

    AndroidCredentialStore(Context context) throws IOException, AndroidCredentialStoreException {
        appContext = context.getApplicationContext();
        credentials = new HashMap<String, Credential>();
        try {
            retrieveCredentialStore();
        } catch (ClassNotFoundException ex) {
            credentials = new HashMap<String, Credential>();
            new PersistCredentialStore().execute();
            throw new AndroidCredentialStoreException("Credential store corrupted and was rebuilt");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Credential load(String userId) throws IOException {
        return credentials.get(userId);
    }

    /** {@inheritDoc} */
    @Override
    public void store(String userId, Credential credential) throws IOException {
        Preconditions.checkNotNull(credential, "credential must not be null");
        Preconditions.checkNotNull(userId, "userId must not be null");

        credentials.put(userId, credential);
        new PersistCredentialStore().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void delete(String userId) {
        credentials.remove(userId);
        new PersistCredentialStore().execute();
    }

    private void retrieveCredentialStore() throws ClassNotFoundException {
        FileInputStream fIn = null;
        ObjectInputStream in = null;

        if (appContext.getFileStreamPath("kinveyCredentials.bin").exists()) {
            try {
                fIn = appContext.openFileInput("kinveyCredentials.bin");
                in = new ObjectInputStream(fIn);
                credentials = (HashMap<String, Credential>) in.readObject();
            } catch (IOException ex) {
                Log.w(TAG, "Corrupt credential store detected", ex);
            } finally {
                try {
                    if (fIn != null) {
                        fIn.close();
                    }

                    if (in != null) {
                        in.close();
                    }

                } catch (IOException ioe) {
                    Log.w(TAG, "Could not clean up resources", ioe);
                }
            }
        } else {
            new PersistCredentialStore().execute();
        }
    }

    private class PersistCredentialStore extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                FileOutputStream fStream = appContext.openFileOutput("kinveyCredentials.bin", Context.MODE_PRIVATE);
                ObjectOutputStream oStream = new ObjectOutputStream(fStream);

                oStream.writeObject(credentials);
                oStream.flush();
                fStream.getFD().sync();
                oStream.close();

                Log.v(Client.TAG,"Serialization success");
            } catch (Exception e) {
                Log.e(TAG, "Error on persisting credential store", e);
            }
            return null;
        }
    }
}
