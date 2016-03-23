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

package com.kinvey.java.core;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by edward on 10/20/14.
 */
public class RawJsonFactory extends com.google.api.client.json.JsonFactory {
    @Override
    public JsonParser createJsonParser(InputStream inputStream) throws IOException {
        return null;
    }

    @Override
    public JsonParser createJsonParser(InputStream inputStream, Charset charset) throws IOException {
        return null;
    }

    @Override
    public JsonParser createJsonParser(String s) throws IOException {
        return null;
    }

    @Override
    public JsonParser createJsonParser(Reader reader) throws IOException {
        return null;
    }

    @Override
    public JsonGenerator createJsonGenerator(OutputStream outputStream, Charset charset) throws IOException {
        return null;
    }

    @Override
    public JsonGenerator createJsonGenerator(Writer writer) throws IOException {
        return null;
    }
}
