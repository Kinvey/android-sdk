package com.kinvey.android;

import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Query;
import com.kinvey.java.store.DataStore;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Class represents internal implementation of Async pull request that is used to create pull
 */
public class AsyncPullRequest extends AsyncClientRequest<Void> {
    private final DataStore store;
    private Query query;

    /**
     * Async pull request constructor
     * @param query Query that is used to fetch data from network
     * @param store Kinvey data store instance to be used to execute network requests
     * @param callback async callbacks to be invoked when job is done
     */
    public AsyncPullRequest(DataStore store,
                            Query query,
                            KinveyPullCallback callback){
        super(callback);
        this.query = query;
        this.store = store;
    }


    @Override
    protected Void executeAsync() throws IOException, InvocationTargetException, IllegalAccessException {
        store.pullBlocking(query);
        return null;
    }
}
