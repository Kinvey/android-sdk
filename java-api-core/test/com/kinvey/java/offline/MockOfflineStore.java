package com.kinvey.java.offline;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.network.NetworkStore;
import com.kinvey.java.model.KinveyDeleteResponse;

/**
 * Created by edward on 8/3/15.
 */
public class MockOfflineStore implements OfflineStore<GenericJson> {

    @Override
    public GenericJson executeGet(AbstractClient client, NetworkStore appData, AbstractKinveyOfflineClientRequest request) {
        GenericJson ret = new GenericJson();
        ret.put("hello", "get");
        return ret;
    }

    @Override
    public KinveyDeleteResponse executeDelete(AbstractClient client, NetworkStore appData, AbstractKinveyOfflineClientRequest request) {
        KinveyDeleteResponse ret = new KinveyDeleteResponse();
        ret.setCount(1);
        return ret;
    }

    @Override
    public GenericJson executeSave(AbstractClient client, NetworkStore appData, AbstractKinveyOfflineClientRequest request) {
        GenericJson ret = new GenericJson();
        ret.put("hello", "save");
        return ret;
    }

    @Override
    public void insertEntity(AbstractClient client, NetworkStore appData, GenericJson entity, AbstractKinveyOfflineClientRequest request) {

    }

    @Override
    public void clearStorage(String userid) {

    }

    @Override
    public void kickOffSync() {

    }

    public static class NullStore implements OfflineStore<GenericJson> {

        @Override
        public GenericJson executeGet(AbstractClient client, NetworkStore<GenericJson> appData, AbstractKinveyOfflineClientRequest<GenericJson> request) {
            return null;
        }

        @Override
        public KinveyDeleteResponse executeDelete(AbstractClient client, NetworkStore<GenericJson> appData, AbstractKinveyOfflineClientRequest<GenericJson> request) {
            return null;
        }

        @Override
        public GenericJson executeSave(AbstractClient client, NetworkStore<GenericJson> appData, AbstractKinveyOfflineClientRequest<GenericJson> request) {
            return null;
        }

        @Override
        public void insertEntity(AbstractClient client, NetworkStore<GenericJson> appData, GenericJson entity, AbstractKinveyOfflineClientRequest<GenericJson> request) {

        }

        @Override
        public void clearStorage(String userid) {

        }

        @Override
        public void kickOffSync() {

        }
    }
}
