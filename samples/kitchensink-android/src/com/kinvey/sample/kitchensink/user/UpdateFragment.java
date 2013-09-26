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

import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.kinvey.android.AsyncUser;
import com.kinvey.android.AsyncUserDiscovery;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserListCallback;
import com.kinvey.java.User;
import com.kinvey.java.model.UserLookup;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author edwardf
 */
public class UpdateFragment extends UseCaseFragment implements View.OnClickListener{

    private Button updateCurrent;
    private Button updateOther;

    @Override
    public int getViewID() {
        return R.layout.feature_user_update;
    }

    @Override
    public void bindViews(View v) {
        updateCurrent = (Button) v.findViewById(R.id.user_update_current);
        updateOther = (Button) v.findViewById(R.id.user_update_other);

        updateCurrent.setOnClickListener(this);
        updateOther.setOnClickListener(this);
    }

    @Override
    public String getTitle() {
        return "Update";
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


    private void lookupAndUpdate(){

        AsyncUserDiscovery users = getApplicationContext().getClient().userDiscovery();
        UserLookup criteria = users.userLookup();
        criteria.setLastName("Smith");
        users.lookup(criteria, new KinveyUserListCallback() {
            @Override
            public void onSuccess(User[] result) {
                Toast.makeText(getSherlockActivity(), "lookup returned: " + result.length + " user(s)!", Toast.LENGTH_SHORT).show();
                if (result != null && result.length > 0){
                    performOtherUserUpdate(result[0]);
                }
            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(getSherlockActivity(), "lookup failed -> " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performOtherUserUpdate(User u){

        AsyncUser user = getApplicationContext().getClient().user();
        u.put("last_name", "UpdatedName");
        user.update(u, new KinveyUserCallback() {
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

    @Override
    public void onClick(View view) {
        if (view == updateOther){
            performUserUpdate();
        }else if (view == updateCurrent){
            lookupAndUpdate();
        }
    }
}
