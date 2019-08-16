package com.kinvey.androidTest.store.data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import androidx.test.runner.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyDeleteCallback;
import com.kinvey.java.core.KinveyClientCallback;

import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
/**
 * Created by Prots on 8/24/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class AsyncTaskTest {

    Client client = null;
    Looper resultingLooper = null;
    Looper currentLooper = null;


    private static class AsyncTest extends AsyncClientRequest<Integer>{
        private final int result;

        public AsyncTest(int result, KinveyClientCallback<Integer> callback){
            super(callback);
            this.result = result;
        }

        @Override
        protected Integer executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
            return result;
        }
    }

    @Before
    public void setup(){
        Context mMockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        client = new Client.Builder(mMockContext).build();
    }

    @Test
    public void testRunOnLooper() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                currentLooper = Looper.myLooper();
                new AsyncTest(1, new KinveyClientCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        finish();
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        finish();
                    }

                    public void finish(){
                        resultingLooper = Looper.myLooper();
                        latch.countDown();
                    }
                }).execute();
                Looper.loop();
            }
        }).start();

        latch.await();
        assertEquals(currentLooper, resultingLooper);
    }

    @After
    public void tearDown() {
        if (client.getKinveyHandlerThread() != null) {
            try {
                client.stopKinveyHandlerThread();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

}
