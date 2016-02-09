package com.kinvey.java.store;

/**
 * Created by Prots on 2/4/16.
 */
public enum StoreType {
    SYNC(ReadPolicy.FORCE_LOCAL, WritePolicy.FORCE_NETWORK),
    CACHE(ReadPolicy.PREFER_LOCAL, WritePolicy.LOCAL_THEN_NETWORK),
    NETWORK(ReadPolicy.FORCE_NETWORK, WritePolicy.FORCE_NETWORK);

    public ReadPolicy readPolicy;

    public WritePolicy writePolicy;

    StoreType(ReadPolicy readPolicy, WritePolicy writePolicy) {
        this.readPolicy = readPolicy;
        this.writePolicy = writePolicy;
    }
}
