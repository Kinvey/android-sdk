package com.kinvey.java.store;

/**
 * Created by Prots on 2/4/16.
 */
public enum StoreType {
    SYNC(ReadPolicy.PREFER_LOCAL, WritePolicy.LOCAL_THEN_NETWORK, Long.MAX_VALUE),
    CACHE(ReadPolicy.FORCE_LOCAL, WritePolicy.FORCE_LOCAL, Long.MAX_VALUE),
    NETWORK(ReadPolicy.FORCE_NETWORK, WritePolicy.FORCE_NETWORK, 0L);

    public ReadPolicy readPolicy;

    public WritePolicy writePolicy;
    public long ttl;

    StoreType(ReadPolicy readPolicy, WritePolicy writePolicy, long ttl) {
        this.readPolicy = readPolicy;
        this.writePolicy = writePolicy;
        this.ttl = ttl;
    }
}
