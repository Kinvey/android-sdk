/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;

import com.google.common.base.Preconditions;
import com.kinvey.java.Logger;
import com.kinvey.java.auth.ClientUsers;

/**
 * @author mjsalinger
 * @since 2.0
 */
class AndroidClientUsers implements ClientUsers {
    private HashMap<String,String> userList;
    private String activeUser;
    private static AndroidClientUsers _instance;
    SharedPreferences userPreferences;
    Context appContext;

    private enum PersistData {
        USERLIST,
        ACTIVEUSER,
        BOTH
    }

    private AndroidClientUsers(Context context) {
        appContext = context.getApplicationContext();
        userPreferences = appContext.getSharedPreferences(
                appContext.getPackageName(), Context.MODE_PRIVATE);
        retrieveUsers();
        if (userList == null) {
            userList = new HashMap<String, String>();
        }
        activeUser = userPreferences.getString("activeUser","");
    }

    private void persistData(PersistData type) {
        SharedPreferences.Editor editor = userPreferences.edit();

        switch(type) {
            case USERLIST:
                persistUsers();
                break;
            case ACTIVEUSER:

                editor.putString("activeUser",activeUser);
                break;
            case BOTH:

                editor.putString("activeUser", activeUser);
                persistUsers();
                break;
            default:
                throw new IllegalArgumentException("Illegal PersistData argument");
        }
        editor.commit();

    }

    private synchronized void persistUsers() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new PersistUsers().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new PersistUsers().execute();
        }
    }

    synchronized static AndroidClientUsers getClientUsers(Context context) {
        if (_instance == null) {
            _instance = new AndroidClientUsers(context);
        }
        return _instance;
    }

    /** {@inheritDoc} */
    @Override
    public void addUser(String userID, String type) {
        userList.put(userID, type);
        persistData(PersistData.USERLIST);
    }

    /** {@inheritDoc} */
    @Override
    public void removeUser(String userID) {
        if(userID.equals(getCurrentUser())) {
            setCurrentUser(null);
        }
        userList.remove(userID);
        persistData(PersistData.BOTH);
    }

    /** {@inheritDoc} */
    @Override
    public void switchUser(String userID) {
        Preconditions.checkState(userList.containsKey(userID), "userID %s was not in the credential store", userID);
        activeUser = userID;
        persistData(PersistData.ACTIVEUSER);
    }

    /** {@inheritDoc} */
    @Override
    public void setCurrentUser(String userID) {
        Preconditions.checkState(userList.containsKey(userID), "userID %s was not in the credential store", userID);
        activeUser = userID;
        persistData(PersistData.ACTIVEUSER);
    }

    /** {@inheritDoc} */
    @Override
    public String getCurrentUser() {
        return activeUser;
    }

    /** {@inheritDoc} */
    @Override
    public String getCurrentUserType() {
        String userType = userList.get(activeUser);
        return userType == null ? "" : userType;
    }

    private void retrieveUsers() {
        FileInputStream fIn = null;
        ObjectInputStream in = null;

        try {
            fIn = appContext.openFileInput("kinveyUsers.bin");
            in = new ObjectInputStream(fIn);
            userList = (HashMap<String,String>) in.readObject();
        } catch (FileNotFoundException e) {
            //ignore we're probably initializing it
        } catch (Exception e) {
            // trap all exceptions and log
            // not propagating this exception
        	Logger.ERROR("Failed to initialize kinveyUsers.bin");
        } finally {
            try {
                if (fIn != null) {
                    fIn.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException io) {
            	Logger.ERROR("Failed to clean up resources while reading kinveyUser.bin");
            }
        }
    }

    private class PersistUsers extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            FileOutputStream fStream = null;
            ObjectOutputStream oStream = null;

            try {
                fStream = appContext.openFileOutput("kinveyUsers.bin", Context.MODE_PRIVATE);
                oStream = new ObjectOutputStream(fStream);

                oStream.writeObject(userList);


                Logger.INFO("Serialization of user successful");
            } catch (IOException e) {
            	Logger.ERROR(e.getMessage());
            } finally{

                try {
                    if (oStream != null) {
                        oStream.flush();
                    }
                    if (fStream != null) {
                        fStream.getFD().sync();
                    }
                } catch (IOException e) {
//                    e.printStackTrace();
                } finally {
                    try {
                        if (oStream != null) {
                            oStream.close();
                        }
                    } catch (IOException e) {
//                        e.printStackTrace();  
                    }

                }



            }

            return null;
        }
    }
}