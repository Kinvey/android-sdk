package com.kinvey.java.deltaset;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.util.Date;

/**
 * Created by Prots on 12/11/15.
 */
public class DeltaSetItem extends GenericJson {
    @Key("_id")
    private String id;

    @Key("_kmd")
    private  KMD kmd;

    public String getId() {
        return id;
    }

    public KMD getKmd() {
        return kmd;
    }

    public static class KMD {
        @Key("lmt")
        private String lmt;

        public String getLmt(){
            return lmt;
        }
    }

}
