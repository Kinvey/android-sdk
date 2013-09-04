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
package com.example.testdrive.android.test;

import android.widget.Button;
import android.widget.ProgressBar;
import com.example.testdrive.android.R;
import com.example.testdrive.android.TestDrive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


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
//        activity = Robolectric.buildActivity(TestDrive.class).create().get();
//        load = (Button) activity.findViewById(R.id.load);
//        loadAll = (Button) activity.findViewById(R.id.loadAll);
//        query = (Button) activity.findViewById(R.id.query);
//        save = (Button) activity.findViewById(R.id.save);
//        delete = (Button) activity.findViewById(R.id.delete);
//        progress = (ProgressBar) activity.findViewById(R.id.refresh_progress);

    }


    @Test
    public void testAppName() throws Exception {
        String appName = new TestDrive().getResources().getString(R.string.app_name);
        assertThat(appName, equalTo("TestDrive"));
    }


}