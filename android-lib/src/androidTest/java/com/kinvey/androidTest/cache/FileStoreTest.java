package com.kinvey.androidTest.cache;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.dto.User;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.store.FileStore;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.store.UserStore;

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

    private boolean failture, success;
    @Before
    public void setUp() throws IOException {
        Client.Builder builder = new Client.Builder(InstrumentationRegistry.getContext());
        client = builder.build();
        UserStore<User> user = client.userStore();
        if (!user.isUserLoggedIn()) {
            user.loginBlocking().execute();
        }
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


    @Test
    public void testUploadAndGet() throws IOException {

        for (StoreType storeType : StoreType.values()){
            FileStore fileStore = client.getFileStore();
            fileStore.setStoreType(storeType);

            File test = new File(client.getContext().getFilesDir(), "test"+storeType.toString()+".xml");
            if (!test.exists()){
                test.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(test);
            fos.write("this is a sample file to test".getBytes());

            FileMetaData metadata = fileStore.upload(test);

            File destination = new File(client.getContext().getFilesDir(), "testDownload"+storeType.toString()+".xml");
            if (!destination.exists()){
                destination.createNewFile();
            }

            failture = false;
            success = false;


            client.getFileStore().download(metadata,
                    new FileOutputStream(destination),
                    new DownloaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpDownloader downloader) throws IOException {

                        }

                        @Override
                        public void onSuccess(Void result) {
                            success = true;
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            failture = true;
                        }
                    });

            assertFalse("Should not fail", failture);
            assertTrue("Should pass", success);
        }



    }


}
