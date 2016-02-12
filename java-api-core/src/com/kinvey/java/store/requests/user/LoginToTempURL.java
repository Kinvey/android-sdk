package com.kinvey.java.store.requests.user;

import com.google.api.client.http.HttpContent;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.KinveyException;
import com.kinvey.java.core.AbstractKinveyClientRequest;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.UserStore;

import java.io.IOException;

/**
 * Created by Prots on 2/12/16.
 */
public final class LoginToTempURL<T extends User> extends AbstractKinveyClientRequest<GenericJson> {

    private UserStore<T> userStore;

    public LoginToTempURL(UserStore<T> userStore, String tempURL, HttpContent content) {
        super(userStore.getClient(), tempURL, "POST", "", content, GenericJson.class);
        this.userStore = userStore;
        this.setOverrideRedirect(true);
    }

    @Override
    public GenericJson onRedirect(String newLocation) throws IOException {

        int codeIndex = newLocation.indexOf("code=");
        if (codeIndex == -1) {
            throw new KinveyException("Redirect does not contain `code=`, was: " + newLocation);
        }

        String accesstoken = newLocation.substring(codeIndex + 5, newLocation.length());

        return userStore.getMICToken(accesstoken).execute();


    }
}
