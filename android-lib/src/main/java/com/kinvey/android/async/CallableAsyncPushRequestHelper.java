package com.kinvey.android.async;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncItem;
import com.kinvey.java.sync.dto.SyncRequest;

import java.util.concurrent.Callable;

public class CallableAsyncPushRequestHelper implements Callable {

    private final AbstractClient client;
    private final SyncManager manager;
    private final SyncRequest syncRequest;
    private final SyncItem syncItem;

    CallableAsyncPushRequestHelper(AbstractClient client,
                           SyncManager manager,
                           SyncRequest syncRequest,
                           SyncItem syncItem) {
        this.client = client;
        this.manager = manager;
        this.syncRequest = syncRequest;
        this.syncItem = syncItem;
    }

    @Override
    public Long call() throws Exception {
        manager.executeRequest(client, syncRequest);
        manager.deleteCachedItem((String) syncItem.get("_id"));
        return 1L;
    }
}
