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
import com.kinvey.sample.contentviewr.model.ContentType;
import com.kinvey.sample.contentviewr.model.SourceType;

/**
 * @author edwardf
 */
public class WindowFactory {


    public static Viewer getViewer(ContentType.WINDOWTYPE source){

        Viewer ret = null;
        switch (source){

            case WEB:
                ret = new HTMLViewer();
                break;
            case IMAGE:
                ret = new ImageViewer();
                break;
            case PDF:
                ret = new PDFViewer();


        }

        return ret;


//        SOURCELOCATION type = SOURCELOCATION.getByName(jsontype);
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
