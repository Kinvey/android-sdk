package com.kinvey.android.model;

import com.kinvey.android.AsyncClientRequest;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.BaseUser;
import com.kinvey.java.store.BaseUserStore;

import java.io.IOException;

/**
 * Created by yuliya on 06/07/17.
 */

public class User extends BaseUser {

    public void update(KinveyClientCallback callback) {
        new Update(callback).execute();
    }

    public void registerLiveService(KinveyClientCallback<Void> callback) {
        new RegisterLiveService(callback).execute();
    }

    public void unregisterLiveService(KinveyClientCallback<Void> callback) {
        new UnregisterLiveService(callback).execute();
    }

    private static class Update<T extends User> extends AsyncClientRequest<T> {

        private Update(KinveyClientCallback<T> callback) {
            super(callback);
        }

        @Override
        protected T executeAsync() throws IOException {
            return BaseUserStore.update();
        }
    }

    private static class RegisterLiveService extends AsyncClientRequest<Void> {

        private RegisterLiveService(KinveyClientCallback<Void> callback) {
            super(callback);
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.registerLiveService();
            return null;
        }
    }

    private static class UnregisterLiveService extends AsyncClientRequest<Void> {

        private UnregisterLiveService(KinveyClientCallback<Void> callback) {
            super(callback);
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.unRegisterLiveService();
            return null;
        }
    }
}
