package com.kinvey.java.store.requests.data;

import java.io.IOException;

/**
 * Created by Prots on 2/15/16.
 */
public class AbstractKinveySaveRequest<T> implements IRequest<T> {
    @Override
    public T execute() throws IOException {
        return null;
    }

    @Override
    public void cancel() {

    }
}
