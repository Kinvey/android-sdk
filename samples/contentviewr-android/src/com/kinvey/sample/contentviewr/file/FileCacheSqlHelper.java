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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.kinvey.java.model.FileMetaData;

/**
 * @author edwardf
 */
public class FileCacheSqlHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "KINVEY_FILE.db";
    private static final String FILE_CACHE_TABLE = "file_cache";
    private static final int DB_VERSION = 1;

    private static final String COLUMN_FILENAME = "filename";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TIMESTAMP = "timestamp";



    public FileCacheSqlHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String command = "CREATE TABLE IF NOT EXISTS "
                + FILE_CACHE_TABLE
                + "("
                + COLUMN_ID + " TEXT not null, "
                + COLUMN_FILENAME + " TEXT not null, "
                + COLUMN_TIMESTAMP + " INTEGER not null"
                + ");";

                ;
        runCommand(command);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int current, int next) {
        //no need for this method yet
    }

    public void runCommand(String command) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(command);

    }

    public String getFileNameForId(String id){
        //Cursor c = getReadableDatabase().query()
        return "";

    }

    public void insertRecord(FileMetaData meta){

    }

    public void deleteRecord(FileMetaData meta){

    }


}
