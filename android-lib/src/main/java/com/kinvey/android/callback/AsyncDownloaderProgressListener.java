package com.kinvey.android.callback;


import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyCancellableCallback;
import com.kinvey.java.core.MediaHttpDownloader;

import java.io.IOException;

public interface AsyncDownloaderProgressListener<T> extends DownloaderProgressListener, KinveyCancellableCallback<T> {
    @Override
    public void onSuccess(T result);

    @Override
    public void onFailure(Throwable error);

    @Override
    public void progressChanged(MediaHttpDownloader uploader) throws IOException;

    @Override
    public void onCancelled();

    @Override
    public boolean isCancelled();
}
