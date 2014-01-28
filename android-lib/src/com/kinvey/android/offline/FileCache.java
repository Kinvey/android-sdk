package com.kinvey.android.offline;

import android.content.Context;
import com.kinvey.android.Client;
import com.kinvey.java.model.FileMetaData;

import java.io.FileInputStream;

/**
 * @author edwardf
 */
public interface FileCache {


    public FileInputStream get(Context context, String id);

    public String getFilenameForID(Context context, String id);

    public void save(Context context, Client client, FileMetaData meta, byte[] data);
}
