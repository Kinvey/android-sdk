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
