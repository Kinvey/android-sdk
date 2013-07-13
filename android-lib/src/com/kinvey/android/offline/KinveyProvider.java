// /*
//  * Copyright (c) 2013 Kinvey Inc.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
//  * in compliance with the License. You may obtain a copy of the License at
//  *
//  * http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software distributed under the License
//  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
//  * or implied. See the License for the specific language governing permissions and limitations under
//  * the License.
//  */
// package com.kinvey.android.offline;

// import android.content.ContentProvider;
// import android.content.ContentResolver;
// import android.content.ContentValues;
// import android.content.UriMatcher;
// import android.database.Cursor;
// import android.database.sqlite.SQLiteDatabase;
// import android.database.sqlite.SQLiteQueryBuilder;
// import android.net.Uri;
// import android.text.TextUtils;

// import java.util.Arrays;
// import java.util.HashSet;

// /**
//  * @author edwardf
//  * @since 2.0
//  */
// public class KinveyProvider extends ContentProvider {

//     // database
//     private OfflineHelper database;

//     // Used for the UriMacher
//     private static final int TODOS = 10;
//     private static final int TODO_ID = 20;

//     //TODO must match xml/kinvey_sync
//     public static final String AUTHORITY = "com.kinvey.android.content";

//     private static final String BASE_PATH = "todos";
//     public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
//             + "/" + BASE_PATH);

//     public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
//             + "/todos";
//     public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
//             + "/todo";

//     private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//     static {
//         sURIMatcher.addURI(AUTHORITY, BASE_PATH, TODOS);
//         sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TODO_ID);
//     }

//     @Override
//     public boolean onCreate() {
//         database = new OfflineHelper(getContext(), "");//TODO
//         return false;
//     }

//     @Override
//     public Cursor query(Uri uri, String[] projection, String selection,
//                         String[] selectionArgs, String sortOrder) {

//         // Uisng SQLiteQueryBuilder instead of query() method
//         SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

//         // Check if the caller has requested a column which does not exists
//         checkColumns(projection);

//         // Set the table
//         queryBuilder.setTables(OfflineTable.TABLE_TODO);

//         int uriType = sURIMatcher.match(uri);
//         switch (uriType) {
//             case TODOS:
//                 break;
//             case TODO_ID:
//                 // Adding the ID to the original query
//                 queryBuilder.appendWhere(OfflineTable.COLUMN_ID + "="
//                         + uri.getLastPathSegment());
//                 break;
//             default:
//                 throw new IllegalArgumentException("Unknown URI: " + uri);
//         }

//         SQLiteDatabase db = database.getWritableDatabase();
//         Cursor cursor = queryBuilder.query(db, projection, selection,
//                 selectionArgs, null, null, sortOrder);
//         // Make sure that potential listeners are getting notified
//         cursor.setNotificationUri(getContext().getContentResolver(), uri);

//         return cursor;
//     }

//     @Override
//     public String getType(Uri uri) {
//         return null;
//     }

//     @Override
//     public Uri insert(Uri uri, ContentValues values) {
//         int uriType = sURIMatcher.match(uri);
//         SQLiteDatabase sqlDB = database.getWritableDatabase();
//         int rowsDeleted = 0;
//         long id = 0;
//         switch (uriType) {
//             case TODOS:
//                 id = sqlDB.insert(OfflineTable.TABLE_TODO, null, values);
//                 break;
//             default:
//                 throw new IllegalArgumentException("Unknown URI: " + uri);
//         }
//         getContext().getContentResolver().notifyChange(uri, null);
//         return Uri.parse(BASE_PATH + "/" + id);
//     }

//     @Override
//     public int delete(Uri uri, String selection, String[] selectionArgs) {
//         int uriType = sURIMatcher.match(uri);
//         SQLiteDatabase sqlDB = database.getWritableDatabase();
//         int rowsDeleted = 0;
//         switch (uriType) {
//             case TODOS:
//                 rowsDeleted = sqlDB.delete(OfflineTable.TABLE_TODO, selection,
//                         selectionArgs);
//                 break;
//             case TODO_ID:
//                 String id = uri.getLastPathSegment();
//                 if (TextUtils.isEmpty(selection)) {
//                     rowsDeleted = sqlDB.delete(OfflineTable.TABLE_TODO,
//                             OfflineTable.COLUMN_ID + "=" + id,
//                             null);
//                 } else {
//                     rowsDeleted = sqlDB.delete(OfflineTable.TABLE_TODO,
//                             OfflineTable.COLUMN_ID + "=" + id
//                                     + " and " + selection,
//                             selectionArgs);
//                 }
//                 break;
//             default:
//                 throw new IllegalArgumentException("Unknown URI: " + uri);
//         }
//         getContext().getContentResolver().notifyChange(uri, null);
//         return rowsDeleted;
//     }

//     @Override
//     public int update(Uri uri, ContentValues values, String selection,
//                       String[] selectionArgs) {

//         int uriType = sURIMatcher.match(uri);
//         SQLiteDatabase sqlDB = database.getWritableDatabase();
//         int rowsUpdated = 0;
//         switch (uriType) {
//             case TODOS:
//                 rowsUpdated = sqlDB.update(OfflineTable.TABLE_TODO,
//                         values,
//                         selection,
//                         selectionArgs);
//                 break;
//             case TODO_ID:
//                 String id = uri.getLastPathSegment();
//                 if (TextUtils.isEmpty(selection)) {
//                     rowsUpdated = sqlDB.update(OfflineTable.TABLE_TODO,
//                             values,
//                             OfflineTable.COLUMN_ID + "=" + id,
//                             null);
//                 } else {
//                     rowsUpdated = sqlDB.update(OfflineTable.TABLE_TODO,
//                             values,
//                             OfflineTable.COLUMN_ID + "=" + id
//                                     + " and "
//                                     + selection,
//                             selectionArgs);
//                 }
//                 break;
//             default:
//                 throw new IllegalArgumentException("Unknown URI: " + uri);
//         }
//         getContext().getContentResolver().notifyChange(uri, null);
//         return rowsUpdated;
//     }

//     private void checkColumns(String[] projection) {
//         String[] available = { OfflineTable.COLUMN_CATEGORY,
//                 OfflineTable.COLUMN_SUMMARY, OfflineTable.COLUMN_DESCRIPTION,
//                 OfflineTable.COLUMN_ID };
//         if (projection != null) {
//             HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
//             HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
//             // Check if all columns which are requested are available
//             if (!availableColumns.containsAll(requestedColumns)) {
//                 throw new IllegalArgumentException("Unknown columns in projection");
//             }
//         }
//     }

// }