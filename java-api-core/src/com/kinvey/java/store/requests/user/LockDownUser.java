package com.kinvey.java.store.requests.user;

import com.google.api.client.json.GenericJson;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.store.UserStore;

/**
 * Created by Prots on 2/12/16.
 */
public final class LockDownUser extends AbstractKinveyJsonClientRequest<Void> {
    private static final String REST_PATH = "rpc/{appKey}/lockdown-user";

    private UserStore userStore;

    public LockDownUser(UserStore userStore, GenericJson lock) {
        super(userStore.getClient(), "POST", REST_PATH, lock, Void.class);
        this.userStore = userStore;
    }
}
