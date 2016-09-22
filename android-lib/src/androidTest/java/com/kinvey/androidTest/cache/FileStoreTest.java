package com.kinvey.androidTest.cache;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.kinvey.android.Client;
import com.kinvey.android.store.AsyncFileStore;
import com.kinvey.android.store.AsyncUserStore;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.dto.User;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.store.FileStore;
import com.kinvey.java.store.StoreType;
import com.kinvey.java.store.UserStoreRequestManager;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
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
        if (!client.isUserLoggedIn()) {
            AsyncUserStore.login(client, User.class);
        }
    }
    @Test
    public void testUpload() throws IOException {

        FileStore fileStore = client.getFileStore(StoreType.CACHE);

        File test = new File(client.getContext().getFilesDir(), "test.xml");
        if (!test.exists()){
            test.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(test);
        fos.write("this is a sample file to test".getBytes());

        fileStore.upload(test, new UploaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpUploader uploader) throws IOException {

            }

            @Override
            public void onSuccess(FileMetaData result) {

            }

            @Override
            public void onFailure(Throwable error) {

            }
        });

    }


    @Test
    public void testUploadAndGet() throws IOException {

        for (StoreType storeType : StoreType.values()){
            FileStore fileStore = client.getFileStore(storeType);
            fileStore.setStoreType(storeType);

            File test = new File(client.getContext().getFilesDir(), "test"+storeType.toString()+".xml");
            if (!test.exists()){
                test.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(test);
            fos.write("this is a sample file to test".getBytes());

            FileMetaData metadata = fileStore.upload(test, new UploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {

                }

                @Override
                public void onSuccess(FileMetaData result) {

                }

                @Override
                public void onFailure(Throwable error) {

                }
            });

            File destination = new File(client.getContext().getFilesDir(), "testDownload"+storeType.toString()+".xml");
            if (!destination.exists()){
                destination.createNewFile();
            }

            failture = false;
            success = false;


            client.getFileStore(storeType).download(metadata,
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

    @Test
    public void asyncCallsShouldNotFail() throws IOException {
        AsyncFileStore fileStore = client.getFileStore(StoreType.SYNC);

        File test = new File(client.getContext().getFilesDir(), "test.xml");
        if (!test.exists()){
            test.createNewFile();
        }

        FileOutputStream fos = new FileOutputStream(test);
        fos.write("this is a sample file to test".getBytes());

        UploaderProgressListener progress = new UploaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpUploader uploader) throws IOException {

            }

            @Override
            public void onSuccess(FileMetaData result) {

            }

            @Override
            public void onFailure(Throwable error) {

            }
        };

        KinveyClientCallback<FileMetaData> metaCallback = new KinveyClientCallback<FileMetaData>() {
            @Override
            public void onSuccess(FileMetaData result) {

            }

            @Override
            public void onFailure(Throwable error) {

            }
        };

        FileMetaData meta = new FileMetaData();
        meta.setFileName("test.xml");

        fileStore.upload(test, metaCallback, progress);
        fileStore.upload(test, meta, metaCallback, progress);
        fileStore.upload(new FileInputStream(test), meta, metaCallback, progress);
        fileStore.upload("test.xml", new FileInputStream(test), metaCallback, progress);


    }


}
