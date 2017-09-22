package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by yuliya on 09/21/17.
 */

public class StringGenericJson extends GenericJson {

    @Key("string")
    private String string;

    public StringGenericJson() {
    }

    public StringGenericJson(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
