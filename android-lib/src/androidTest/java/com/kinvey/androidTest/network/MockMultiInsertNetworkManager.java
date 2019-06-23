package com.kinvey.androidTest.network;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.network.NetworkManager;

import java.io.IOException;
import java.util.List;

public class MockMultiInsertNetworkManager<T extends GenericJson> extends NetworkManager<T> {

    private boolean useSingleSave = false;
    private boolean useMultiInsertSave = false;

    /**
     * Constructor to instantiate the NetworkManager class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     * @param client
     */
    public MockMultiInsertNetworkManager(String collectionName, Class<T> myClass, AbstractClient client) {
        super(collectionName, myClass, client);
    }

    @Override
    public Save saveBlocking(T entity) throws IOException {
        useSingleSave = true;
        return super.saveBlocking(entity);
    }

    @Override
    public SaveBatch saveBatchBlocking(List<T> list) throws IOException {
        useMultiInsertSave = true;
        return super.saveBatchBlocking(list);
    }

    public void clear() {
        useSingleSave = false;
        useMultiInsertSave = false;
    }

    public boolean useMultiInsertSave() {
        return useMultiInsertSave && !useSingleSave;
    }
}
