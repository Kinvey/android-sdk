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
package com.kinvey.java;

import android.content.Context;
import com.kinvey.java.auth.ClientUsers;

/**
 * @author edwardf
 */
public class JavaClientUsers implements ClientUsers {

    private static JavaClientUsers _instance;

    static JavaClientUsers getClientUsers() {
        if (_instance == null) {
            _instance = new JavaClientUsers();
        }
        return _instance;
    }

    private JavaClientUsers(){}


    @Override
    public void addUser(String userID, String type) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeUser(String userID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void switchUser(String userID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setCurrentUser(String userID) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getCurrentUser() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getCurrentUserType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
