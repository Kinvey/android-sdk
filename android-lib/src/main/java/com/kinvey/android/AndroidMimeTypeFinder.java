/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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
package com.kinvey.android;


import java.io.File;
import java.io.InputStream;
import java.net.URLConnection;

import android.webkit.MimeTypeMap;

import com.kinvey.java.Logger;
import com.kinvey.java.MimeTypeFinder;
import com.kinvey.java.model.FileMetaData;

/**
 * Uses Android features to determine the MIME type of a provided file.
 * <p/>
 * There are various `getMimeType(...)` methods, which all take various parameters as well as a {@link FileMetaData} object.
 * When the mimetype is found, it will be set in the provided {@link FileMetaData}
 *
 * @author edwardf
 */
public class AndroidMimeTypeFinder implements MimeTypeFinder {

    /**
     * Calculate MimeType from an InputStream
     *
     * @param meta the {@link FileMetaData} to populate
     * @param stream the stream of the data
     */
    @Override
    public void getMimeType(FileMetaData meta, InputStream stream) {
        String mimetype = null;
        try {
            mimetype = URLConnection.guessContentTypeFromStream(stream);
            Logger.INFO("Kinvey - Client - NetworkFileManager | mimetype from stream found as: " + mimetype);
        } catch (Exception e) {
        	Logger.WARNING("Kinvey - Client - NetworkFileManager | content stream mimetype is unreadable, defaulting");
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
        	Logger.WARNING("error reading input stream to get size, setting it to 0");
            numBytes = 0;
        }
        try {
            stream.reset();
        } catch (Exception e) {
        	Logger.ERROR("error resetting stream!");

        }

        Logger.INFO("size is: " + numBytes);


        meta.setSize(numBytes);
    }

    /**
     * Calculate MimeType from a {@link File} object
     *
     * @param meta the {@link FileMetaData} to populate
     * @param file the file of the data
     */
    @Override
    public void getMimeType(FileMetaData meta, File file) {
        if (file == null || file.getName() == null || meta == null) {
        	Logger.WARNING("cannot calculate mimetype without a file or filename!");
            meta.setMimetype("application/octet-stream");
            return;
        }

        if (meta.getMimetype() != null && meta.getMimetype().length() > 0) {
        	Logger.INFO("Mimetype already set");
            return;
        }

        //check metadata file name first
        //check file's file name
        //check stream                          );

        String mimetype;
        String fileExt = "";

        if (meta.getFileName() != null && meta.getFileName().length() > 0 && meta.getFileName().lastIndexOf("") > 0) {
            fileExt = meta.getFileName().substring(meta.getFileName().lastIndexOf('.'), meta.getFileName().length());
        }

        if (file.getName() != null && file.getName().lastIndexOf("") > 0) {
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

    /**
     * Calculate MimeType from a file name
     *
     * @param metaData the {@link FileMetaData} to populate, also containing the filename (with file extension)
     */
    @Override
    public void getMimeType(FileMetaData metaData) {
        if (metaData.getMimetype() != null && metaData.getMimetype().length() > 0) {
            return;
        }
        String mimetype = null;

        if (metaData.getFileName() != null) {
            int dotIndex = metaData.getFileName().lastIndexOf("");

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

