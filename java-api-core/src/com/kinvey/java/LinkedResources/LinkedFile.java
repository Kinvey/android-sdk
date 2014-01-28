/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java.LinkedResources;


import java.io.*;
import java.util.HashMap;

/**
 * This class maintains metadata and java specific file access to a File associated with an Entity through the {@code com.kinvey.java.LinkedData} API.
 *
 * There are references to a `ByteArrayInputStream as well as a `ByteArrayOutputStream`, which can be used to stream to/from the file.
 *
 * NOTE:  It is the responsibility of the client application to close these streams appropriately after usage.
 *
 *
 * @author mjsalinger
 * @author edwardf
 * @since 2.0
 */
public class LinkedFile {

    private String id;
    private String fileName;
    private ByteArrayInputStream input = null;
    private ByteArrayOutputStream output = null;
    private boolean resolve = true;
    private HashMap<String, Object> extras;

    /**
     * Constructor for a LinkedFile, sets BOTH filename and id to be input
     *
     * @param id - the filename which is also used as the id
     */
    public LinkedFile(String id) {
        this.id = id;
        this.fileName = id;
    }

    /**
     * Constructor for a LinkedFile, sets NEITHER a filename or an id
     */
    public LinkedFile(){

    }

    /**
     * Constructor for LinkedFile, allowing unique id and filename
     *
     *
     * @param id the id to use for the linked file
     * @param filename the filename of the linkedfile
     */
    public LinkedFile(String id, String filename){
        this.id = id;
        this.fileName = filename;

    }

    /**
     * Get the id of a Linked File
     *
     * @return  the id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ByteArrayInputStream getInput() {
        return input;
    }

    public void setInput(ByteArrayInputStream input) {
        this.input = input;
    }

    public ByteArrayOutputStream getOutput() {
        return output;
    }

    public void setOutput(ByteArrayOutputStream output) {
        this.output = output;
    }

    public boolean isResolve() {
        return resolve;
    }

    public void setResolve(boolean resolve) {
        this.resolve = resolve;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    /**
     * Add an extra property to this KinveyFile.  When the File is uploaded through LinkedData, any extra properties here
     * will be added to the {@link com.kinvey.java.model.FileMetaData} object created during the file upload.
     *
     * @param key the key to use for the extra associated with the {@link com.kinvey.java.model.FileMetaData}
     * @param value the value of the extra
     */
    public void addExtra(String key, Object value){
        if (extras == null){
            extras = new HashMap<String, Object>();
        }
        extras.put(key, value);

    }

    /**
     * Retrieve an extra property by key associated with this KinveyFile.
     *
     * @param key the key used to define the property
     * @return the value of the property, or null if it hasn't been set.
     */
    public Object getExtra(String key){
        if (extras != null && extras.containsKey(key)){
            return extras.get(key);
        }
        return null;

    }

    public boolean hasExtras(){
        return (extras != null && extras.size() > 0);
    }

    public HashMap<String, Object> getExtras(){
        return this.extras;
    }

}
