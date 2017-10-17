package com.kinvey.android.cache;


import com.kinvey.java.KinveyException;

import java.util.HashMap;
import java.util.UUID;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import io.realm.internal.Table;

/**
 * Created by yuliya on 10/12/17.
 */
public class TableNameManager {

    private static final String COLLECTION_NAME = "_tableManager";
    private static final String ORIGINAL_NAME_FIELD = "original";
    private static final String SHORT_NAME_FIELD = "short";
    private static final String ID_FIELD = "_id";

    /**
     * realm contains fields 'originalName' and 'shortName'
     */
    private DynamicRealm realm;

    private static TableNameManager tableNameManager;

    private TableNameManager(DynamicRealm realm) {
        initTable(realm);
    }

    private void initTable(DynamicRealm realm) {
        RealmSchema schema = realm.getSchema();
        System.out.println("TEST_TEST: initTable");
        if (schema.get(COLLECTION_NAME) == null) {
            System.out.println("TEST_TEST: scheme created: " + COLLECTION_NAME);
            RealmObjectSchema realmObjectSchema = schema.create(COLLECTION_NAME);
            realmObjectSchema.addField(ID_FIELD, String.class, FieldAttribute.PRIMARY_KEY, FieldAttribute.REQUIRED);
            realmObjectSchema.addField(ORIGINAL_NAME_FIELD, String.class, FieldAttribute.REQUIRED);
            realmObjectSchema.addField(SHORT_NAME_FIELD, String.class, FieldAttribute.REQUIRED);
        }
    }

    public String createShortName(String originalName, DynamicRealm realm) {

        DynamicRealmObject object = realm.where(COLLECTION_NAME).equalTo(ORIGINAL_NAME_FIELD, originalName).findFirst();
        String shortName = null;
        if (object == null) {
            shortName = generateShortName();
            object = realm.createObject(COLLECTION_NAME, UUID.randomUUID().toString());
            object.set(ORIGINAL_NAME_FIELD, originalName);
            object.set(SHORT_NAME_FIELD, shortName);

        } else {
            //// TODO: 13.10.2017
            throw new KinveyException("This name " + originalName + "already exists");
        }

        return shortName;
    }

    public String getShortName(String originalName, DynamicRealm realm) {
        DynamicRealmObject realmObject = realm.where(COLLECTION_NAME).equalTo(ORIGINAL_NAME_FIELD, originalName).findFirst();
        return realmObject.getString(SHORT_NAME_FIELD);
    }

    public String getOriginalName(String shortName, DynamicRealm realm) {
        DynamicRealmObject realmObject = realm.where(COLLECTION_NAME).equalTo(SHORT_NAME_FIELD, shortName).findFirst();
        return realmObject.getString(ORIGINAL_NAME_FIELD);
    }

    public static TableNameManager getInstance(DynamicRealm realm) {
        if (tableNameManager == null) {
            tableNameManager = new TableNameManager(realm);
        }
        return tableNameManager;
    }

    private String generateShortName() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 13);
        return "uuid = " + uuid;
    }

}
