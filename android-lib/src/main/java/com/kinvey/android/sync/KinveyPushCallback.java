package com.kinvey.android.sync;

import com.kinvey.java.core.KinveyClientCallback;

/**
 * This class provides callbacks from requests executed by the Sync API.
 *
 * @author mvakulich
 */
public interface KinveyPushCallback extends KinveyClientCallback<Integer> {

    /**
     * Used to indicate successful execution of a request by the background service.
     */
    void onSuccess(Integer result);

    /**
     * Used to indicate the failed execution of a request by the background service.
     */
    void onFailure(Throwable error);

    void onProgress(long current, long all);
}
