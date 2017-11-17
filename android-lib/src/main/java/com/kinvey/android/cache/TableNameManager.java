package com.kinvey.android.cache;


import java.util.UUID;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmObjectSchema;

/**
 * Created by yuliya on 10/12/17.
 */
public class TableNameManager {

    private static final String COLLECTION_NAME = "_tableManager";
    private static final String ORIGINAL_NAME_FIELD = "originalName";
    private static final String SHORT_NAME_FIELD = "optimizedName";

    private static void initTable(DynamicRealm realm) {
        if (realm.getSchema().get(COLLECTION_NAME) == null) {
            RealmObjectSchema realmObjectSchema = realm.getSchema().create(COLLECTION_NAME);
            realmObjectSchema.addField(SHORT_NAME_FIELD, String.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED);
            realmObjectSchema.addField(ORIGINAL_NAME_FIELD, String.class, FieldAttribute.REQUIRED);
        }
    }

    static String createShortName(String originalName, DynamicRealm realm) {
        initTable(realm);
        String shortName = UUID.randomUUID().toString();
        DynamicRealmObject object = realm.createObject(COLLECTION_NAME, shortName);
        object.set(ORIGINAL_NAME_FIELD, originalName);
        return shortName;
    }

    static String getShortName(String originalName, DynamicRealm realm) {
        initTable(realm);
        DynamicRealmObject realmObject = realm.where(COLLECTION_NAME).equalTo(ORIGINAL_NAME_FIELD, originalName).findFirst();
        return realmObject != null ? realmObject.getString(SHORT_NAME_FIELD) : null;
    }

}
