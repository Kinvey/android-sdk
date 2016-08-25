package com.kinvey.java.store;


import com.kinvey.java.AbstractClient;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.dto.User;

import java.io.IOException;

public class UserStore<T> {

    private User user;
    private String email;

    public static void signUp() {

    }

    /*Deletes a 'User' by the 'userId'*/
    public static void destroy(String userId, boolean isHard, AbstractClient client) {

    }

    /*Deletes the 'User'*/
    public static void destroy() {

    }

    public static <T extends User> User login(String username, String password, AbstractClient client, Class<T> userClass) throws IOException {
        return new UserStoreRequestManager(client, userClass, createBuilder(client)).loginBlocking(username, password).execute();
    }
//
//    public static <T extends User> User logout(String username, String password, AbstractClient client, Class<T> userClass) throws IOException {
//        return new UserStoreRequestManager(client, userClass, createBuilder(client)).loginBlocking(username, password).execute();
//    }

    private static KinveyAuthRequest.Builder createBuilder(AbstractClient client) {
        String appKey = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppKey();
        String appSecret = ((KinveyClientRequestInitializer) client.getKinveyRequestInitializer()).getAppSecret();

        return new KinveyAuthRequest.Builder(client.getRequestFactory().getTransport(),
                client.getJsonFactory(), client.getBaseUrl(), appKey, appSecret, null);
    }

}
