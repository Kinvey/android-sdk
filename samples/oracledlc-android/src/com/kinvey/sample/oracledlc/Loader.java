/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.oracledlc;

import com.kinvey.sample.oracledlc.appData.AppDataActivity;

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
