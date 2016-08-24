package com.kinvey.java.store;


import com.kinvey.java.AbstractClient;
import com.kinvey.java.auth.KinveyAuthRequest;
import com.kinvey.java.dto.User;

import java.io.IOException;

public class UserStore<T> extends UserStoreRequestManager {

    private User user;
    private String email;

    public UserStore(AbstractClient client, Class<T> userClass, KinveyAuthRequest.Builder builder) {
        super(client, userClass, builder);
    }

    public static void signUp() {

    }

    /*Deletes a 'User' by the 'userId'*/
    public static void destroy(String userId, boolean isHard, AbstractClient client) {

    }

    /*Deletes the 'User'*/
    public static void destroy() {

    }

    public static LoginRequest login(String username, String password, AbstractClient client) throws IOException {
        return loginBlocking(username, password);
    }



}
