package com.kinvey.java.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.api.client.util.Throwables;
import com.kinvey.java.core.RawJsonFactory;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;

public class BatchList<T> extends GenericJson {

    @Key()
    private List<T> itemsList;

    public BatchList(List<T> itemsList) {
        this.itemsList = itemsList;
    }

    public List<T> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<T> itemsList) {
        this.itemsList = itemsList;
    }

    @Override
    public String toString() {
        JsonFactory factory = getFactory();
        if (factory == null) {
            factory = new JacksonFactory();
            setFactory(factory);
        }
        String result = "";
        try {
            result = factory.toString(itemsList);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return result;
    }
}
