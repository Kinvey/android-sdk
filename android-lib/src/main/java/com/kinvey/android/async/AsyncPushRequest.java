package com.kinvey.android.async;

import com.kinvey.android.AsyncClientRequest;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Class represents internal implementation of Async push request that is used to create push
 */
public class AsyncPushRequest extends AsyncClientRequest<Long> {
    private final String collection;
    private final SyncManager manager;
    private final AbstractClient client;
    private KinveyPushCallback callback;

    /**
     * Async push request constructor
     * @param collection Collection name that we want to push
     * @param manager sync manager that is used
     * @param client Kinvey client instance to be used to execute network requests
     * @param callback async callbacks to be invoked when job is done
     */
    public AsyncPushRequest(String collection,
                            SyncManager manager,
                            AbstractClient client,
                            KinveyPushCallback callback){
        super(callback);
        this.collection = collection;
        this.manager = manager;
        this.client = client;
        this.callback = callback;
    }


    @Override
    protected Long executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
        SyncRequest syncRequest = null;
        long progress = 0;
        while ((syncRequest = manager.popSingleQueue(collection)) != null){
            manager.executeRequest(client, syncRequest);
            publishProgress(++progress);
        }
        return progress;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
        callback.onProgress(values[0], manager.getCount(this.collection));
    }
}
