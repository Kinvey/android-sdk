/*
 * Copyright (c) 2014, Kinvey, Inc.
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

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.kinvey.java.core.RawJsonFactory;

/** {@inheritDoc}
 *
 * @author edwardf
 * */
public class JavaJson {

    public static JsonFactory newCompatibleJsonFactory(JSONPARSER parser) {
        switch (parser){
            case GSON:
                return new GsonFactory();
            case JACKSON:
                return new JacksonFactory();
            case RAW:
                return new RawJsonFactory();
            default:
                return new GsonFactory();
        }

    }


    public enum JSONPARSER {
        GSON,
        JACKSON,
        RAW;

        public static String getOptions(){
            StringBuilder values = new StringBuilder();
            for (JSONPARSER p : JSONPARSER.values()){
                values.append(p + ", ");
            }

            values.setLength(values.length() - 2);

            return values.toString();
        }
    }



}
