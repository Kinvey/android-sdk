package com.kinvey.java.store.requests.user;

import com.google.api.client.http.HttpContent;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStore;

/**
 * Created by Prots on 2/12/16.
 */
public final class GetMICTempURL<T extends User> extends AbstractKinveyClientRequest<GenericJson> {
    private static final String REST_PATH = "oauth/auth";

    private UserStore<T> userStore;

    public GetMICTempURL(UserStore<T> userStore, HttpContent content) {
        super(userStore.getClient(), userStore.MICHostName, "POST", REST_PATH, content, GenericJson.class);
        this.userStore = userStore;
        if (userStore.MICApiVersion != null && userStore.MICApiVersion.length() > 0) {
            this.uriTemplate = userStore.MICApiVersion + "/" + this.uriTemplate;
        }
    }
}
