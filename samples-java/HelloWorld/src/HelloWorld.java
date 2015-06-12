/*
 * Copyright (c) 2014, Kinvey, Inc.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.query.AbstractQuery.SortOrder;
import com.kinvey.nativejava.Client;

/**
 * @author edwardf
 */
public class HelloWorld {

    public static final String appKey = "kid_-J7AmdoNC";
    public static final String appSecret = "b064f25bc52a4ceca8bdb7537dc8aa6e";

    public static void main(String[] args){
        System.out.println("Hello World");

        Client myJavaClient = new Client.Builder(appKey, appSecret)
        	//.setBaseUrl("https://v3yk1n-kcs.kinvey.com")
        	.build();
//        Logger.configBuilder().all();
//        myJavaClient.enableDebugLogging();
        boolean ping = false;
        try{
        	ping= myJavaClient.ping();
        }catch(Exception e){}
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
        	
        	FileMetaData[] metas = myJavaClient.file().prepDownloadBlocking(new Query()).execute();
        	FileMetaData[] metaSort = myJavaClient.file().prepDownloadBlocking(new Query().addSort("_id", SortOrder.ASC)).execute();
        	FileMetaData[] metaLimit = myJavaClient.file().prepDownloadBlocking(new Query().setLimit(10)).execute();
        	
        	System.out.println("plain query count -> " + metas.length);
        	System.out.println("plain query first -> " + metas[0].getId());
        	System.out.println("sort query count -> " + metaSort.length);
        	System.out.println("sort query first -> " + metaSort[0].getId());
        	System.out.println("limit query count -> " + metaLimit.length);
        	System.out.println("limit query first -> " + metaLimit[0].getId());
        
        }catch (IOException e){
            e.printStackTrace();
            
        }
//        
//        try{
//        	KinveyDeleteResponse delete = myJavaClient.file().deleteBlocking(new FileMetaData("myFileId")).execute();            
//        }catch(IOException e){
//            System.out.println("Couldn't delete! -> " + e);
//            e.printStackTrace();
//        }


        try{

            InputStream is = new FileInputStream("/Users/edward/1.png");

            FileMetaData fm = new FileMetaData();
            fm.setFileName("1.png");
            fm.setMimetype("image/png");
            fm.setPublic(true);

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

            OutputStream is = new FileOutputStream(new File("/Users/edward/2.png"));

            FileMetaData fm = new FileMetaData();
            fm.setFileName("2.png");
            fm.setMimetype("image/png");
            fm.setPublic(true);

            DownloaderProgressListener progressListener = new DownloaderProgressListener() {
                @Override
                public void progressChanged(MediaHttpDownloader uploader) throws IOException {
                    System.out.println("download progress change!");
                }

                @Override
                public void onSuccess(Void result) {
                    System.out.println("download success!");
                }

                @Override
                public void onFailure(Throwable error) {
                    System.out.println("download failed -> " + error);
                }
            };
//            myJavaClient.file().downloadBlocking(new FileMetaData("asd"), is, progressListener);
            myJavaClient.file().downloadBlocking(new Query().equals("_id", "123"), is, progressListener);

            System.out.println("downloading Complete!");
        }catch(IOException e){
            System.out.println("Couldn't upload! -> " + e);
            e.printStackTrace();
        }


    }
}
