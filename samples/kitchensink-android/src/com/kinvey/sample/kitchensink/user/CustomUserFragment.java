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
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author edwardf
 */
public class CustomUserFragment extends UseCaseFragment implements View.OnClickListener {

    private Button retrieve;
    private Button update;

    @Override
    public int getViewID() {
        return R.layout.feature_user_update;
    }

    @Override
    public void bindViews(View v) {
        retrieve = (Button) v.findViewById(R.id.user_retrieve);
        update = (Button) v.findViewById(R.id.user_update);

        retrieve.setOnClickListener(this);
        update.setOnClickListener(this);
    }

    @Override
    public String getTitle() {
        return "Custom User";
    }

    @Override
    public void onClick(View view) {
        if (view == retrieve){
            retrieveUser();
        }else if(view == update){
            updateUser();
        }
    }

    private void retrieveUser(){

        MyCustomUser user = getClient().user(MyCustomUser.class);
        user.retrieve(new KinveyClientCallback<MyCustomUser>(){


            @Override
            public void onSuccess(MyCustomUser result) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable error) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });



    }

    private void updateUser(){

//        MyCustomUser user = getClient().user(MyCustomUser.class);
//        user.update(new KinveyClientCallback<MyCustomUser>(){
//
//
//            @Override
//            public void onSuccess(MyCustomUser result) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public void onFailure(Throwable error) {
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });



    }
}
