package com.kinvey.androidTest.model;

import com.google.api.client.util.Key;

import java.util.List;

public class PersonLongListName extends Person{

    @Key("sub_industry_ids")
    private List<String> list;

    @Key("author_list_test_field")
    private List<Author> authors;

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
}
