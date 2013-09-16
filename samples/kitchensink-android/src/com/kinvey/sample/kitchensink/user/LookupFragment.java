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
package com.kinvey.sample.kitchensink.user;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.kinvey.android.AsyncUser;
import com.kinvey.android.AsyncUserDiscovery;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.model.UserLookup;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author edwardf
 */
public class LookupFragment extends UseCaseFragment implements  View.OnClickListener{

    private Button lookup;
    private Button updateUser;





    @Override
    public int getViewID() {
        return R.layout.feature_user_lookup;
    }

    @Override
    public void bindViews(View v) {
        lookup = (Button) v.findViewById(R.id.user_lookup_lookup);
        lookup.setOnClickListener(this);
        updateUser = (Button) v.findViewById(R.id.user_lookup_update);
        updateUser.setOnClickListener(this);
    }

    @Override
    public String getTitle() {
        return "Lookup";
    }


    private void performUserUpdate(){

        AsyncUser user = getApplicationContext().getClient().user();
        user.put("last_name", "Smith");
        user.update(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                Toast.makeText(getSherlockActivity(), "updated user!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "update failed -> " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });





    }


    private void performUserLookup(){

        AsyncUserDiscovery users = getApplicationContext().getClient().userDiscovery();
        UserLookup criteria = users.userLookup();
        criteria.setLastName("Smith");
        users.lookup(criteria, new KinveyUserListCallback() {
            @Override
            public void onSuccess(User[] result) {
                Toast.makeText(getSherlockActivity(), "lookup returned: " + result.length + " user(s)!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "lookup failed -> " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void performRetrievalWithReferences(){


        AsyncUser user = getClient().user();

        user.retrieve(new String[]{"ref"}, new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                Log.i("got user", result.toString());

            }

            @Override
            public void onFailure(Throwable error) {
                Log.i("failed", error.getMessage());
                error.printStackTrace();;
            }
        });


        Query q = new Query();
        q.equals("username", "keepon");
        user.retrieve(q, new String[]{"ref"}, new KinveyUserListCallback() {
            @Override
            public void onSuccess(User[] result) {
                Log.i("got users", result.toString());
            }

            @Override
            public void onFailure(Throwable error) {
                Log.i("failed", error.getMessage());
                error.printStackTrace();;
            }
        });



    }

    @Override
    public void onClick(View view) {
        if (view == lookup){
            performUserLookup();
        }else if (view == updateUser){
            performUserUpdate();
        }
    }
}
