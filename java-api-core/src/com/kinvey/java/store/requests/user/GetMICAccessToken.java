package com.kinvey.java.store.requests.user;

import com.google.api.client.http.HttpContent;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.store.UserStore;

/**
 * Created by Prots on 2/12/16.
 */
public final class GetMICAccessToken extends AbstractKinveyClientRequest<GenericJson> {
    private static final String REST_PATH = "oauth/token";

    private UserStore userStore;

    public GetMICAccessToken(UserStore userStore, HttpContent content) {
        super(userStore.getClient(), userStore.MICHostName, "POST", REST_PATH, content, GenericJson.class);
        this.userStore = userStore;
    }
}
