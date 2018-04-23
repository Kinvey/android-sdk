package com.kinvey.java.store;

import com.kinvey.java.Query;
import com.kinvey.java.network.NetworkManager;

import java.util.concurrent.Callable;

public class CallableAsyncPullRequestHelper implements Callable {

    private final NetworkManager.Pull pullRequest;
    private final Query query;

    CallableAsyncPullRequestHelper(NetworkManager.Pull pull, Query query) {
        this.pullRequest = pull;
        this.query = query;
    }

    @Override
    public PullTaskResponse call() throws Exception {
        return new PullTaskResponse(pullRequest.execute(), query);
    }
}
