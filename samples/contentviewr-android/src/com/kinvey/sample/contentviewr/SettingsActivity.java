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

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.kinvey.sample.contentviewr.LoginFragment;
import com.kinvey.sample.contentviewr.NotificationFragment;
import com.kinvey.sample.contentviewr.core.ContentFragment;

/**
 * @author edwardf
 */
public class SettingsActivity extends SherlockFragmentActivity {

    public static final String LOGIN = "LOGIN";
    public static final String PUSH = "PUSH";

    public static final String EXTRA_TYPE = "EXTRA_TYPE";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        String type;
        if (getIntent().hasExtra(EXTRA_TYPE)) {
            type = getIntent().getExtras().getString(EXTRA_TYPE);


            if (type.equals(PUSH)){
                replaceFragment(new NotificationFragment(), false);
            } else{
                replaceFragment(new LoginFragment(), false);
            }

        }
    }

        public void replaceFragment(ContentFragment frag, boolean backstack){
            FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
            tr.replace(android.R.id.content, frag);
            tr.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

            if(backstack){
                tr.addToBackStack("back");
            }
            tr.commit();
        }






}
