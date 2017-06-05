package com.kinvey.java.dto;

import com.google.api.client.util.Key;

/**
 * Created by yuliya on 06/01/17.
 */

public class UserNameExists {
    @Key("usernameExists")
    private boolean usernameExists;

    public boolean doesUsernameExist() {
        return usernameExists;
    }

    public void setUsernameExists(boolean usernameExists) {
        this.usernameExists = usernameExists;
    }
}
