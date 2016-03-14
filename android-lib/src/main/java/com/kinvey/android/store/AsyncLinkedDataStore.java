package com.kinvey.android.store;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.LinkedResources.LinkedGenericJson;
import com.kinvey.java.network.LinkedNetworkManager;
import com.kinvey.java.store.LinkedDataStore;
import com.kinvey.java.store.StoreType;

/**
 * Created by Prots on 3/10/16.
 */
public class AsyncLinkedDataStore<T extends LinkedGenericJson> extends AsyncDataStore<T> {
    /**
     * Constructor for creating LinkedDataStore for given collection that will be mapped to itemType class
     *
     * @param client     Kinvey client instance to work with
     * @param collection collection name
     * @param itemType   class that data should be mapped to
     * @param storeType  type of storage that client want to use
     */
    public AsyncLinkedDataStore(AbstractClient client, String collection, Class<T> itemType, StoreType storeType) {
        super(collection, itemType, client, storeType,
                new LinkedNetworkManager<T>(collection, itemType, client));
    }
}
