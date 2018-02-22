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

    public void registerRealtime(KinveyClientCallback<Void> callback) {
        new RegisterRealtime(callback).execute();
    }

    public void unregisterRealtime(KinveyClientCallback<Void> callback) {
        new UnregisterRealtime(callback).execute();
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

    private static class RegisterRealtime extends AsyncClientRequest<Void> {

        private RegisterRealtime(KinveyClientCallback<Void> callback) {
            super(callback);
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.registerRealtime();
            return null;
        }
    }

    private static class UnregisterRealtime extends AsyncClientRequest<Void> {

        private UnregisterRealtime(KinveyClientCallback<Void> callback) {
            super(callback);
        }

        @Override
        protected Void executeAsync() throws IOException {
            BaseUserStore.unRegisterRealtime();
            return null;
        }
    }
}
