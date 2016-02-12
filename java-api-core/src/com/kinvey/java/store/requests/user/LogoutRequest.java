package com.kinvey.java.store.requests.user;

import com.kinvey.java.auth.CredentialManager;
import com.kinvey.java.auth.CredentialStore;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.store.UserStore;

/**
 * Logout Request Class.  Constructs the HTTP request object for Logout requests.
 */
public final class LogoutRequest {

    private UserStore userStore;
    private CredentialStore store;

    public LogoutRequest(UserStore userStore, CredentialStore store){
        this.userStore = userStore;
        this.store = store;
    }

    public void execute() {
        CredentialManager manager = new CredentialManager(this.store);
        manager.removeCredential(userStore.getCurrentUser().getId());
        userStore.setCurrentUser(null);
        ((KinveyClientRequestInitializer) userStore.getClient().getKinveyRequestInitializer()).setCredential(null);
    }
}
