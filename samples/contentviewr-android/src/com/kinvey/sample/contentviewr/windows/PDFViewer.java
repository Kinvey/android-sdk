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
package com.kinvey.sample.contentviewr.windows;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import com.kinvey.android.offline.SQLiteFileCache;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MetaDownloadProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.sample.contentviewr.Contentviewr;
import com.kinvey.sample.contentviewr.R;
import java.io.*;

/**
 * @author edwardf
 */
public class PDFViewer extends Viewer {

//    WebView pdfView;
    ProgressBar progress;

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Override
    public int getViewID() {
        return R.layout.viewer_pdf;
    }

    @Override
    public void bindViews(View v) {
//        pdfView = (WebView) v.findViewById(R.id.pdfview);
//
//        pdfView.getSettings().setJavaScriptEnabled(true);

        progress = (ProgressBar) v.findViewById(R.id.pdf_progress);
        loadPDF();
//        String pdf = "http://www.adobe.com/devnet/acrobat/pdfs/pdf_open_parameters.pdf";
//        pdfView.loadUrl("http://docs.google.com/gview?embedded=true&url=" + pdf);

//        webview.getSettings().setJavaScriptEnabled(true);
//        webview.getSettings().setPluginsEnabled(true);
//        webview.loadUrl("http://docs.google.com/gview?embedded=true&url=

    }

    @Override
    public String getTitle() {
        return "PDFViewer";
    }






    private void loadPDF(){
        if (content == null){
            Log.i("ok", "content is null");
            return;
        }

        progress.setVisibility(View.VISIBLE);




        SQLiteFileCache cache = new SQLiteFileCache(Contentviewr.cacheLocation);
        String filename = cache.getFilenameForID(getClient(), content.getSource().getReference());
        if (filename != null){
            Log.i("ok", "filename is not null");
//            if (pdfView == null){
//                Log.i("ok", "pdf exists from cache");
//                return;
//            }


            //String uri = getClient().getContext().getCacheDir() + filename;
            File pdf = new File(Contentviewr.cacheLocation,  filename);


//            pdfView.fromFile(pdf).defaultPage(1).enableSwipe(true).load();

            //File  = new File("/sdcard/example.pdf");

            if (pdf.exists()) {
                Log.i("ok", "pdf exists from cache");
                Uri path = Uri.fromFile(pdf);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                progress.setVisibility(View.GONE);
                startActivity(intent);
//
//                try {
//                    startActivity(intent);
//                }
//                catch (ActivityNotFoundException e) {
//                    Toast.makeText(getSherlockActivity(),
//                            "No Application Available to View PDF",
//                            Toast.LENGTH_SHORT).show();
//                }
                return;
            } else{
                Log.i("ok", "pdf doesnt exists from cache");
            }



            //return;
        }


        FileMetaData meta = new FileMetaData(content.getSource().getReference());
        getClient().file().download(meta, out, new MetaDownloadProgressListener() {
            @Override
            public void progressChanged(MediaHttpDownloader downloader) throws IOException {}

            @Override
            public void onSuccess(Void result) {
                if (getSherlockActivity() == null){
                    Log.i("zoomImage", "nulled out");
                    if (progress != null){
                        progress.setVisibility(View.GONE);
                    }

                    return;
                }
                Log.i("pdfview", "set from service");
                SQLiteFileCache cache = new SQLiteFileCache(Contentviewr.cacheLocation);
                byte[] outarray = out.toByteArray();
                if (getMetadata() != null){
                    cache.save(getClient(), getMetadata(), outarray);
                }


                if (getClient() == null){
                    return;
                }
                File pdf = new File(Contentviewr.cacheLocation, getMetadata().getFileName());
//                pdfView.fromFile(pdf).defaultPage(1).enableSwipe(true).load();


                if (pdf.exists()) {
                    Log.i("ok", "pdf exists from api");
                    Uri path = Uri.fromFile(pdf);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(path, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    progress.setVisibility(View.GONE);

                        startActivity(intent);

                }else{
                    Log.e("ok", "pdf doesn't exist");
                }

            }

            @Override
            public void onFailure(Throwable error) {
                Log.i("zoomImage", "failure " + error );
                error.printStackTrace();

            }
        });

    }
}


