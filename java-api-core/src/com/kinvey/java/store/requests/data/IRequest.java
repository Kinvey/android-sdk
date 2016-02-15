package com.kinvey.java.store.requests.data;

import java.io.IOException;

/**
 * Created by Prots on 2/8/16.
 */
public interface IRequest<T> {
    T execute() throws IOException;
    void cancel();
}
