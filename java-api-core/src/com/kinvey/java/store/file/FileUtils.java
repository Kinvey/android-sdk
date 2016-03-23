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

package com.kinvey.java.store.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Prots on 2/18/16.
 */
abstract public class FileUtils {

    private static final int CHUNK_SIZE = 1024*1024; //1MB


    public static void copyStreams(InputStream is, OutputStream os) throws IOException {

        byte[] chunk = new byte[CHUNK_SIZE];
        int curChunk = 0;
        while ((curChunk = is.read(chunk, 0, CHUNK_SIZE)) > 0){
            os.write(chunk, 0, CHUNK_SIZE);
        }

    }
}
