package com.kinvey.androidTest.cache;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.gson.Gson;

/**
 * Created by Prots on 1/27/16.
 */
public class SampleGsonObject1 extends GenericJson {
    @Key("_id")
    public String _id;

    @Key("title")
    public String title;

    public SampleGsonObject1(){};

    public SampleGsonObject1(String _id, String title) {
        this._id = _id;
        this.title = title;
    }
}
