package com.kinvey.androidTest.cache;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.cache.RealmCacheManager;
import com.kinvey.java.store.FileStore;
import com.kinvey.java.store.StoreType;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Prots on 1/27/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class FileStoreTest {
    Client client;
    @Before
    public void setUp() throws IOException {
        Client.Builder builder = new Client.Builder(InstrumentationRegistry.getContext());
        client = builder.build();
        client.userStore().loginBlocking();
    }
    @Test
    public void testUpload() throws IOException {

        FileStore fileStore = client.getFileStore();
        fileStore.setStoreType(StoreType.CACHE);

        File test = new File(client.getContext().getFilesDir(), "test.xml");
        if (!test.exists()){
            test.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(test);
        fos.write("this is a sample file to test".getBytes());

        fileStore.upload(test);

    }


}
