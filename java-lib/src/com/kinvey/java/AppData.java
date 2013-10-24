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
package com.kinvey.nativejava;


import com.kinvey.java.AbstractClient;

/**
 * @author edwardf
 */
public class AppData<T> extends com.kinvey.java.AppData<T> {
    /**
     * Constructor to instantiate the AppData class.
     *
     * @param collectionName Name of the appData collection
     * @param myClass        Class Type to marshall data between.
     */
    protected AppData(String collectionName, Class<T> myClass, AbstractClient client) {
        super(collectionName, myClass, client);
    }
}
