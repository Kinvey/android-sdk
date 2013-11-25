/*
 * Copyright (c) 2013 Kinvey Inc.
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
package com.kinvey.android.secure;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import android.util.Log;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.AppData;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import com.kinvey.android.offline.DatabaseHandler;
import com.kinvey.android.offline.OfflineTable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edwardf
 */
public class SecureHelper extends SQLiteOpenHelper implements DatabaseHandler {


    private static SecureHelper _instance;

    private Context context;
    private String userId;

    /**
     * This class is a synchronized Singleton, and this is how you get an instance of it.
     *
     * @param context the current active application context
     * @return an instance of the OfflineHelper class
     */
    public static synchronized SecureHelper getInstance(Context context, String userid){
        if (_instance == null){
            _instance = new SecureHelper(context);
            _instance.userId = userid;
        }
        return _instance;
    }

    private SecureHelper(Context context){
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    /**
     * Called by operating system when a new database is created.
     *
     * Don't actually do anything here.
     *
     * @param database the newly created database
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.v(Client.TAG, "offline helper onCreate");
    }

    /**
     * Called by operating system when a database needs to be upgraded
     *
     * @param database
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        //haven't had to change database yet.
    }


    /**
     * Creates a new collection table, adds it to the metadata table, and returns it
     *
     * @param collectionName - the collection to create a new table for
     * @return
     */
    @Override
    public OfflineTable getTable(String collectionName){
        OfflineTable table = new OfflineTable(collectionName);

        table.onCreate(this);

        createCollectionTable();
        addCollection(collectionName);
        return new OfflineTable(collectionName);
    }

    /**
     * Add a collection to the metadata table if it doesn't already exist
     *
     * @param collectionName - the name of the AppData collection
     */
    private void addCollection(String collectionName){

        SQLiteDatabase db = getWritableDatabase(getPassword());
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, collectionName);

        int change = db.updateWithOnConflict(COLLECTION_TABLE, values, null, null, db.CONFLICT_REPLACE);
        if (change == 0){
            db.insert(COLLECTION_TABLE, null, values);
        }
    }

    /**
     * query the metdata table for a list of all collection tables, returning them as a list
     *
     * @return a list of collection table names
     */
    @Override
    public List<String> getCollectionTables(){
        ArrayList<String> ret = new ArrayList<String>();

        if (checkTableExists(COLLECTION_TABLE)){
            SQLiteDatabase db = getReadableDatabase(getPassword());
            Cursor c = db.query(COLLECTION_TABLE, new String[]{COLUMN_NAME}, null, null, null, null, null);
            while (c.moveToNext()){
                ret.add(c.getString(0));
            }
            c.close();
        }
        return ret;



    }


    /**
     * Get an entity directly from an offline table
     *
     * @param client - an instance of the client
     * @param appData - an instance of appdata
     * @param id - the id of the entity to return
     * @return the entity with the appdata collection and provided id
     */

    public GenericJson getEntity(AbstractClient client, AppData appData, String id) {
        GenericJson ret;

        ret = getTable(appData.getCollectionName()).getEntity(this, client, id, appData.getCurrentClass());

        return ret;
    }


    /**
     * This method creates a table for managing the names of all collections, if it doesn't already exist.
     */
    private void createCollectionTable(){
        SQLiteDatabase db = getWritableDatabase(getPassword());

        String createCommand = "CREATE TABLE IF NOT EXISTS "
                + COLLECTION_TABLE
                + "("
                + COLUMN_NAME + " TEXT not null "
                + ");";

        runCommand(createCommand);

//        db.close();

    }

    /**
     * This method checks if a table exists, by querying sqlite_master for the table name.
     *
     * @param tableName the name of the table to check if exists
     * @return true if that table exists, false if it doesn't
     */
    private boolean checkTableExists(String tableName){
        SQLiteDatabase db = getReadableDatabase(getPassword());

        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public void setContext(Context context){
        this.context = context.getApplicationContext();
    }

    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        SQLiteDatabase db = getReadableDatabase(getPassword());
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public void runCommand(String command) {
        SQLiteDatabase db = getWritableDatabase(getPassword());
        db.execSQL(command);

    }

    @Override
    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        SQLiteDatabase db = getWritableDatabase(getPassword());
        return db.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase(getPassword());
        return db.insert(table, nullColumnHack, values);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase(getPassword());
        return db.delete(table, whereClause, whereArgs);
    }



    private String getPassword(){
        try{
            return Crypto.initKeys(userId);
        }catch (Exception e){
            return "offline";
        }


    }


}
