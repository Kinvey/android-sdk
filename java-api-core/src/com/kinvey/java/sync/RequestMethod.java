package com.kinvey.java.sync;

/**
 * Created by yuliya on 09/28/17.
 */

public enum RequestMethod {
    SAVE("SAVE"),
    DELETE("DELETE");

    private String query;

    RequestMethod(String query) {
        this.query = query;
    }

    public static RequestMethod fromString(String verb){
        for (RequestMethod v : RequestMethod.values()){
            if (v.query.equals(verb)){
                return v;
            }
        }
        return null;
    }
}
