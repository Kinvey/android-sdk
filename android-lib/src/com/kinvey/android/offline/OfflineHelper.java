/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android.offline;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.offline.OfflineRequestInfo.OfflineMetaData;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.AppData;
import com.kinvey.java.Logger;

/**
 * This class manages a set of {@link OfflineTable}s.  Tables are not maintained in memory, and are created on demand.
 *
 * This class is used by the OS to access sqllite, and delegates control to the appropriate OfflineTable (with a couple helper methods).
 *
 *
 * @author edwardf
 * @since 2.0
 */
public class OfflineHelper extends SQLiteOpenHelper implements DatabaseHandler {



    private static OfflineHelper _instance;
    private Context context;

    /**
     * This class is a synchronized Singleton, and this is how you get an instance of it.
     *
     * @param context the current active application context
     * @return an instance of the OfflineHelper class
     */
    public static synchronized OfflineHelper getInstance(Context context){
        if (_instance == null){
            _instance = new OfflineHelper(context);
            _instance.setContext(context);
        }
        return _instance;
    }


    /**
     *
     * @param context
     */
    private OfflineHelper(Context context){
        super(context, DB_NAME, null, DATABASE_VERSION);
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
    	Logger.INFO("offline helper onCreate");
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

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, collectionName);
        int change = db.updateWithOnConflict(COLLECTION_TABLE, values, COLUMN_NAME+"='" + collectionName +"'", null, db.CONFLICT_REPLACE);
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
            SQLiteDatabase db = getReadableDatabase();
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

    public GenericJson getEntity(AbstractClient client, AppData appData, OfflineMetaData id) {
        GenericJson ret;

        ret = getTable(appData.getCollectionName()).getEntity(this, client, id.id, appData.getCurrentClass(), null);

        return ret;
    }


    /**
     * This method creates a table for managing the names of all collections, if it doesn't already exist.
     */
    private void createCollectionTable(){
        SQLiteDatabase db = getWritableDatabase();

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
        SQLiteDatabase db = getReadableDatabase();

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
        SQLiteDatabase db = getReadableDatabase();
        return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public void runCommand(String command) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(command);

    }

    @Override
    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        SQLiteDatabase db = getWritableDatabase();
        return db.updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm);
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(table, nullColumnHack, values);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(table, whereClause, whereArgs);
    }

}
