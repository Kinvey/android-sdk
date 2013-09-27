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
package com.kinvey.sample.kitchensink.test;

import android.app.Application;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.internal.ActionBarSherlockCompat;
import com.actionbarsherlock.internal.ActionBarSherlockNative;
import com.kinvey.sample.kitchensink.AndroidUtil;
import com.kinvey.sample.kitchensink.KitchenSink;
import com.kinvey.sample.kitchensink.KitchenSinkApplication;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.appData.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author edwardf
 */

@RunWith(RobolectricTestRunner.class)
public class AppDataTest {

    private AppDataActivity activity;

    private PutFragment putFrag;
    private GetFragment getFrag;
    private QueryFragment queryFrag;
    private AggregateFragment aggFrag;





    @Before
    public void setUp() throws Exception {
        //config robolectric for real HTTP and persistance
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        ActivityController<AppDataActivity> controller = Robolectric.buildActivity(AppDataActivity.class);
        ((KitchenSinkApplication)controller.get().getApplication()).getClient().user().loginBlocking("tester", "tester").execute();




        activity = controller.create().start().resume().get();



        putFrag = new PutFragment();

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, putFrag);
        fragmentTransaction.commit();
        activity.getSupportFragmentManager().executePendingTransactions();








    }

    @Test
    public void testPut(){
        putFrag.getView().findViewById(R.id.appdata_put_button).performClick();
        ShadowHandler.idleMainLooper();
        String s= ShadowToast.getTextOfLatestToast();
        assertTrue(s.contains("Successfully saved"));
    }






}
