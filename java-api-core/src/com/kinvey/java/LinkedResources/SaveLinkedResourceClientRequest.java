/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.java.LinkedResources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Logger;
import com.kinvey.java.MimeTypeFinder;
import com.kinvey.java.annotations.ReferenceHelper;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.store.FileStore;

/**
 * Implementation of a Client Request, which can upload linked resources.
 * <p>
 * On the call to execute, if a file is a LinkedGenericJson, then this iterates through all the attachments and uploads them.
 * Once all files have been uploaded, a call to super.execute() is made.
 * </p>
 * <p>
 * call setUploadProgressListener to get callbacks for all file uploads.
 * </p>
 *
 * @author edwardf
 * @since 2.0.7
 */
public class SaveLinkedResourceClientRequest<T> extends AbstractKinveyJsonClientRequest<T> {

    private UploaderProgressListener upload;

    private MimeTypeFinder mimeTypeFinder;



    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param requestMethod            HTTP Method
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     *                                 the base path from the base URL will be stripped out. The URI template can also be a
     *                                 full URL. URI template expansion is done using
     *                                 {@link com.google.api.client.http.UriTemplate#expand(String, String, Object, boolean)}
     * @param jsonContent              POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass            response class to parse into
     */
    protected SaveLinkedResourceClientRequest(AbstractClient abstractKinveyJsonClient, String requestMethod, String uriTemplate, GenericJson jsonContent, Class<T> responseClass) {

        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, responseClass);

    }

    public void setMimeTypeFinder(MimeTypeFinder finder){
        this.mimeTypeFinder = finder;
    }

    @Override
    public T execute() throws IOException {
        //TODO edwardf possible optimization-- if file hasn't changed, don't bother uploading it...? not sure if possible

        if (getJsonContent() instanceof LinkedGenericJson) {


        	Logger.INFO("Kinvey - LR, " + "linked resource found, file count at: " + ((LinkedGenericJson) getJsonContent()).getAllFiles().keySet().size());


            for (final String key : ((LinkedGenericJson) getJsonContent()).getAllFiles().keySet()) {
                if (((LinkedGenericJson) getJsonContent()).getFile(key) != null) {

                	Logger.INFO("Kinvey - LR, " + "found a LinkedGenericJson: " + key);// + " -> " + ((LinkedGenericJson) getJsonContent()).getFile(key).getId());
                    if (((LinkedGenericJson) getJsonContent()).getFile(key).isResolve()) {

                        InputStream in = ((LinkedGenericJson) getJsonContent()).getFile(key).getInput();

                        InputStreamContent mediaContent = null;
                        String mimetype = "application/octet-stream";
                        if (in != null) {
                            mediaContent = new InputStreamContent(mimetype, in);
                            mediaContent.setCloseInputStream(false);
                            mediaContent.setRetrySupported(false);
                        }


                        MetaUploadListener metaUploadListener = new MetaUploadListener() {
                            @Override
                            public void metaDataUploaded(FileMetaData metaData) {

                            }

                            @Override
                            public void progressChanged(MediaHttpUploader uploader) throws IOException {

                            }

                            @Override
                            public void onSuccess(FileMetaData result) {

                            }

                            @Override
                            public void onFailure(Throwable error) {

                            }
                        };

                        FileStore fileStore = getAbstractKinveyClient().getFileStore();

                        LinkedFile lf = ((LinkedGenericJson) getJsonContent()).getFile(key);
                        FileMetaData meta = new FileMetaData(lf.getId());
                        meta.setFileName(lf.getFileName());

                        if (lf.hasExtras()){
                            for (String k : lf.getExtras().keySet()){
                                meta.put(k, lf.getExtra(k));
                            }
                        }
                        mimeTypeFinder.getMimeType(meta, in);

                        FileMetaData file = fileStore.upload(mediaContent.getInputStream(), meta, new UploaderProgressListener() {
                            @Override
                            public void progressChanged(MediaHttpUploader uploader) throws IOException {
                                if (upload != null) {
                                    upload.progressChanged(uploader);
                                }
                            }

                            @Override
                            public void onSuccess(FileMetaData result) {
                                if (upload != null) {
                                    upload.onSuccess(result);
                                }
                            }

                            @Override
                            public void onFailure(Throwable error) {
                                if (upload != null) {
                                    upload.onFailure(error);
                                }
                            }
                        });

                    }
                }
            }



            Logger.INFO("Kinvey - LR, " + "saving the entity!");
            return super.execute();
        } else {
            return super.execute();
        }


    }

    public UploaderProgressListener getUpload() {
        return upload;
    }

    public void setUpload(UploaderProgressListener upload) {
        this.upload = upload;
    }

    public static interface MetaUploadListener extends UploaderProgressListener{
        /**
         * Called to notify that metadata has been successfully uploaded to the /blob/
         * <p/>
         * <p>
         * This method is called once, before the file upload actually begins but after the metadata has been set in the
         * blob collection.  This metadata is used by the NetworkFileManager API to determine the upload URL, and contains the id of the file.
         * </p>
         *
         * @param metaData - The NetworkFileManager MetaData associated with the upload about to occur.
         */
        public void metaDataUploaded(FileMetaData metaData);
    }
}