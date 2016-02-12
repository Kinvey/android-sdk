package com.kinvey.java.store.requests.user;

import com.google.api.client.util.Key;
import com.google.gson.Gson;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.store.UserStore;

import java.io.IOException;

/**
 * Delete Request Class, extends AbstractKinveyJsonClientRequest<Void>.  Constructs the HTTP request object for
 * Delete User requests.
 */
public final class Delete extends AbstractKinveyJsonClientRequest<Void> {
    private static final String REST_PATH = "user/{appKey}/{userID}?hard={hard}";

    private UserStore userStore;
    @Key
    private boolean hard = false;

    @Key
    private String userID;

    public Delete(UserStore userStore, String userID, boolean hard) {
        super(userStore.getClient(), "DELETE", REST_PATH, null, Void.class);
        this.userStore = userStore;
        this.userID = userID;
        this.hard = hard;
        this.getRequestHeaders().put("X-Kinvey-Client-App-Version", userStore.getClientAppVersion());
        if (userStore.getCustomRequestProperties() != null && !userStore.getCustomRequestProperties().isEmpty()){
            this.getRequestHeaders().put("X-Kinvey-Custom-Request-Properties", new Gson().toJson(userStore.getCustomRequestProperties()) );
        }
    }

    @Override
    public Void execute() throws IOException {
        super.execute();
        userStore.removeFromStore(userID);
        userStore.logout();

        return null;
    }
}
