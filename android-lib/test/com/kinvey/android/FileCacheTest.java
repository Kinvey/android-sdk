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
package com.kinvey.android;

import android.app.Activity;
import com.google.common.io.ByteStreams;
import com.kinvey.android.offline.FileCacheSqlHelper;
import com.kinvey.android.offline.SQLiteFileCache;
import com.kinvey.java.model.FileMetaData;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * @author edwardf
 */
@RunWith(RobolectricTestRunner.class)
public class FileCacheTest {

    Activity activity;
    Client client;

    SQLiteFileCache cache;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(Activity.class)
                .create()
                .get();
        client = new Client
                .Builder("someAppKey", "someAppSecret", activity.getApplicationContext())
                .build();
    }

    @Test
    public void testDefaultLocation(){
        SQLiteFileCache cache = new SQLiteFileCache(activity.getApplicationContext());
        Assert.assertEquals(cache.getCacheDir(), activity.getApplicationContext().getCacheDir());
    }

    @Test
    public void testCustomLocation(){
        File dir = new File("myfile");
        dir.mkdirs();
        SQLiteFileCache cache = new SQLiteFileCache(dir);
        Assert.assertEquals(cache.getCacheDir(), dir);
        dir.delete();
    }

    @Test
    public void testBadLocation(){
        File dir = new File("/myfile.txt");
        try{
            SQLiteFileCache cache = new SQLiteFileCache(dir);
            //constructor should have thrown an exception
            Assert.assertTrue(false);
        }catch(Exception e){
            Assert.assertTrue(e.getMessage().contains("File Cache needs a directory"));
        }
        dir.delete();
    }

    @Test
    public void testSaveAndLoad(){
        cache = new SQLiteFileCache(activity);
        String fileID = "123";
        FileMetaData fm = new FileMetaData(fileID);
        fm.setFileName("duck.txt");

        cache.save(activity, client, fm, new byte[5]);
        FileInputStream fis = cache.get(activity, fileID);
        Assert.assertNotNull(fis);
        byte[] res = null;
        try{
            res = ByteStreams.toByteArray(fis);
        }catch (IOException e){
            Assert.assertTrue("converting to byte array threw an exception", false);
        }
        Assert.assertNotNull(res);
        Assert.assertEquals(5, res.length);
    }







}
