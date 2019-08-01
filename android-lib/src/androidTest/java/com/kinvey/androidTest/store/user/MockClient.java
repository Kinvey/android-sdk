package com.kinvey.androidTest.store.user;

import android.content.Context;

import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.kinvey.android.Client;
import com.kinvey.android.model.User;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.core.KinveyHeaders;

public class MockClient<T extends User> extends Client<T> {


    private MockClient(HttpTransport transport, HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath, JsonObjectParser objectParser, KinveyClientRequestInitializer kinveyRequestInitializer, CredentialStore store, BackOffPolicy requestPolicy, byte[] encryptionKey, Context context) {
        super(transport, httpRequestInitializer, rootUrl, servicePath, objectParser, kinveyRequestInitializer, store, requestPolicy, encryptionKey, context);
    }

    public static class Builder<T extends User> extends Client.Builder<T> {

        Context context;
        private Class userClass = null;

        public Builder(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public MockClient.Builder setUserClass(Class<T> userClass) {
            this.userClass = userClass;
            return this;
        }

        @Override
        public MockClient<T> build() {
            MockClient<T> client = new MockClient<>(new MockHttpTransport(), getHttpRequestInitializer(), getBaseUrl(), getServicePath(),
                    getObjectParser(), new MockKinveyClientRequestInitializer(), null, null, null, context);
            client.setUserClass(userClass != null ? userClass : (Class<T>) User.class);
            return client;
        }

        public MockClient<T> build(HttpTransport transport) {
            MockClient<T> client = new MockClient<>(transport, getHttpRequestInitializer(), getBaseUrl(), getServicePath(),
                    getObjectParser(), new MockKinveyClientRequestInitializer(), null, null, null, context);
            client.setUserClass(userClass != null ? userClass : (Class<T>) User.class);
            initUserFromCredentialStore(client);
            return client;
        }

    }

    static class MockKinveyClientRequestInitializer extends KinveyClientRequestInitializer {

        boolean isCalled;

        MockKinveyClientRequestInitializer() {
            super("appkey", "appsecret", new KinveyHeaders());
        }

        @Override
        public void initialize(AbstractKinveyClientRequest<?> request) {
            isCalled = true;
        }
    }

}
