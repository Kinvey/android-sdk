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

import com.google.api.client.json.GenericJson;

import java.util.HashMap;

/**
 * Use this class as a base Entity instead of {@code com.google.api.client.json.GenericJson} when using the LinkedData API.
 * <p>
 * This class maintains a Map of linked files, using the JSONKey of the field as the key and a {@code com.kinvey.java.LinkedResources.LinkedFile} as the value.
 * </p>
 * <p>
 * The LinkedData API uses this map to determine if there are any attachments to download.
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
