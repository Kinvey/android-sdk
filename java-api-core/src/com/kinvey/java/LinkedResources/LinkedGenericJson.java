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

package com.kinvey.java.LinkedResources;

import com.google.api.client.json.GenericJson;

import java.util.HashMap;

/**
 * Use this class as a base Entity instead of {@code com.google.api.client.json.GenericJson} when using the LinkedNetworkManager API.
 * <p>
 * This class maintains a Map of linked files, using the JSONKey of the field as the key and a {@code com.kinvey.java.LinkedResources.LinkedFile} as the value.
 * </p>
 * <p>
 * The LinkedNetworkManager API uses this map to determine if there are any attachments to download.
 * </p>
 *
 * @author mjsalinger
 * @since 2.0
 */
public abstract class LinkedGenericJson extends GenericJson {
    private HashMap<String, LinkedFile> files;

    /**
     * General constructor, initializes map of LinkedFiles
     */
    public LinkedGenericJson() {
        super();
        files = new HashMap<String, LinkedFile>();
    }

    public void putFile(String key, LinkedFile file) {
        files.put(key, file);
    }
    public void putFile(String key) {
        files.put(key, null);
    }

    public LinkedFile getFile(String key) {
        return files.get(key);
    }

    public HashMap<String,LinkedFile> getAllFiles() {
        return files;
    }
}
