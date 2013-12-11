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
package com.kinvey.sample.contentviewr.windows;

import com.kinvey.sample.contentviewr.model.ContentItem;
import com.kinvey.sample.contentviewr.model.SourceType;

/**
 * @author edwardf
 */
public class WindowFactory {

    public enum TYPE {
        HTML("html"), IMAGE("image"), PDF("");

        private String jsonname;

        private TYPE(String name){
            this.jsonname = name;

        }

        public String getName(){
            return this.jsonname;
        }

        public static TYPE getByName(String name){
            for (TYPE t : TYPE.values()){
                if (t.getName().equals(name)){
                    return t;
                }
            }
            return HTML;
        }
    }



    public static Viewer getViewer(String jsontype){
        TYPE type = TYPE.getByName(jsontype);
        Viewer ret = null;
        switch (type){
            case HTML:
                ret = new HTMLViewer();
                break;
            case IMAGE:
                ret = new ImageViewer();
                break;
            case PDF:
                break;

        }


        return ret;
    }

    public static Viewer getViewer(SourceType source){

        Viewer ret = null;
        switch (source.getType()){

            case WEBSITE:
                ret = new HTMLViewer();
                break;
            case FILE:
                ret = new ImageViewer();
                break;

        }

        return ret;


//        TYPE type = TYPE.getByName(jsontype);
//        Viewer ret = null;
//        switch (type){
//            case HTML:
//                ret = new HTMLViewer();
//                break;
//            case IMAGE:
//                ret = new ImageViewer();
//                break;
//            case PDF:
//                break;
//
//        }
//
//
//        return ret;
    }



}
