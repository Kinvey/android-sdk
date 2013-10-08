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

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.GenericJson;
import com.kinvey.java.*;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;

import java.io.*;
import java.util.ArrayList;

/**
 * @author edwardf
 */
public class HelloWorld {





    public static void main(String[] args){
        System.out.println("Hello World");

        Client myJavaClient = new Client.Builder("kid_ePZ9kJuZMi","3b16c9a8fb8e4b90bf1c71e5b0fe87eb").build();
        Boolean ping = myJavaClient.ping();
        System.out.println("Client ping -> " + ping);

        try {
            myJavaClient.user().loginBlocking("kid_ePZ9kJuZMi","3b16c9a8fb8e4b90bf1c71e5b0fe87eb").execute();
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
//    BufferedReader br  = new BufferedReader(new FileReader("/ic_lockscreen_decline_activated.png"));

            InputStream is = new FileInputStream("/ic_lockscreen_decline_activated.png");

            FileMetaData fm = new FileMetaData();
            fm.setFileName("lockscreen.png");
            fm.setMimetype("image/png");

            fm.setPublic(true);


            InputStreamContent mediaContent = new InputStreamContent("image/png", is);
            mediaContent.setLength(is.available());

            mediaContent.setCloseInputStream(false);
            mediaContent.setRetrySupported(false);


        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
