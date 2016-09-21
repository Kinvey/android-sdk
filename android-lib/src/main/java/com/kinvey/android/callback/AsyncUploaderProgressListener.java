package com.kinvey.android.callback;


import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;

import java.io.IOException;

public interface AsyncUploaderProgressListener<T> extends UploaderProgressListener, KinveyClientCallback<T> {
    @Override
    public void onSuccess(T result);

    @Override
    public void onFailure(Throwable error);

    @Override
    public void progressChanged(MediaHttpUploader uploader) throws IOException;
}
