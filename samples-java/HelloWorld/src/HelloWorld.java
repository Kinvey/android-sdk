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
import java.lang.reflect.GenericSignatureFormatError;
import java.util.ArrayList;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.ObjectParser;
import com.google.gson.Gson;
import com.kinvey.java.Logger;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.java.model.Aggregation;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.java.query.AbstractQuery.SortOrder;
import com.kinvey.nativejava.AppData;
import com.kinvey.nativejava.Client;
import com.kinvey.nativejava.JavaJson;

/**
 * @author edwardf
 */
public class HelloWorld {

    public static final String appKey = "kid_WJxJbK9kC";
    public static final String appSecret = "f16df5c4be864d34aff7d1cc962f20b9";

//    public static final String appKey = "kid_-J7AmdoNC";
//    public static final String appSecret = "b064f25bc52a4ceca8bdb7537dc8aa6e";

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


//        HttpRequest httpRequest = new NetHttpTransport().createRequestFactory().buildRequest("GET", new GenericUrl(), );

//        httpRequest.setParser(new JsonObjectParser(JavaJson.newCompatibleJsonFactory(JavaJson.JSONPARSER.GSON)));

//        httpRequest.setSuppressUserAgentSuffix(true);

        GenericJson unicode = new GenericJson();
        unicode.put("encode", "Ã");
        GenericJson escaped = new GenericJson();
        escaped.put("encode", "\u00AD");


        ObjectParser parser =  new JsonObjectParser(JavaJson.newCompatibleJsonFactory(JavaJson.JSONPARSER.GSON));

//        parser.

//        try {
//            System.out.println("encode");
//            System.out.println(new Gson().toJson(unicode));
//            System.out.println("Ã".getBytes("UTF-8").length);
//            System.out.println("Ã".getBytes("unicode").length);


//            System.out.println(new String("Ã".getBytes("UTF-8"), "UTF-8"));
//            System.out.println(new String("Ã".getBytes("unicode"), "unicode"));
//            System.out.println(new String("Ã".getBytes("UTF-8"), "UTF-8").getBytes("UTF-8").length);
//            System.out.println(new String(new String("Ã".getBytes("UTF-8"), "UTF-8").getBytes("UTF-8"), "UTF-8"));
//            System.out.println(new String(new String("Ã".getBytes("UTF-8"), "UTF-8").getBytes("UTF-8"), "UTF-8").getBytes("UTF-8").length);
//            System.out.println(new String(new String(new String("Ã".getBytes("UTF-8"), "unicode").getBytes("UTF-8"), "unicode").getBytes("UTF-8"), "unicode"));

//
//            System.out.println("escaped");
//            System.out.println(new Gson().toJson(escaped));
//            System.out.println("\u00AD".getBytes("UTF-8").length);
//            System.out.println("\u00AD".getBytes("unicode").length);
//            System.out.println(new String("\u00AD".getBytes("UTF-8"), "UTF-8"));
//            System.out.println(new String("\u00AD".getBytes("unicode"), "unicode"));

//            private static final Charset UTF_8 = Charset.forName("UTF-8");
//            private String forceUtf8Coding(String input) {return new String(input.getBytes(UTF_8), UTF_8))}

//            System.out.println("done");
//        }catch (Exception e){}



