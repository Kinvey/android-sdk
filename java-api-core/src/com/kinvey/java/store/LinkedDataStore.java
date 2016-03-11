package com.kinvey.java.store;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.LinkedResources.LinkedGenericJson;
import com.kinvey.java.network.LinkedNetworkManager;

public class LinkedDataStore<T extends LinkedGenericJson> extends DataStore<T> {
    /**
     * Constructor for creating LinkedDataStore for given collection that will be mapped to itemType class
     *
     * @param client     Kinvey client instance to work with
     * @param collection collection name
     * @param itemType   class that data should be mapped to
     * @param storeType  type of storage that client want to use
     */
    public LinkedDataStore(AbstractClient client, String collection, Class<T> itemType, StoreType storeType) {
        super(client, collection, itemType, storeType,
                new LinkedNetworkManager<T>(collection, itemType, client));
    }
}
