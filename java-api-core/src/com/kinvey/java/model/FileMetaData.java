package com.kinvey.java.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 *
 *
 * @author edwardf
 */
public class FileMetaData extends GenericJson{


    @Key("_id")
    private String id;

    @Key("_filename")
    private String fileName;

    @Key("size")
    private long size;

    @Key("mimeType")
    private String mimetype;

    @Key("_acl")
    private KinveyMetaData.AccessControlList acl;

    @Key("_uploadURL")
    private String uploadUrl;

    @Key("_downloadURL")
    private String downloadURL;

    @Key("_public")
    private boolean _public = false;




    public FileMetaData() {
    }

    public FileMetaData(String id){
        setId(id);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public KinveyMetaData.AccessControlList getAcl() {
        return acl;
    }

    public void setAcl(KinveyMetaData.AccessControlList acl) {
        this.acl = acl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public boolean is_public() {
        return _public;
    }

    public void set_public(boolean _public) {
        this._public = _public;
    }
}
