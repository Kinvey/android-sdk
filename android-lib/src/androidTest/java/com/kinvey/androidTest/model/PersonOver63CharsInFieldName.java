package com.kinvey.androidTest.model;


import com.google.api.client.util.Key;

import java.util.List;

public class PersonOver63CharsInFieldName extends Person{

    @Key(LONG_NAME)
    private List<String> list;

}
