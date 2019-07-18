package com.kinvey.androidTest.store.datastore;

import android.content.Context;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.kinvey.android.Client;
import com.kinvey.android.callback.AsyncDownloaderProgressListener;
import com.kinvey.android.callback.AsyncUploaderProgressListener;
import com.kinvey.android.model.User;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.FileStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.androidTest.LooperThread;
import com.kinvey.androidTest.model.Person;
import com.kinvey.java.KinveyException;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.store.StoreType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by yuliya on 08/25/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class EncryptionTest {

    private static final String COLLECTION = "PersonsNew";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final String ACCESS_ERROR = "Access Error";
    private static final String TEST_FILENAME = "test.xml";

    private Client client;
    private Context mContext;

    private static class UserKinveyClientCallback implements KinveyClientCallback<User> {

        private CountDownLatch latch;
        private User result;
        private Throwable error;

        private UserKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(User user) {
            this.result = user;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        private void finish() {
            latch.countDown();
        }
    }

    private static class PersonKinveyClientCallback implements KinveyClientCallback<Person> {

        private CountDownLatch latch;
        private Person result;
        private Throwable error;

        PersonKinveyClientCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Person result) {
            this.result = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        void finish() {
            latch.countDown();
        }
    }

    private static class DefaultUploadProgressListener implements AsyncUploaderProgressListener<FileMetaData> {
        private CountDownLatch latch;
        private FileMetaData fileMetaDataResult;
        private Throwable error;
        private boolean isCancelled = false;
        private boolean onCancelled = false;
        private int progressChangedCounter = 0;

        private DefaultUploadProgressListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void progressChanged(MediaHttpUploader uploader) throws IOException {
            progressChangedCounter++;
        }

        @Override
        public void onCancelled() {
            onCancelled = true;
            finish();
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public void onSuccess(FileMetaData result) {
            this.fileMetaDataResult = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        private void finish() {
            latch.countDown();
        }
    }

    private static class DefaultDownloadProgressListener implements AsyncDownloaderProgressListener<FileMetaData> {

        private CountDownLatch latch;
        private FileMetaData fileMetaDataResult;
        private Throwable error;
        private boolean isCancelled = false;
        private boolean onCancelled = false;
        private int progressChangedCounter = 0;

        private DefaultDownloadProgressListener(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void progressChanged(MediaHttpDownloader downloader) throws IOException {
            progressChangedCounter++;
        }

        @Override
        public void onCancelled() {
            onCancelled = true;
            finish();
        }

        @Override
        public boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public void onSuccess(FileMetaData result) {
            this.fileMetaDataResult = result;
            finish();
        }

        @Override
        public void onFailure(Throwable error) {
            this.error = error;
            finish();
        }

        private void finish() {
            latch.countDown();
        }
    }
    
    @Before
    public void setUp() throws InterruptedException, IOException {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void testSetEncryptionKey() throws IOException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        assertNotNull(client);
    }

    @Test
    public void testDataStoreEncryption() throws InterruptedException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        UserKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DataStore<Person> encryptedStore = DataStore.collection(COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = createPerson(USERNAME);
        PersonKinveyClientCallback saveCallback = save(encryptedStore, person);
        Assert.assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Client secondClient = new Client.Builder(mContext).setEncryptionKey(key).build();
        DataStore<Person> notEncryptedStore = DataStore.collection(COLLECTION, Person.class, StoreType.SYNC, secondClient);
        PersonKinveyClientCallback findCallback = find(notEncryptedStore, saveCallback.result.getId());
        Assert.assertNotNull(findCallback.result);
        assertNull(findCallback.error);
        client.performLockDown();
    }


    @Test
    public void testDataStoreEncryptionFail() throws InterruptedException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        UserKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DataStore<Person> encryptedStore = DataStore.collection(COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = createPerson(USERNAME);
        PersonKinveyClientCallback saveCallback = save(encryptedStore, person);
        Assert.assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Client clientWithoutEncryption = new Client.Builder(mContext).build();
        KinveyException fileException = null;
        try {
            DataStore.collection(COLLECTION, Person.class, StoreType.SYNC, clientWithoutEncryption);
        } catch (KinveyException exception) {
            fileException = exception;
        }
        assertNotNull(fileException);
        assertEquals(fileException.getReason(), ACCESS_ERROR);
    }

    @Test
    public void testDataStoresWithAndWithoutEncryptionInOneClient() throws InterruptedException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        UserKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DataStore<Person> encryptedStore = DataStore.collection(COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = createPerson(USERNAME);
        PersonKinveyClientCallback saveCallback = save(encryptedStore, person);
        Assert.assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Client clientWithoutEncryption = new Client.Builder(mContext).build();
        KinveyException kinveyException = null;
        DataStore<Person> otherStore = null;
        try {
            otherStore = DataStore.collection(COLLECTION + "_OTHER", Person.class, StoreType.SYNC, clientWithoutEncryption);
        } catch (KinveyException exception) {
            kinveyException = exception;
        }
        assertNull(otherStore);
        assertNotNull(kinveyException);
        assertEquals(kinveyException.getReason(), ACCESS_ERROR);
    }

    @Test
    public void testDataStoresDecryption() throws InterruptedException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        UserKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        DataStore<Person> encryptedStore = DataStore.collection(COLLECTION, Person.class, StoreType.SYNC, client);
        Person person = createPerson(USERNAME);
        PersonKinveyClientCallback saveCallback = save(encryptedStore, person);
        Assert.assertNotNull(saveCallback.result);
        assertNull(saveCallback.error);
        Client secondClient = new Client.Builder(mContext).setEncryptionKey(key).build();
        DataStore<Person> otherStore = DataStore.collection(COLLECTION + "_OTHER", Person.class, StoreType.SYNC, secondClient);
        assertNotNull(otherStore);
    }

    @Test
    public void testFileStoreCreatingWithEncryption() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        FileStore fileStore = client.getFileStore(StoreType.SYNC);
        assertNotNull(fileStore);
    }

    @Test
    public void testFileStoresWithAndWithoutEncryptionInOneClient() {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        FileStore fileStore = client.getFileStore(StoreType.SYNC);
        assertNotNull(fileStore);
        Client clientWithoutEncryption = new Client.Builder(mContext).build();
        KinveyException kinveyException = null;
        try {
            clientWithoutEncryption.getFileStore(StoreType.SYNC);
        } catch (KinveyException exception) {
            kinveyException = exception;
        }
        assertNotNull(kinveyException);
    }

    @Test
    public void testFileStoresDecryption() throws InterruptedException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        FileStore fileStore = client.getFileStore(StoreType.SYNC);
        assertNotNull(fileStore);
        Client secondClient = new Client.Builder(mContext).setEncryptionKey(key).build();
        FileStore secondFileStore = secondClient.getFileStore(StoreType.SYNC);
        assertNotNull(secondFileStore);
    }

    @Test
    public void testFileStoreEncryptionUploading() throws InterruptedException, IOException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        UserKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        FileStore fileStore = client.getFileStore(StoreType.SYNC);
        File file = createFile();
        DefaultUploadProgressListener listener = uploadFileWithMetadata(fileStore, file, testMetadata());
        assertNotNull(listener.fileMetaDataResult);
        assertNull(listener.error);
        file.delete();
    }

    @Test
    public void testFileStoreEncryptionUploadingDownloading() throws InterruptedException, IOException {
        byte[] key = new byte[64];
        new SecureRandom().nextBytes(key);
        client = new Client.Builder(mContext).setEncryptionKey(key).build();
        UserKinveyClientCallback callback = login(USERNAME, PASSWORD);
        assertNull(callback.error);
        assertNotNull(callback.result);
        FileStore fileStore = client.getFileStore(StoreType.SYNC);
        File file = createFile();
        DefaultUploadProgressListener listener = uploadFileWithMetadata(fileStore, file, testMetadata());
        assertNotNull(listener.fileMetaDataResult);
        assertNull(listener.error);
        file.delete();
        Client secondClient = new Client.Builder(mContext).setEncryptionKey(key).build();
        FileStore secondFileStore = secondClient.getFileStore(StoreType.SYNC);
        assertNotNull(secondFileStore);
        DefaultDownloadProgressListener downloadListener = downloadFile(secondFileStore, listener.fileMetaDataResult);
        assertNotNull(downloadListener.fileMetaDataResult);
        assertNull(downloadListener.error);
    }

    private File createFile() throws IOException {
        File file = new File(client.getContext().getFilesDir(), TEST_FILENAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private FileMetaData testMetadata() {
        final FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setFileName(TEST_FILENAME);
        return fileMetaData;
    }

    private DefaultDownloadProgressListener downloadFile(final FileStore fileStore, final FileMetaData metaFile) throws InterruptedException, IOException {
        final CountDownLatch downloadLatch = new CountDownLatch(1);
        final DefaultDownloadProgressListener listener = new DefaultDownloadProgressListener(downloadLatch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final FileOutputStream fos = new FileOutputStream(createFile());
                    fileStore.download(metaFile, fos, listener, null);
                } catch (IOException e) {
                    listener.onFailure(e);
                }
            }
        });
        looperThread.start();
        downloadLatch.await();
        looperThread.mHandler.sendMessage(new Message());
        return listener;
    }

    public DefaultUploadProgressListener uploadFileWithMetadata(final  FileStore fileStore, final File f, final FileMetaData metaData) throws InterruptedException, IOException {
        final CountDownLatch latch = new CountDownLatch(1);
        final DefaultUploadProgressListener listener = new DefaultUploadProgressListener(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            @Override
            public void run() {
                try {
                    fileStore.upload(f, metaData, listener);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onFailure(e);
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return listener;
    }

    private UserKinveyClientCallback login(final String userName, final String password) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final UserKinveyClientCallback callback = new UserKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            public void run() {
                if (!client.isUserLoggedIn()) {
                    try {
                        UserStore.login(userName, password, client, callback);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    callback.onSuccess(client.getActiveUser());
                }
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private PersonKinveyClientCallback save(final DataStore<Person> store, final Person person) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final PersonKinveyClientCallback callback = new PersonKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            public void run() {
                store.save(person, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private PersonKinveyClientCallback find(final DataStore<Person> store, final String id) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final PersonKinveyClientCallback callback = new PersonKinveyClientCallback(latch);
        LooperThread looperThread = new LooperThread(new Runnable() {
            public void run() {
                store.find(id, callback);
            }
        });
        looperThread.start();
        latch.await();
        looperThread.mHandler.sendMessage(new Message());
        return callback;
    }

    private Person createPerson(String name) {
        Person person = new Person();
        person.setUsername(name);
        return person;
    }

    @After
    public void tearDown() {
        if (client != null && client.getKinveyHandlerThread() != null) {
            client.performLockDown();
            try {
                client.stopKinveyHandlerThread();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }


}
