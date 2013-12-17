/*
 * Copyright (c) 2013 Kinvey Inc.
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

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.GenericJson;
import com.kinvey.nativejava.Client;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;

import java.io.*;

/**
 * @author edwardf
 */
public class HelloWorld {

    public static final String appKey = "kid_Pe0tK5Dcyi";
    public static final String appSecret = "060a2f1f723c40bcb7875eaba4438f54";

    public static void main(String[] args){
        System.out.println("Hello World");

        Client myJavaClient = new Client.Builder(appKey, appSecret).setBaseUrl("http://v3yk1n.kinvey.com").build();
        myJavaClient.enableDebugLogging();
        Boolean ping = myJavaClient.ping();
        System.out.println("Client ping -> " + ping);

        try {
            myJavaClient.user().loginBlocking(appKey, appSecret).execute();
            System.out.println("Client login -> " + myJavaClient.user().isUserLoggedIn());
        } catch (IOException e) {
            System.out.println("Couldn't login -> " + e);
            e.printStackTrace();
        }

        HelloEntity test = new HelloEntity();
        test.setSomedata("hello");
        String id = "";
        try{
            HelloEntity saved = myJavaClient.appData("native", HelloEntity.class).saveBlocking(test).execute();
            System.out.println("Client appdata saved -> " + saved.getId());
            id = saved.getId();
        }catch (IOException e){
            System.out.println("Couldn't save! -> " + e);
            e.printStackTrace();
        }

        try{
            HelloEntity loaded = myJavaClient.appData("native", HelloEntity.class).getEntityBlocking(id).execute();
            System.out.println("Client appdata loaded by id -> " + loaded.getId());
        }catch (IOException e){
            System.out.println("Couldn't load! -> " + e);
            e.printStackTrace();
        }

        try{
            HelloEntity[] loaded = myJavaClient.appData("native", HelloEntity.class).getBlocking(new String[]{id}).execute();
            System.out.println("Client appdata loaded by query -> " + loaded.length);
        }catch (IOException e){
            System.out.println("Couldn't load! -> " + e);
            e.printStackTrace();
        }

        try{

            InputStream is = new FileInputStream("/Users/edwardflemingiii/ic_lockscreen_decline_activated.png");

            FileMetaData fm = new FileMetaData();
            fm.setFileName("lockscreen.png");
            fm.setMimetype("image/png");

            UploaderProgressListener progressListener = new UploaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    System.out.println("upload progress change!");
                }

                @Override
                public void onSuccess(Void result) {
                    System.out.println("upload success!");
                }

                @Override
                public void onFailure(Throwable error) {
                    System.out.println("upload failed -> " + error);
                }
            };
            myJavaClient.file().uploadBlocking(fm, is, progressListener);

            System.out.println("uploading Complete!");
        }catch(IOException e){
            System.out.println("Couldn't upload! -> " + e);
            e.printStackTrace();
        }

        try{

            myJavaClient.user().lockDownUserBlocking("52b0c4bca73e1db53e66cb1b", true).execute();
            System.out.println("locked out");
        }catch (Exception e){
            System.out.println("couldnt locked out");
            e.printStackTrace();
        }

    }
}
