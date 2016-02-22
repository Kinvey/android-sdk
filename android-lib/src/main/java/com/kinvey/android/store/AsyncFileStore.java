package com.kinvey.android.store;

import com.kinvey.java.cache.ICacheManager;
import com.kinvey.java.network.NetworkFileManager;
import com.kinvey.java.store.FileStore;
import com.kinvey.java.store.StoreType;

/**
 * Created by Prots on 2/22/16.
 */
public class AsyncFileStore extends FileStore {


    public AsyncFileStore(NetworkFileManager networkFileManager,
                          ICacheManager cacheManager, Long ttl, StoreType storeType, String cacheFolder) {
        super(networkFileManager, cacheManager, ttl, storeType, cacheFolder);
    }
}
