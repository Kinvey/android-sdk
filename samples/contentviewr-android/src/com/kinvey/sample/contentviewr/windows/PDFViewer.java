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
package com.kinvey.sample.contentviewr.windows;

import android.util.Log;
import android.view.View;
import com.joanzapata.pdfview.PDFView;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.core.MetaDownloadProgressListener;
import com.kinvey.java.model.FileMetaData;
import com.kinvey.sample.contentviewr.R;
import com.kinvey.sample.contentviewr.file.FileCache;

import java.io.*;

/**
 * @author edwardf
 */
public class PDFViewer extends Viewer {

    PDFView pdfView;

    @Override
    public int getViewID() {
        return R.layout.viewer_pdf;
    }

    @Override
    public void bindViews(View v) {
        pdfView = (com.joanzapata.pdfview.PDFView) v.findViewById(R.id.pdfview);

        loadPDF();

//        File pdf = new File(getClient().getContext().getCacheDir(), "")

//        pdfView.fromFile(").
////                .pages(0, 2, 1, 3, 3, 3)
//                .defaultPage(1)
//                .showMinimap(false)
//                .enableSwipe(true)
////                .onDraw(onDrawListener)
////                .onLoad(onLoadCompleteListener)
////                .onPageChange(onPageChangeListener)
//                .load();


    }

    @Override
    public String getTitle() {
        return "PDFViewer";
    }


    private void loadPDF(){
        if (content == null){
            return;
        }

        //final ByteArrayOutputStream out = new ByteArrayOutputStream();

        FileCache cache = new FileCache();
        String filename = cache.getFilenameForID(getSherlockActivity().getApplicationContext(), content.getSource().getReference());
        if (filename != null){
            if (pdfView == null){
                return;
            }


            //String uri = getClient().getContext().getCacheDir() + filename;
            File pdf = new File(getClient().getContext().getCacheDir(), filename);
            pdfView.fromFile(pdf).defaultPage(1).enableSwipe(true).load();



            return;
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        FileMetaData meta = new FileMetaData(content.getSource().getReference());
        getClient().file().download(meta, out, new MetaDownloadProgressListener() {
            @Override
            public void progressChanged(MediaHttpDownloader downloader) throws IOException {}

            @Override
            public void onSuccess(Void result) {
                if (pdfView == null){
                    Log.i("zoomImage", "nulled out");

                    return;
                }
                Log.i("zoomImage", "set from service");
                FileCache cache = new FileCache();
                byte[] outarray = out.toByteArray();
                if (getMetadata() != null){
                    cache.save(getClient().getContext(), getClient(), getMetadata(), outarray);
                }



                File pdf = new File(getClient().getContext().getCacheDir(), getMetadata().getFileName());
                pdfView.fromFile(pdf).defaultPage(1).enableSwipe(true).load();


            }

            @Override
            public void onFailure(Throwable error) {
                Log.i("zoomImage", "failure " + error );
                error.printStackTrace();

            }
        });

    }
}


