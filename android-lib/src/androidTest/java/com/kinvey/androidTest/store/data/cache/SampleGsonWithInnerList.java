package com.kinvey.androidTest.store.data.cache;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import java.util.List;

/**
 * Created by Prots on 2/29/16.
 */
public class SampleGsonWithInnerList extends GenericJson {
    @Key
    private List<SampleGsonObject1> details;
    @Key
    private String _id;

    public SampleGsonWithInnerList(String _id, List<SampleGsonObject1> details) {
        this.details = details;
        this._id = _id;
    }

    public SampleGsonWithInnerList() {}

    public List<SampleGsonObject1> getDetails() {
        return details;
    }

    public void setDetails(List<SampleGsonObject1> details) {
        this.details = details;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }
}
