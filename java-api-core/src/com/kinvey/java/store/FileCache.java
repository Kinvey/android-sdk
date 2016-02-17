package com.kinvey.java.store;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.model.FileMetaData;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author edwardf
 */
public interface FileCache {


    public FileInputStream get(AbstractClient client, String id);

    public String getFilenameForID(AbstractClient client, String id);

    public void save(AbstractClient client, FileMetaData meta, InputStream is);
}
