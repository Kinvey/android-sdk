package com.kinvey.android.async;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Constants;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.core.KinveyJsonResponseException;
import com.kinvey.java.sync.SyncManager;
import com.kinvey.java.sync.dto.SyncItem;
import com.kinvey.java.sync.dto.SyncRequest;

import java.util.concurrent.Callable;

public class CallableAsyncPushRequestHelper<T extends GenericJson> implements Callable {

    private static final String IGNORED_EXCEPTION_MESSAGE = "EntityNotFound";
    private static final int IGNORED_EXCEPTION_CODE = 404;

    private final AbstractClient client;
    private final SyncManager manager;
    private final SyncRequest syncRequest;
    private final SyncItem syncItem;
    private final Class<T> storeItemType;

    CallableAsyncPushRequestHelper(AbstractClient client,
                           SyncManager manager,
                           SyncRequest syncRequest,
                           SyncItem syncItem,
                           Class<T> storeItemType) {
        this.client = client;
        this.manager = manager;
        this.syncRequest = syncRequest;
        this.syncItem = syncItem;
        this.storeItemType = storeItemType;
    }

    @Override
    public Long call() throws Exception {
        try {
            if (syncRequest.getHttpVerb() == SyncRequest.HttpVerb.POST) {
                String tempID = syncRequest.getEntityID().getId();
                GenericJson result = manager.executeRequest(client, syncRequest);
                ICache<T> cache = client.getCacheManager().getCache(syncRequest.getCollectionName(), this.storeItemType, Long.MAX_VALUE);
                T temp = cache.get(tempID);
                temp.set("_id", result.get("_id"));
                cache.delete(tempID);
                cache.save(temp);
            } else {
                manager.executeRequest(client, syncRequest);
            }
        } catch (KinveyJsonResponseException e) {
            if (e.getStatusCode() != IGNORED_EXCEPTION_CODE && !e.getMessage().contains(IGNORED_EXCEPTION_MESSAGE)) {
                throw e;
            }
        }
        manager.deleteCachedItem((String) syncItem.get(Constants._ID));
        return 1L;
    }
}
