package com.kinvey.android.callback;


import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.FileMetaData;

import java.io.IOException;

public interface AsyncDownloaderProgressListener<T> extends DownloaderProgressListener, KinveyClientCallback<T> {
    @Override
    public void onSuccess(T result);

    @Override
    public void onFailure(Throwable error);

    @Override
    public void progressChanged(MediaHttpDownloader uploader) throws IOException;
}
