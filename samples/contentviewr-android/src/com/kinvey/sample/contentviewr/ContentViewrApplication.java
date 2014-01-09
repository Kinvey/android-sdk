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
package com.kinvey.sample.contentviewr;

import android.app.Application;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.sample.contentviewr.model.ContentUser;

/**
 * @author edwardf
 */
public class ContentViewrApplication extends Application {


    private Client client;



    public Client getClient(){
        return client;
    }

    public void loadClient(KinveyUserCallback callback){
        if (client == null){
            client = new Client.Builder(getApplicationContext()).setRetrieveUserCallback(callback).setUserClass(ContentUser.class).build();
        }else{
            if(callback != null){
                if (client.user().isUserLoggedIn()){
                    callback.onSuccess(client.user());
                }else{
                    callback.onFailure(new NullPointerException(""));
                }

            }
        }
    }
}
