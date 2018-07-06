package com.kinvey.java.model;

import java.util.List;

/**
 * Created by yuliya on 10/26/17.
 */

public abstract class AbstractKinveyExceptionsListResponse {

    private List<Exception> listOfExceptions;

    public List<Exception> getListOfExceptions() {
        return listOfExceptions;
    }

    public void setListOfExceptions(List<Exception> listOfExceptions) {
        this.listOfExceptions = listOfExceptions;
    }

}
