package com.kinvey.android.callback;


import com.kinvey.java.core.KinveyCancellableCallback;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;

import java.io.IOException;

public interface AsyncUploaderProgressListener<T> extends UploaderProgressListener, KinveyCancellableCallback<T> {
    @Override
    public void onSuccess(T result);

    @Override
    public void onFailure(Throwable error);

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException;

    @Override
    public void onCancelled();

    @Override
    public boolean isCancelled();
}
