package com.kinvey.java.offline;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.model.FileMetaData;

import java.io.FileInputStream;

/**
 * @author edwardf
 */
public interface FileCache {


    public FileInputStream get(AbstractClient client, String id);

    public String getFilenameForID(AbstractClient client, String id);

    public void save(AbstractClient client, FileMetaData meta, byte[] data);
}
