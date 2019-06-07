package com.kinvey.java.dto;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Throwables;
import com.google.gson.Gson;
import java.util.List;

public class BatchList<T> extends GenericJson {

    @Key()
    private List<T> itemsList;

    private Gson gson = new Gson();

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
        String result = "";
        try {
            result = gson.toJson(itemsList);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        return result;
    }
}
