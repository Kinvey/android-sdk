package com.kinvey.java.store.requests;

import com.google.api.client.json.GenericJson;

/**
 * Created by Prots on 2/8/16.
 */
public class PushRequest<T extends GenericJson> extends AbstractKinveyExecuteRequest<T> {
    @Override
    public Void execute() {

        return null;
    }

    @Override
    public void cancel() {

    }
}
