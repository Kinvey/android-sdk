package com.kinvey.android.cache;


import java.util.HashMap;

import io.realm.DynamicRealm;

/**
 * Created by yuliya on 10/12/17.
 */
public class TableNameManager {

    private HashMap<String, String> hashMap = new HashMap<>();
    private DynamicRealm realm;
    private String collection = "_tableManager";
    private static TableNameManager tableNameManager;

    private TableNameManager(DynamicRealm realm) {
        this.realm = realm;
        realm.where(collection).findAll();
    }

    public String createShortName(String originalName) {


        String shortName = null;
        hashMap.put(shortName, originalName);
        return shortName;
    }

    public String getOriginalName(String shortName) {
        String originalName = hashMap.get(shortName);
        return originalName;
    }

    public static TableNameManager getInstance(DynamicRealm realm) {
        if (tableNameManager == null) {
            tableNameManager = new TableNameManager(realm);
        }
        return tableNameManager;
    }

}
