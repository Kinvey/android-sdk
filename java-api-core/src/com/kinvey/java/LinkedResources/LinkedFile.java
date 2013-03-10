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


import java.util.Arrays;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class LinkedFile {
    private byte[] fileData;
    private String fileName;

    public LinkedFile(byte[] fileData, String fileName) {
        this.fileData = Arrays.copyOf(fileData, fileData.length);
        this.fileName = fileName;
    }



    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = Arrays.copyOf(fileData, fileData.length);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
