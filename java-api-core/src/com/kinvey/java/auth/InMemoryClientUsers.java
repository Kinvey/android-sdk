/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.auth;

import com.google.common.base.Preconditions;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class InMemoryClientUsers implements ClientUsers {

    private HashMap<String,String> userList;
    private String activeUser;
    private static InMemoryClientUsers _instance;


    private InMemoryClientUsers() {

        if (userList == null) {
            userList = new HashMap<String, String>();
        }

        if (activeUser == null) {
            activeUser = "";
        }
    }


    public static InMemoryClientUsers getClientUsers() {
        if (_instance == null) {
            _instance = new InMemoryClientUsers();
        }
        return _instance;
    }

    @Override
    public void addUser(String userID, String type) {
        userList.put(userID, type);
    }

    @Override
    public void removeUser(String userID) {
        if(userID.equals(getCurrentUser())) {
            setCurrentUser(null);
        }
        userList.remove(userID);
    }

    @Override
    public void switchUser(String userID) {
        Preconditions.checkState(userList.containsKey(userID), "userID %s was not in the credential store", userID);
        activeUser = userID;
    }

    @Override
    public void setCurrentUser(String userID) {
        Preconditions.checkState(userList.containsKey(userID), "userID %s was not in the credential store", userID);
        activeUser = userID;
    }

    @Override
    public String getCurrentUser() {
        return activeUser;
    }

    @Override
    public String getCurrentUserType() {
        String userType = userList.get(activeUser);
        return userType == null ? "" : userType;
    }
}
