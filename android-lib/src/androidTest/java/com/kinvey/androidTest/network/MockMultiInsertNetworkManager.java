package com.kinvey.androidTest.network;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.network.NetworkManager;
import com.kinvey.java.sync.dto.SyncRequest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MockMultiInsertNetworkManager<T extends GenericJson> extends NetworkManager<T> {

    private boolean useSingleSave = false;
    private boolean useMultiInsertSave = false;
    private AtomicInteger multiPostCount = new AtomicInteger(0);

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
        Save result = super.saveBlocking(entity);
        if (SyncRequest.HttpVerb.POST.toString().equals(result.getRequestMethod().toUpperCase())) {
            useSingleSave = true;
        }
        return result;
    }

    @Override
    public SaveBatch saveBatchBlocking(List<T> list) throws IOException {
        useMultiInsertSave = true;
        multiPostCount.incrementAndGet();
        return super.saveBatchBlocking(list);
    }

    public void clear() {
        multiPostCount.set(0);
        useSingleSave = false;
        useMultiInsertSave = false;
    }

    public int getMultiPostCount() {
        return multiPostCount.get();
    }

    public boolean useMultiInsertSave() {
        return useMultiInsertSave && !useSingleSave;
    }
}
