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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.kinvey.android.Client;
import com.kinvey.java.model.FileMetaData;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * File Caching allows your application to store files locally in an arbitrary location, and maintains metadata about the contents of the cache in sqlite table.
 * <p/>
 * When saving a new file into the directory, an AsyncTask will be kicked off which will delete the oldest files until the cache size is under the threshold.
 *
 * @author edwardf
 */
public class FileCache {

    public static final String TAG = "kinvey - filecache";
    private static final long CACHE_LIMIT = 1024L * 1024L * 20; // 20 mb default

    //Maintains a file pointer to the location of the cache-- this better be a directory
    private File cacheDir;

    /**
     * Load up a file cache using internal storage's cache directory
     *
     * @param context
     */
    public FileCache(Context context){
        cacheDir = context.getCacheDir();
    }

    /**
     * Load up a file cache using a custom defined location
     *
     * @param location
     */
    public FileCache(File location){
        if (!location.exists()){
             location.mkdirs();
        }

        if (location.isDirectory()){
            cacheDir = location;
        }else{
            Log.e(TAG, "File Cache needs a directory! This isn't one -> " + location.getAbsolutePath());
            throw new NullPointerException("File Cache needs a directory! This isn't one -> " + location.getAbsolutePath());
        }
    }


    /**
     * Get a file from the local file cache.  If the file is not present, then this method will return null.
     * <p/> the file cache uses a sqlite table internally to maintain metadata about all locally stored files.
     *
     * @param context the current active context of the running application
     * @param id the id of the file to attempt to load
     * @return a `FileInputStream` for the file associated with the provided id, or null
     */
    public FileInputStream get(Context context, String id){
        Preconditions.checkNotNull(id, "String id cannot be null!");
        Preconditions.checkNotNull(context, "Context context cannot be null!");

        FileCacheSqlHelper helper = FileCacheSqlHelper.getInstance(context);// new FileCacheSqlHelper(context);
        String filename = helper.getFileNameForId(id);
        helper.dump();

        if (filename == null){
            //file name is not in the metadata table
            Log.i(TAG, "cache miss on db -> (" + id + ")" );
            return null;
        }

        File cachedFile = new File(cacheDir, filename);


        if (!cachedFile.exists()){
            //file name is in the metadata table, but the file doesn't exist
            //so remove it from the metadata table
            Log.i(TAG, "cache miss on filesystem-> (" + id + ", " + filename + ")" );
            helper.deleteRecord(id);
            return null;
        }
        Log.i(TAG, "cache hit -> (" + id + ", " + filename + ")" );

        FileInputStream ret = null;
        try{
            ret = new FileInputStream(cachedFile);
        }catch (Exception e){
            Log.e(TAG, "couldn't load cached file -> " + e.getMessage());
            e.printStackTrace();
        }

        return ret;
    }


    /**
     * Retrieve the filename of the file associated with the provided id
     *
     * @param context the applications' context
     * @param id the id of the file to lookup
     * @return {@code null} or the filename
     */
    public String getFilenameForID(Context context, String id){
        FileCacheSqlHelper helper = FileCacheSqlHelper.getInstance(context);// new FileCacheSqlHelper(context);
        return helper.getFileNameForId(id);
    }


    /**
     * Save a file into the file cache
     *
     *
     * @param context the application's context
     * @param client the current client
     * @param meta the filemetadata associated with the file
     * @param data the data of the file to write to disk
     */
    public synchronized void save(Context context, Client client, FileMetaData meta, byte[] data){
        Preconditions.checkNotNull(meta, "FileMetaData meta cannot be null!");
        Preconditions.checkNotNull(meta.getId(), "FileMetaData meta.getId() cannot be null!");
        Preconditions.checkNotNull(meta.getFileName(), "FileMetaData meta.getFileName() cannot be null!");
        Preconditions.checkNotNull(data, "byte[] data cannot be null!");

        Log.i(TAG, "cache saving -> (" + meta.getId() + ", " + meta.getFileName() + ") -> " + data.length );

        //insert into database table
        FileCacheSqlHelper helper = FileCacheSqlHelper.getInstance(context);//new FileCacheSqlHelper(context);
        helper.insertRecord(client, meta);
        helper.dump();

        //write to cache dir

        File file = new File(cacheDir, meta.getFileName());
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(data);

        }catch (Exception e){
            Log.e(TAG, "couldn't write file to cache -> " + e.getMessage());
            e.printStackTrace();
        }finally {
            try{
                if (os != null){
                    os.flush();
                    os.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //check size of cachedir
        //delete older files if necessary, until down to threshold size limit

        trimCache(context);
    }

    /**
     * This method compares the current size of all files in the cache dir to the limit size.  If there are too many files,
     * an async task will be kicked off to delete older files.
     *
     * @param context - the current application's context
     */
    public void trimCache(Context context){
        new TrimCache().execute(context);

    }

    /**
     * This task will check the size of the cache against a threshold, and remove the oldest files until the threshold is hit.
     *
     *
     */
    private class TrimCache extends AsyncTask<Context, Void, Void> {


        @Override
        protected Void doInBackground(Context ... context) {
            //first check current size of cache, and compare it to cache limit.



            long deleted = 0;
            long oldest = Calendar.getInstance().getTimeInMillis();
            long cacheSize = getDirSize(cacheDir);
            File toDelete = null;

            //while the starting size minus any deleted is still greater than the limit
            //iterate through all files and grab a reference to the oldest one
            //and then delete that oldest one
            //and add it's size to the deleted counter.

            while (cacheSize - deleted > CACHE_LIMIT){

                for (File f : cacheDir.listFiles()){
                    if (f.lastModified() < oldest){
                        oldest = f.lastModified();
                        toDelete = f;
                    }
                }

                if (toDelete != null){
                    deleted += toDelete.length();
                    toDelete.delete();
                    toDelete = null;
                }
            }

            return null;
        }
    }


    /**
     * Iterate through all files in a provided directory, and add up their sizes.
     *
     * @param dir the directory to get the total file size of
     * @return the size of all files in the provided directory
     */
    private static long getDirSize(File dir) {

        long size = 0;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

}
