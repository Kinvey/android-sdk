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

import android.os.Environment;
import android.widget.ListView;
import com.kinvey.sample.kitchensink.KitchenSink;
import com.kinvey.sample.kitchensink.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author edwardf
 */
@RunWith(RobolectricTestRunner.class)
public class KitchenSinkTest {


    private KitchenSink activity;
    private ListView featureList;


    @Before
    public void setUp() throws Exception {
        //config robolectric for real HTTP and persistance
        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);

        activity = Robolectric.buildActivity(KitchenSink.class).create().get();

        featureList = (ListView) activity.findViewById(R.id.ks_list);

    }


    @Test
    public void testAppName() throws Exception {
        String appName = activity.getResources().getString(R.string.app_name);
        assertThat(appName, equalTo("Kitchensink"));

    }

    @Test
    public void testListContents(){
        //yes, 5 is a magic number.  At the time of writing this test, there are 5 features--
        //so, anytime in the future, there should be at least 5
        assertTrue(featureList.getAdapter().getCount() >=5);



    }
}
