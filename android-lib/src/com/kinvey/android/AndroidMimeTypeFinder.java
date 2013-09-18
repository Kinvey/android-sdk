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
package com.kinvey.android;


import static com.kinvey.android.Client.TAG;

import android.util.Log;
import android.webkit.MimeTypeMap;
import com.kinvey.java.MimeTypeFinder;
import com.kinvey.java.model.FileMetaData;

import java.io.File;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * @author edwardf
 */
public class AndroidMimeTypeFinder implements MimeTypeFinder {

    @Override
    public void getMimeType(FileMetaData meta, InputStream stream) {
        String mimetype = null;
        try {
            mimetype = URLConnection.guessContentTypeFromStream(stream);
            System.out.println("Kinvey - Client - File | mimetype from stream found as: " + mimetype);
        } catch (Exception e) {
            System.out.println("Kinvey - Client - File | content stream mimetype is unreadable, defaulting");
        }

        if (mimetype == null) {
            getMimeType(meta);
        } else {
            meta.setMimetype(mimetype);
        }


        stream.mark(0x100000 * 10);  //10MB mark limit
        int numBytes = 0;
        try {
            while (stream.read() != -1) {
                numBytes++;
            }

        } catch (Exception e) {
            Log.i(TAG, "error reading input stream to get size, setting it to 0");
            numBytes = 0;
        }
        try {
            stream.reset();
        } catch (Exception e) {
            Log.i(TAG, "error resetting stream!");

        }

        Log.i(TAG, "size is: " + numBytes);


        meta.setSize(numBytes);
    }

    @Override
    public void getMimeType(FileMetaData meta, File file) {
        if (file == null || file.getName() == null || meta == null) {
            Log.v(Client.TAG, "cannot calculate mimetype without a file or filename!");
            meta.setMimetype("application/octet-stream");
            return;
        }

        if (meta.getMimetype() != null && meta.getMimetype().length() > 0) {
            Log.v(Client.TAG, "Mimetype already set");
            return;
        }

        //check metadata file name first
        //check file's file name
        //check stream                          );

        String mimetype;
        String fileExt = "";

        if (meta.getFileName() != null && meta.getFileName().length() > 0 && meta.getFileName().lastIndexOf(".") > 0) {
            fileExt = meta.getFileName().substring(meta.getFileName().lastIndexOf('.'), meta.getFileName().length());
        }

        if (file.getName() != null && file.getName().lastIndexOf(".") > 0) {
            if (fileExt.length() == 0) {
                fileExt = file.getName().substring(file.getName().lastIndexOf('.'), file.getName().length());
            }
        }


        //did we get it from file extension? if not, attempt to get it from file contents
        if (fileExt.length() > 0) {
            mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.substring(1, fileExt.length()));
        } else {
            mimetype = "application/octet-stream";
        }

        meta.setMimetype(mimetype);
        meta.setSize(file.length());
    }

    @Override
    public void getMimeType(FileMetaData metaData) {
        if (metaData.getMimetype() != null && metaData.getMimetype().length() > 0) {
            return;
        }
        String mimetype = null;

        if (metaData.getFileName() != null) {
            int dotIndex = metaData.getFileName().lastIndexOf(".");

            if (dotIndex > 0 && dotIndex + 1 < metaData.getFileName().length()) {
                mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(metaData.getFileName().substring(dotIndex + 1, metaData.getFileName().length()));
            }
        }

        if (mimetype == null) {
            mimetype = "application/octet-stream";
        }
        metaData.setMimetype(mimetype);
    }
}

