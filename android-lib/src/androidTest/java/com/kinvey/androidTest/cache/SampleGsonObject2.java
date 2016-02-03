package com.kinvey.androidTest.cache;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by Prots on 1/27/16.
 */
public class SampleGsonObject2 extends GenericJson {
    @Key("_id")
    public String _id;
    @Key("title")
    public String title;
    @Key("test")
    public Integer test;

    public SampleGsonObject2(){};

    public SampleGsonObject2(String _id, String title, Integer test) {
        this._id = _id;
        this.title = title;
        this.test = test;
    }
}
