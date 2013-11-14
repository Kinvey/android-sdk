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
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import com.kinvey.android.offline.DatabaseHandler;
import com.kinvey.android.offline.OfflineTable;

import java.util.List;

/**
 * @author edwardf
 */
public class SecureHelper extends SQLiteOpenHelper implements DatabaseHandler {


    private static SecureHelper _instance;

    private Context context;

    /**
     * This class is a synchronized Singleton, and this is how you get an instance of it.
     *
     * @param context the current active application context
     * @return an instance of the OfflineHelper class
     */
    public static synchronized SecureHelper getInstance(Context context){
        if (_instance == null){
            _instance = new SecureHelper(context);
        }
        return _instance;
    }

    private SecureHelper(Context context){
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }



    @Override
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void runCommand(String command) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OfflineTable getTable(String collectionName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCollectionTables() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }



}
