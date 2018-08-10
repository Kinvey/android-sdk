package com.kinvey.androidTest.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class EntityForInQueryTest extends GenericJson{

    public static final String COLLECTION = "InQueryTestCollection";

    @Key
    private long longVal;
    @Key
    private String stringVal;
    @Key
    private boolean booleanVal;
    @Key
    private int intVal;
    @Key
    private float floatVal;

    public long getLongVal() {
        return longVal;
    }

    public void setLongVal(long longVal) {
        this.longVal = longVal;
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    public boolean isBooleanVal() {
        return booleanVal;
    }

    public void setBooleanVal(boolean booleanVal) {
        this.booleanVal = booleanVal;
    }

    public int getIntVal() {
        return intVal;
    }

    public void setIntVal(int intVal) {
        this.intVal = intVal;
    }

    public float getFloatVal() {
        return floatVal;
    }

    public void setFloatVal(float floatVal) {
        this.floatVal = floatVal;
    }
}
