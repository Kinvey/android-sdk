package com.kinvey.java.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by Prots on 2/12/16.
 */
public class User extends GenericJson {
    @Key("_id")
    private String id;

    @Key("username")
    private String username;
    private String authToken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }
}
