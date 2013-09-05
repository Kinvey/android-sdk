/** 
 * Copyright (c) 2013 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.kinvey.sample.kitchensink;

import com.kinvey.sample.kitchensink.appData.AppDataActivity;
import com.kinvey.sample.kitchensink.custom.CustomEndpointActivity;
import com.kinvey.sample.kitchensink.file.FileActivity;
import com.kinvey.sample.kitchensink.push.PushActivity;
import com.kinvey.sample.kitchensink.user.UserActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edwardf
 * @since 2.0
 */
public class Loader {


    public static List<Feature> getFeatureList(){
        ArrayList<Feature> featureList = new ArrayList<Feature>();

        Feature appData = new Feature("App Data", "App Data can be used to store and retrieve objects using Kinvey's BaaS", AppDataActivity.class);
        featureList.add(appData);

        Feature files = new Feature("File", "Store large file, images or video", FileActivity.class);
        featureList.add(files);

        Feature push = new Feature("Push", "Enable Push notifications using GCM", PushActivity.class);
        featureList.add(push);

        Feature customEndpoint = new Feature("Custom Endpoints", "Define behavoir on you backend and run it from the client.", CustomEndpointActivity.class);
        featureList.add(customEndpoint);

        Feature user = new Feature("User", "Store data associated with a user and perform user lookup operations", UserActivity.class);
        featureList.add(user);



        return featureList;
    }

    //--------------class declaration of features
    public static class Feature{

        private String name;
        private String blurb;
        private Class activity;

        public Feature(String name, String blurb, Class activity){
            this.name = name;
            this.blurb = blurb;
            this.activity = activity;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBlurb() {
            return blurb;
        }

        public void setBlurb(String blurb) {
            this.blurb = blurb;
        }

        public Class getActivity() {
            return activity;
        }

        public void setActivity(Class activity) {
            this.activity = activity;
        }
    }
}
