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
package com.kinvey.sample.contentviewr.file;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.api.client.json.JsonGenerator;
import com.kinvey.android.Client;
import com.kinvey.java.model.FileMetaData;

import java.io.StringWriter;

/**
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
            //_instance.setContext(context);
        }
        return _instance;
    }


    private FileCacheSqlHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

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

    public void runCommand(String command, SQLiteDatabase db) {
        //SQLiteDatabase db = getWritableDatabase();
        db.execSQL(command);

    }

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
            Log.e(FileCache.TAG, "unable to serialize JSON! -> " + ex);
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

    public void deleteRecord(String id){

        SQLiteDatabase db = getWritableDatabase();
        db.delete(FILE_CACHE_TABLE, COLUMN_ID+"='" + id +"'", null);

    }


    public void dump() {
        if (false){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(FILE_CACHE_TABLE, new String[]{COLUMN_ID, COLUMN_FILENAME}, null, null, null, null, null);
        while(c.moveToNext()){
            Log.i("DUMP", "********");
            Log.i("DUMP", "********");
            Log.i("DUMP", "********");
            Log.i("DUMP", c.getString(0) + " and " + c.getString(1));
            Log.i("DUMP", "********");
            Log.i("DUMP", "********");
            Log.i("DUMP", "********");
        }
        c.close();;
        }

    }
}
