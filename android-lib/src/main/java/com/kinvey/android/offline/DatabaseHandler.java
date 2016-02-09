/*
 * Copyright (c) 2014, Kinvey, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.android.offline;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.offline.OfflineRequestInfo.OfflineMetaData;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.network.AppData;

/**
 * This class provides declarations for "methods you would perform on a database".  This abstraction allows for various implementations of the actual database itself.
 * <p/> note the method parameters match Android's native SQLite3 implementation.
 *
 *
 * @author edwardf
 */
public interface DatabaseHandler {

    static final int DATABASE_VERSION = 1;
    static final String DB_NAME = "kinveyOffline.db";

    static final String COLLECTION_TABLE = "collections";
    static final String COLUMN_NAME = "name";

    public Cursor query (String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit);

    public void runCommand(String command);

    public int updateWithOnConflict (String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm);

    public long insert (String table, String nullColumnHack, ContentValues values);

    public int delete (String table, String whereClause, String[] whereArgs);

    public OfflineTable getTable(String collectionName);

    public List<String> getCollectionTables();

    public GenericJson getEntity(AbstractClient client, AppData appData, OfflineMetaData id);

    public int getDBSchemaVersion ();

    public void updateDBSchemaVersion (int newVersion);


}