        try{
//

        AppData<GenericJson> appData = myJavaClient.appData("EncodingTest", GenericJson.class);
        GenericJson entity = new GenericJson();
        entity.put("encoding", "Ã");
        String entityId = (String) appData.saveBlocking(entity).execute().get("_id");


        for (int i = 0; i < 10; i++) {
            entity = appData.getBlocking(new Query().equals("_id", entityId)).execute()[0];
            System.out.println("Found:" + entity);
            GenericJson saved = appData.saveBlocking(entity).execute();
            System.out.println("Saved:" + saved);
        }
        }catch (Exception e){
            e.printStackTrace();

        }
//        GenericJson first = new GenericJson();
//        first.put("_id", "1");
//        first.put("encode", "=");
//        GenericJson second = new GenericJson();
//        second.put("_id", "2");
//        second.put("encode", "\u00AD");
//
//        try {
//            myJavaClient.appData("encoder", GenericJson.class).saveBlocking(first).execute();
//            myJavaClient.appData("encoder", GenericJson.class).saveBlocking(second).execute();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        GenericJson firstUp = new GenericJson();
//        GenericJson secondUp = new GenericJson();
//        try {
//            firstUp = myJavaClient.appData("encoder", GenericJson.class).getEntityBlocking("1").execute();
//            secondUp = myJavaClient.appData("encoder", GenericJson.class).getEntityBlocking("2").execute();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        System.out.println("first: " + firstUp.get("encode"));
//        System.out.println("second: " + secondUp.get("encode"));


//        AppData<GenericJson[]> ok = myJavaClient.appData("00CCUZones", GenericJson[].class);
//        ArrayList<String> fields = new ArrayList<String>();
//        fields.add("room_id");
//        try {
//            GenericJson[] e = ok.countBlocking(fields, null).execute();
//        }catch (Exception e){
//            System.out.println("Couldn't count! -> " + e);
//            e.printStackTrace();
//        }

//
//
//        HelloEntity test = new HelloEntity();
//        test.setSomedata("hello");
//        String id = "";
//        try{
//            HelloEntity saved = myJavaClient.appData("native", HelloEntity.class).saveBlocking(test).execute();
//            System.out.println("Client appdata saved -> " + saved.getId());
//            id = saved.getId();
//        }catch (IOException e){
//            System.out.println("Couldn't save! -> " + e);
//            e.printStackTrace();
//        }
//
//        try{
//            HelloEntity loaded = myJavaClient.appData("native", HelloEntity.class).getEntityBlocking(id).execute();
//            System.out.println("Client appdata loaded by id -> " + loaded.getId());
//        }catch (IOException e){
//            System.out.println("Couldn't load! -> " + e);
//            e.printStackTrace();
//        }
//
//        try{
//            HelloEntity[] loaded = myJavaClient.appData("native", HelloEntity.class).getBlocking(new String[]{id}).execute();
//            System.out.println("Client appdata loaded by query -> " + loaded.length);
//        }catch (IOException e){
//            System.out.println("Couldn't load! -> " + e);
//            e.printStackTrace();
//        }
//
//        try{
//
//        	FileMetaData[] metas = myJavaClient.file().prepDownloadBlocking(new Query()).execute();
//        	FileMetaData[] metaSort = myJavaClient.file().prepDownloadBlocking(new Query().addSort("_id", SortOrder.ASC)).execute();
//        	FileMetaData[] metaLimit = myJavaClient.file().prepDownloadBlocking(new Query().setLimit(10)).execute();
//
////        	System.out.println("plain query count -> " + metas.length);
////        	System.out.println("plain query first -> " + metas[0].getId());
////        	System.out.println("sort query count -> " + metaSort.length);
////        	System.out.println("sort query first -> " + metaSort[0].getId());
////        	System.out.println("limit query count -> " + metaLimit.length);
////        	System.out.println("limit query first -> " + metaLimit[0].getId());
//
//        }catch (IOException e){
//            e.printStackTrace();
//
//        }
////
////        try{
////        	KinveyDeleteResponse delete = myJavaClient.file().deleteBlocking(new FileMetaData("myFileId")).execute();
////        }catch(IOException e){
////            System.out.println("Couldn't delete! -> " + e);
////            e.printStackTrace();
////        }
//
//
//        try{
//
//            InputStream is = new FileInputStream("/Users/edward/alpha.apk");
//
//            FileMetaData fm = new FileMetaData();
//            fm.setFileName("alpha.apk");
////            fm.setMimetype("image/png");
//            fm.setPublic(true);
//
//            UploaderProgressListener progressListener = new UploaderProgressListener() {
//                @Override
//                public void progressChanged(MediaHttpUploader uploader) throws IOException {
//                    System.out.println("upload progress change!");
//                }
//
//                @Override
//                public void onSuccess(Void result) {
//                    System.out.println("upload success!");
//                }
//
//                @Override
//                public void onFailure(Throwable error) {
//                    System.out.println("upload failed -> " + error);
//                }
//            };
//            myJavaClient.file().uploadBlocking(fm, is, progressListener);
//
//            System.out.println("uploading Complete!");
//        }catch(IOException e){
//            System.out.println("Couldn't upload! -> " + e);
//            e.printStackTrace();
//        }
//
//        try{
//
//            OutputStream is = new FileOutputStream(new File("/Users/edward/alpha.apk"));
//
//            FileMetaData fm = new FileMetaData();
//            fm.setFileName("2.png");
//            fm.setMimetype("image/png");
//            fm.setPublic(true);
//
//            DownloaderProgressListener progressListener = new DownloaderProgressListener() {
//                @Override
//                public void progressChanged(MediaHttpDownloader uploader) throws IOException {
//                    System.out.println("download progress change!");
//                }
//
//                @Override
//                public void onSuccess(Void result) {
//                    System.out.println("download success!");
//                }
//
//                @Override
//                public void onFailure(Throwable error) {
//                    System.out.println("download failed -> " + error);
//                }
//            };
////            myJavaClient.file().downloadBlocking(new FileMetaData("asd"), is, progressListener);
//            myJavaClient.file().downloadBlocking(new Query().equals("_id", "123"), is, progressListener);
//
//            System.out.println("downloading Complete!");
//        }catch(IOException e){
//            System.out.println("Couldn't upload! -> " + e);
//            e.printStackTrace();
//        }


    }
}
