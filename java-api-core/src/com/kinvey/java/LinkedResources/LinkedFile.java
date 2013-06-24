/*
 * Copyright (c) 2013 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.java.LinkedResources;


import java.io.*;

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
    private ByteArrayInputStream input = null;
    private ByteArrayOutputStream output = null;
    private boolean resolve = true;

    /**
     * Constructor for a LinkedFile
     *
     * @param id - the id of the linked file
     */
    public LinkedFile(String id) {
        this.id = id;
    }

    /**
     * Get the file name of a Linked File
     *
     * @return  the file name
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
}
