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

    public void remove(AbstractClient client, String id);
}
