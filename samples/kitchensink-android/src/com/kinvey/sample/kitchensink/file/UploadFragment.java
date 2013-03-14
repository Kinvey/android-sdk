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
package com.kinvey.sample.kitchensink.file;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.kinvey.java.core.MediaHttpUploader;
import com.kinvey.java.core.UploaderProgressListener;
import com.kinvey.sample.kitchensink.KitchenSink;
import com.kinvey.sample.kitchensink.R;
import com.kinvey.sample.kitchensink.UseCaseFragment;

/**
 * @author m0rganic
 * @since 2.0
 */
public class UploadFragment extends UseCaseFragment implements View.OnClickListener {

    EditText editor;
    Button bUpload;
    TextView tProgress;

    @Override
    public int getViewID() {
        return R.layout.feature_file_upload;
    }

    @Override
    public void bindViews(View v) {
        editor = (EditText) v.findViewById(R.id.file_upload_contents);
        bUpload = (Button) v.findViewById(R.id.file_upload_button);
        bUpload.setOnClickListener(this);
        tProgress = (TextView) v.findViewById(R.id.file_upload_progress);
    }

    @Override
    public void populateViews() {
        new LoadTask().execute(getTarget());
    }

    @Override
    public String getTitle() {
        return "Upload";
    }

    @Override
    public void onClick(View v) {
        if (v == bUpload) {
            tProgress.setText(null);
            new SaveTask(editor.getText().toString(), getTarget()).execute();
            hideKeyboard(v);
        }
    }

    private File getTarget() {
        return(new File(getSherlockActivity().getFilesDir(), FileActivity.FILENAME));
    }

    private void boom(Exception e) {
        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(KitchenSink.TAG, "Exception saving file", e);
    }

    private void save(String text, File target) throws IOException {
        FileOutputStream fos=new FileOutputStream(target);
        OutputStreamWriter out=new OutputStreamWriter(fos);

        out.write(text);
        out.flush();
        fos.getFD().sync();
        out.close();

    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)getSherlockActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editor.getWindowToken(), 0);
    }

    private String load(File target) throws IOException {
        String result="";

        try {
            InputStream in=new FileInputStream(target);

            if (in != null) {
                InputStreamReader tmp=new InputStreamReader(in);
                BufferedReader reader=new BufferedReader(tmp);
                String str;
                StringBuilder buf=new StringBuilder();

                while ((str=reader.readLine()) != null) {
                    buf.append(str + "\n");
                }

                in.close();
                result=buf.toString();
            }
        }
        catch (java.io.FileNotFoundException e) {
            // that's OK, we probably haven't created it yet
        }

        return(result);
    }

    private class LoadTask extends AsyncTask<File, Void, String> {
        private Exception e=null;

        @Override
        protected String doInBackground(File... args) {
            String result="";

            try {
                result=load(args[0]);
            }
            catch (Exception e) {
                this.e=e;
            }

            return(result);
        }

        @Override
        protected void onPostExecute(String text) {
            if (e == null) {
                editor.setText(text);
            }
            else {
                boom(e);
            }
        }
    }

    private class SaveTask extends AsyncTask<Void, Void, Void> {
        private Exception saveError=null;
        private String text;
        private File target;

        SaveTask(String text, File target) {
            this.text=text;
            this.target=target;
        }

        @Override
        protected Void doInBackground(Void... args) {
            try {
                save(text, target);

                //Call the kinvey specific task to perform upload
                getApplicationContext().getClient().file().upload(getTarget(), new UploaderProgressListener() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.i(KitchenSink.TAG, "successfully upload: " + getTarget().length() + " byte file.");
                        Toast.makeText(getSherlockActivity(), "Upload finished.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Throwable error) {
                        Log.e(KitchenSink.TAG, "failed to upload: " + getTarget().length() + " byte file.", error);
                        boom((Exception) error);
                    }

                    @Override
                    public void progressChanged(MediaHttpUploader uploader) throws IOException {
                        Log.i(KitchenSink.TAG, "upload progress: " + uploader.getUploadState());

                        final String state = new String(uploader.getUploadState().toString());

                        getSherlockActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               tProgress.setText((tProgress.getText() == null) ? state
                                       : tProgress.getText() + "\n" + state);
                            }
                        });
                    }
                });
            }
            catch (Exception e) {
                boom(e);
            }

            return (null);
        }

    }
}
