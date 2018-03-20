/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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

import com.google.api.client.json.GenericJson;
import com.kinvey.java.AbstractClient;
import com.kinvey.java.Logger;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.store.BaseFileStore;
import com.kinvey.java.store.StoreType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Implementation of a Client Request, which can download linked resources through the NetworkFileManager API as well as the NetworkManager API in one request.
 * <p>
 * On the call to execute, if a file is a LinkedGenericJson, then first it gets the entity.  Then it iterates through all the attachments and downloads them.
 * Once all files have been downloaded, the entity is returned
 * </p>
 * <p>
 * call setDownloadProgressListener to get callbacks for all file downloads.
 * </p>
 *
 * @author edwardf
 * @since 2.0.7
 */
public class GetLinkedResourceClientRequest<T> extends AbstractKinveyJsonClientRequest<T> {

    private DownloaderProgressListener download;


    /**
     * @param abstractKinveyJsonClient kinvey credential JSON client
     * @param uriTemplate              URI template for the path relative to the base URL. If it starts with a "/"
     *                                 the base path from the base URL will be stripped out. The URI template can also be a
     *                                 full URL. URI template expansion is done using
     *                                 {@link com.google.api.client.http.UriTemplate#expand(String, String, Object, boolean)}
     * @param jsonContent              POJO that can be serialized into JSON content or {@code null} for none
     * @param responseClass            response class to parse into
     */
    protected GetLinkedResourceClientRequest(AbstractClient abstractKinveyJsonClient, String uriTemplate, GenericJson jsonContent, Class<T> responseClass) {
        super(abstractKinveyJsonClient, "GET", uriTemplate, jsonContent, responseClass);
    }


    @Override
    public T execute() throws IOException {

        T entity = super.execute();

        if (entity instanceof LinkedGenericJson[]) {
        	Logger.INFO("Kinvey - LR, " + "linked resource array found");
            LinkedGenericJson[] casted = (LinkedGenericJson[]) entity;
            for (LinkedGenericJson ent : casted) {
                downloadResources(ent);
            }
            return entity;


        } else if (entity instanceof LinkedGenericJson) {
        	Logger.INFO("Kinvey - LR, " + "linked resource instance found");
            downloadResources((LinkedGenericJson) entity);
            return entity;

        } else {
        	Logger.INFO("Kinvey - LR, " + "not a linked resource, behaving as usual!");
            return entity;

        }



    }

    private void downloadResources(LinkedGenericJson entity) throws IOException {
    	Logger.INFO("Kinvey - LR, " + "linked resource found, file count at: " + entity.getAllFiles().keySet().size());

        for (String key : (entity).getAllFiles().keySet()) {
            if (entity.get(key) != null) {

            	Logger.INFO("Kinvey - LR, " + "getting a LinkedGenericJson: " + key);//-> " + ((Map) entity.get(key)).get("_loc").toString());

                if (entity.getFile(key) == null) {
                    if (((Map)entity.get(key)).containsKey("_id")){
                        entity.putFile(key, new LinkedFile(((Map) entity.get(key)).get("_id").toString()));
                    }else if (((Map)entity.get(key)).containsKey("_loc")){     //TODO backwards compt for v2 of NetworkFileManager API, this condition can be removed when it's done
                        LinkedFile lf = new LinkedFile();
                        lf.setFileName(((Map) entity.get(key)).get("_loc").toString());
                        entity.putFile(key, lf);
//                        entity.putFile(key, new LinkedFile(((Map) entity.get(key)).get("_loc").toString()));
                    }
                }


                entity.getFile(key).setOutput(new ByteArrayOutputStream());

                BaseFileStore store = getAbstractKinveyClient().getFileStore(StoreType.SYNC);

                FileMetaData meta = new FileMetaData();
                if (((Map) entity.get(key)).containsKey("_id")){
                    meta.setId(((Map) entity.get(key)).get("_id").toString());
                    store.download(meta, entity.getFile(key).getOutput(), null, null, download);

                }/*else if(((Map) entity.get(key)).containsKey("_loc")){
                    meta.setFileName(((Map) entity.get(key)).get("_loc").toString());
                    store.download(((Map) entity.get(key)).get("_loc").toString(),
                            entity.getFile(key).getOutput(), null, download);

                }*/



//                FileMetaData meta = new FileMetaData(((Map) entity.get(key)).get("_id").toString());


            }
        }

    }



    public DownloaderProgressListener getDownloadProgressListener() {
        return download;
    }

    public void setDownloadProgressListener(DownloaderProgressListener download) {
        this.download = download;
    }
}