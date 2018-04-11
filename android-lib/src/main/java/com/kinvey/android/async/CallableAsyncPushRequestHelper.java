package com.kinvey.android.async;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.Constants;
import com.kinvey.java.core.KinveyJsonResponseException;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncItem;
import com.kinvey.java.sync.dto.SyncRequest;

import java.util.concurrent.Callable;

public class CallableAsyncPushRequestHelper implements Callable {

    private static final String IGNORED_EXCEPTION_MESSAGE = "EntityNotFound";
    private static final int IGNORED_EXCEPTION_CODE = 404;

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
        try {
            manager.executeRequest(client, syncRequest);
        } catch (KinveyJsonResponseException e) {
            if (e.getStatusCode() != IGNORED_EXCEPTION_CODE && !e.getMessage().contains(IGNORED_EXCEPTION_MESSAGE)) {
                throw e;
            }
        }
        manager.deleteCachedItem((String) syncItem.get(Constants._ID));
        return 1L;
    }
}
