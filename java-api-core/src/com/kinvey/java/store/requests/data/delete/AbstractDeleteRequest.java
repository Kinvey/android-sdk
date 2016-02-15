package com.kinvey.java.store.requests.data.delete;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.cache.ICache;
import com.kinvey.java.network.AppData;
import com.kinvey.java.store.ReadPolicy;
import com.kinvey.java.store.WritePolicy;
import com.kinvey.java.store.requests.data.AbstractKinveyDataListRequest;
import com.kinvey.java.store.requests.data.AbstractKinveyExecuteRequest;

import java.io.IOException;
import java.util.List;

/**
 * Created by Prots on 2/8/16.
 */
public abstract class AbstractDeleteRequest<T extends GenericJson> extends AbstractKinveyExecuteRequest<T> {
    private AbstractClient client;
    private final String collectionName;
    private final Class<T> clazz;
    protected final ICache<T> cache;
    private final WritePolicy writePolicy;

    public AbstractDeleteRequest(AbstractClient client, String collectionName, Class<T> clazz,
                                 ICache<T> cache, WritePolicy writePolicy) {
        this.client = client;
        this.collectionName = collectionName;
        this.clazz = clazz;

        this.cache = cache;
        this.writePolicy = writePolicy;
    }

    @Override
    public Void execute() throws IOException {
        AppData<T> appData = client.appData(collectionName, clazz);
        switch (writePolicy){
            case FORCE_LOCAL:
                deleteCached();
                //TODO: write to sync
                break;
            case FORCE_NETWORK:

                try {
                    deleteNetwork();
                } catch (IOException e) {
                    //TODO: add to sync
                    e.printStackTrace();
                }

                //write to network, fallback to sync
                break;
            case LOCAL_THEN_NETWORK:
                //write to local and network, push to sync if network fails
                deleteCached();
                try {
                    deleteNetwork();
                } catch (IOException e) {
                    //TODO: add to sync
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    protected AppData<T> getNetworkData(){
        return client.appData(collectionName, clazz);
    }

    @Override
    public void cancel() {

    }

    abstract protected List<T> deleteCached();
    abstract protected List<T> deleteNetwork() throws IOException;

}
