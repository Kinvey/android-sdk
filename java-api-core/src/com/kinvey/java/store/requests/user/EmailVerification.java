package com.kinvey.java.store.requests.user;

import com.google.api.client.util.Key;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.store.UserStore;

/**
 * EmailVerification Request Class, extends AbstractKinveyJsonClientRequest<User>.  Constructs the HTTP request
 * object for EmailVerification requests.
 */
public final class EmailVerification extends AbstractKinveyJsonClientRequest<Void> {
    private static final String REST_PATH = "rpc/{appKey}/{userID}/user-email-verification-initiate";

    private UserStore userStore;
    @Key
    private String userID;

    public EmailVerification(UserStore userStore, String userID) {
        super(userStore.getClient(), "POST", REST_PATH, null, Void.class);
        this.userStore = userStore;
        this.userID = userID;
        this.setRequireAppCredentials(true);
    }
}
