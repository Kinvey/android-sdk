package com.kinvey.java.store.requests.user;

import com.google.api.client.util.Key;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.store.UserStore;

/**
 * ResetPassword Request Class, extends AbstractKinveyJsonClientRequest<User>.  Constructs the HTTP request object
 * for ResetPassword User requests.
 */
public final class ResetPassword extends AbstractKinveyJsonClientRequest<Void> {
    private static final String REST_PATH = "/rpc/{appKey}/{userID}/user-password-reset-initiate";

    private UserStore userStore;
    @Key
    private String userID;

    public ResetPassword(UserStore userStore, String username) {
        super(userStore.getClient(), "POST", REST_PATH, null,  Void.class);
        this.userStore = userStore;
        this.userID = username;
        this.setRequireAppCredentials(true);

    }
}
