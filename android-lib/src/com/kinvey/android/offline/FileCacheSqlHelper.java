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

import java.io.StringWriter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.api.client.json.JsonGenerator;
import com.kinvey.android.Client;
import com.kinvey.java.model.FileMetaData;

/**
 * This class manages a sqlite database for maintaining metadata about cached files.
 * Each file must have a unique identifier and a filename.  If the filename is unique, that can be used as the unique id.
 * <p/>
 * This class provides methods for saving new records, retrieving records, and deleting records.
 *
 * @author edwardf
 */
public class FileCacheSqlHelper extends SQLiteOpenHelper {

    private static FileCacheSqlHelper _instance;

    private static final String DB_NAME = "KINVEY_FILE.db";
    private static final String FILE_CACHE_TABLE = "file_cache";
    private static final int DB_VERSION = 1;

    private static final String COLUMN_FILENAME = "filename";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_JSON = "json";


    /**
     * This class is a synchronized Singleton, and this is how you get an instance of it.
     *
     * @param context the current active application context
     * @return an instance of the OfflineHelper class
     */
    public static synchronized FileCacheSqlHelper getInstance(Context context){
        if (_instance == null){
            _instance = new FileCacheSqlHelper(context);
        }
        return _instance;
    }

    /**
     * Private constructor to force a singleton
     *
     * @param context
     */
    private FileCacheSqlHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    /**
     * Called by android first time this database is accessed, to create it.
     *
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String command = "CREATE TABLE IF NOT EXISTS "
                + FILE_CACHE_TABLE
                + "("
                + COLUMN_ID + " TEXT not null, "
                + COLUMN_FILENAME + " TEXT not null, "
                + COLUMN_JSON + " TEXT not null"
                + ");";
        runCommand(command, sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int current, int next) {
        //no need for this method yet
    }


    /**
     * Execute an arbitrary sqlite command against a sqlite database.
     * <p/>
     * no validation is performed here.
     *
     * @param command the command to execute
     * @param db the database to execute the command against
     */
    public void runCommand(String command, SQLiteDatabase db) {
        //SQLiteDatabase db = getWritableDatabase();
        db.execSQL(command);

    }


    /**
     * Return the filename of the file on disc relavant to the provided id
     *
     *
     * @param id the id of the filename to look up
     * @return the filename of the file or {@code null}
     */
    public String getFileNameForId(String id){

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(FILE_CACHE_TABLE, new String[]{COLUMN_FILENAME},  COLUMN_ID+"='" + id +"'", null, null, null, null);

        String filename = null;
        if (c.moveToFirst()){
            filename = c.getString(0);
        }
        c.close();

        return filename;


    }


    /**
     * Insert a new record into the file cache metadata table.
     *
     * @param client a Kinvey Client, needed for JSON serialization
     * @param meta the {@link FileMetaData} object for the file, containing an id and a filename
     */
    public void insertRecord(Client client, FileMetaData meta){

        ContentValues values = new ContentValues();

        String jsonResult = "";
        StringWriter writer = new StringWriter();
        try {
            JsonGenerator generator = client.getJsonFactory().createJsonGenerator(writer);
            generator.serialize(meta);
            generator.flush();
            jsonResult = writer.toString();
        } catch (Exception ex) {
            Log.e(SQLiteFileCache.TAG, "unable to serialize JSON! -> " + ex);
        }

        values.put(COLUMN_JSON, jsonResult);

        values.put(COLUMN_ID, meta.getId());
        values.put(COLUMN_FILENAME, meta.getFileName());


        SQLiteDatabase db = getWritableDatabase();

        int change = db.updateWithOnConflict(FILE_CACHE_TABLE, values,  COLUMN_ID+"='" + meta.getId() +"'", null, db.CONFLICT_REPLACE);
        if (change == 0){
            db.insert(FILE_CACHE_TABLE, null, values);
        }
        db.close();
    }


    /**
     * Remove a record from the table
     *
     * @param id the id of the record to remove
     */
    public void deleteRecord(String id){

        SQLiteDatabase db = getWritableDatabase();
        db.delete(FILE_CACHE_TABLE, COLUMN_ID+"='" + id +"'", null);

    }


    /**
     * Dump the contents of the database to the logs, used for debugging purposes.
     *
     */
    public void dump() {
        if (false){
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.query(FILE_CACHE_TABLE, new String[]{COLUMN_ID, COLUMN_FILENAME}, null, null, null, null, null);
            while(c.moveToNext()){
//                Logger.INFO("********");
//                Logger.INFO("********");
//                Logger.INFO("********");
//                Logger.INFO(c.getString(0) + " and " + c.getString(1));
//                Logger.INFO("********");
//                Logger.INFO("********");
//                Logger.INFO("********");
            }
            c.close();;
        }

    }
}
