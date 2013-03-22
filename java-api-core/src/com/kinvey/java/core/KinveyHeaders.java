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
package com.kinvey.java.core;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.Key;

/**
 * @author m0rganic
 * @since 2.0
 */
public class KinveyHeaders extends HttpHeaders {

    private String VERSION = "2.1.0";

    @Key("X-Kinvey-API-Version")
    private String kinveyApiVersion = "2";

    private String userAgent = "android-kinvey-http/"+ VERSION;

    public KinveyHeaders() {
        super();
        setUserAgent(userAgent);
    }
}
