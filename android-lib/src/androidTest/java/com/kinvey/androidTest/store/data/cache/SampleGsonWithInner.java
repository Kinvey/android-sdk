package com.kinvey.androidTest.store.data.cache;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Created by Prots on 2/29/16.
 */
public class SampleGsonWithInner extends GenericJson {
    @Key
    private SampleGsonObject1 details;
    @Key
    private String _id;

    public SampleGsonWithInner(String _id, SampleGsonObject1 details) {
        this.details = details;
        this._id = _id;
    }

    public SampleGsonWithInner() {}

    public SampleGsonObject1 getDetails() {
        return details;
    }

    public void setDetails(SampleGsonObject1 details) {
        this.details = details;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}
