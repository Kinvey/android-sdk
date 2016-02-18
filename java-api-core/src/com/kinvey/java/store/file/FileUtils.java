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
