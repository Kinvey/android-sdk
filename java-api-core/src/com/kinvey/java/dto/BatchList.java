package com.kinvey.java.dto;

import com.google.api.client.json.GenericJson;

import java.util.List;

public class BatchList<T> extends GenericJson {

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


}
