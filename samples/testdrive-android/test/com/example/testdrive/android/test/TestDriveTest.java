/*
 * Copyright (c) 2014, Kinvey, Inc.
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
package com.example.testdrive.android.test;

import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import com.example.testdrive.android.R;
import com.example.testdrive.android.TestDrive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * @author edwardf
 */
@RunWith(RobolectricTestRunner.class)
public class TestDriveTest {

    private TestDrive activity;

    private Button load;
    private Button loadAll;
    private Button query;
    private Button save;
    private Button delete;

    private ProgressBar progress;


    @Before
    public void setUp() throws Exception {
        //config robolectric for real HTTP and persistance
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        activity = Robolectric.buildActivity(TestDrive.class).create().get();
        ShadowHandler.idleMainLooper();
        //both creating a new user and logging in an existing user show a toast containing the string `implicit`
        String s= ShadowToast.getTextOfLatestToast();
        assertTrue(s.contains("implicit"));

        load = (Button) activity.findViewById(R.id.load);
        loadAll = (Button) activity.findViewById(R.id.loadAll);
        query = (Button) activity.findViewById(R.id.query);
        save = (Button) activity.findViewById(R.id.save);
        delete = (Button) activity.findViewById(R.id.delete);
        progress = (ProgressBar) activity.findViewById(R.id.refresh_progress);


    }


    @Test
    public void testAppName() throws Exception {
        String appName = activity.getResources().getString(R.string.app_name);
        assertThat(appName, equalTo("TestDrive"));

    }

    @Test
    public void testSave() throws Exception{
        save.performClick();
        ShadowHandler.idleMainLooper();
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Entity Saved") );
    }

    @Test
    public void testLoad() throws Exception{
        testSave();
        load.performClick();
        ShadowHandler.idleMainLooper();
        assertTrue(ShadowToast.getTextOfLatestToast().contains("Entity Retrieved"));

    }

    @Test
    public void testQuery() throws Exception{
        testSave();
        query.performClick();
        ShadowHandler.idleMainLooper();
        assertTrue(ShadowToast.getTextOfLatestToast().contains("Retrieved"));

    }

    @Test
    public void testLoadAll() throws Exception{
        testSave();
        loadAll.performClick();
        ShadowHandler.idleMainLooper();
        assertTrue(ShadowToast.getTextOfLatestToast().contains("Retrieved"));


    }

    @Test
    public void testDelete() throws Exception{

        testSave();

        delete.performClick();
        ShadowHandler.idleMainLooper();
        assertTrue(ShadowToast.getTextOfLatestToast().contains("Number of Entities Deleted"));

    }


}