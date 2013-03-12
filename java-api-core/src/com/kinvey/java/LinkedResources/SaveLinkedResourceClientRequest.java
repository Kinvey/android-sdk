/*
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.java.LinkedResources;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.core.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Implementation of a Client Request, which can uploadBlocking linked resources.
 * <p>
 * On the call to execute, if a file is a LinkedGenericJson, then this iterates through all the attachments and uploads them.
 * Once all files have been uploaded, a call to super.execute() is made.
 * </p>
 * <p>
 * call setUploadProgressListener to getBlocking callbacks for all file uploads.
 * </p>
 *
 * @author edwardf
 * @since 2.0.7
 */
public class SaveLinkedResourceClientRequest<T> extends AbstractKinveyJsonClientRequest<T> {

    private UploaderProgressListener upload;


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
    protected SaveLinkedResourceClientRequest(AbstractKinveyJsonClient abstractKinveyJsonClient, String requestMethod, String uriTemplate, Object jsonContent, Class<T> responseClass) {
        super(abstractKinveyJsonClient, requestMethod, uriTemplate, jsonContent, responseClass);
    }

    @Override
    public T execute() throws IOException {
        //TODO edwardf possible optimization-- if file hasn't changed, don't bother uploading it...? not sure if possible

        if (getJsonContent() instanceof LinkedGenericJson) {


            System.out.println("Kinvey - LR, " + "linked resource found, file countBlocking at: " + ((LinkedGenericJson) getJsonContent()).getAllFiles().keySet().size());


            for (final String key : ((LinkedGenericJson) getJsonContent()).getAllFiles().keySet()) {
                System.out.println("Kinvey - LR, " + "saving a LinkedGenericJson: " + key + " -> " + ((LinkedGenericJson) getJsonContent()).getFile(key).getFileName());

//                byte[] file = ((LinkedGenericJson) getJsonContent()).getFile(key).getFileData();
                InputStream in =  ((LinkedGenericJson) getJsonContent()).getFile(key).getInput();

                InputStreamContent mediaContent = null;
                String mimetype = "application/octet-stream";
                if (in != null) {
                    mediaContent = new InputStreamContent(mimetype, in);
//                    mediaContent.setLength(in.);
                }
                mediaContent.setCloseInputStream(false);
                mediaContent.setRetrySupported(false);

                getAbstractKinveyClient().file().setUploadProgressListener(upload);

                getAbstractKinveyClient().file().uploadBlocking(((LinkedGenericJson) getJsonContent()).getFile(key).getFileName(), mediaContent).execute();


                String filename = ((LinkedGenericJson) getJsonContent()).getFile(key).getFileName();
                //TODO edwardf test various use/edge cases for this MIME type calculation
                //default to a text file if no file extension on file name.
                String mime = "txt";
                if (filename.contains(".")){
                    mime = filename.substring(filename.lastIndexOf('.') + 1);
                }

                HashMap<String,String> resourceMap = new HashMap<String, String>();
                resourceMap.put("_mime-type", mime);
                resourceMap.put("_loc", filename);
                resourceMap.put("_type","resource");
                ((GenericJson) getJsonContent()).put(key, resourceMap);
            }

            System.out.println("Kinvey - LR, " + "saving the entity!");
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
}