package com.kinvey.samples.speed;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.AndroidJson;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import java.util.Date;

public class SpeedTest extends Activity {

    private Client client;

    private String twelveEmpty = "test12Empty";
    private String twelveFields = "test12Fields";
    private String twelveFull = "test12Full";

    private String key = "kid_WynS2Fk0E";
    private String secret = "1c1004f225c448649ad17e68607476b1";

    private long viewTime = 0L;

    private long gson_buildTime = 0L;
    private long gson_loginTime = 0L;

    private long jackson_buildTime = 0L;
    private long jackson_loginTime = 0L;

    private TextView viewView;

    private long[] gson_emptyTimes;
    private long[] gson_fieldsTimes;
    private long[] gson_fullTimes;

    private long[] jackson_emptyTimes;
    private long[] jackson_fieldsTimes;
    private long[] jackson_fullTimes;


    private TextView gson_buildView;
    private TextView gson_loginView;

    private TextView gson_minEmpty;
    private TextView gson_maxEmpty;
    private TextView gson_avgEmpty;

    private TextView gson_minFields;
    private TextView gson_maxFields;
    private TextView gson_avgFields;

    private TextView gson_minFull;
    private TextView gson_maxFull;
    private TextView gson_avgFull;

    private TextView jackson_buildView;
    private TextView jackson_loginView;

    private TextView jackson_minEmpty;
    private TextView jackson_maxEmpty;
    private TextView jackson_avgEmpty;

    private TextView jackson_minFields;
    private TextView jackson_maxFields;
    private TextView jackson_avgFields;

    private TextView jackson_minFull;
    private TextView jackson_maxFull;
    private TextView jackson_avgFull;

    private EditText editRequest;
    private EditText editEntity;

    private static Typeface robotoThin;

    private int semaphore = 0;
    private int requestCount = 12;
    private int entityCount = 12;

    private static final String TAG = "SpeedTester";


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                gson_setUpAndLaunch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long viewStart = new Date().getTime();

        setContentView(R.layout.main);

        robotoThin = Typeface.createFromAsset(this.getAssets(), "Roboto-Thin.ttf");

        ((TextView) findViewById(R.id.title_requestcount)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.title_requestcount)).setTextSize(12);

        ((TextView) findViewById(R.id.title_entitycount)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.title_entitycount)).setTextSize(12);

        ((TextView) findViewById(R.id.title_views)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.title_views)).setTextSize(12);

        ((TextView) findViewById(R.id.gson_title_empty)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_title_empty)).setTextSize(12);

        ((TextView) findViewById(R.id.gson_title_fields)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_title_fields)).setTextSize(12);

        ((TextView) findViewById(R.id.gson_title_full)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_title_full)).setTextSize(12);

        ((TextView) findViewById(R.id.gson_title_login)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_title_login)).setTextSize(12);

        ((TextView) findViewById(R.id.gson_title_build)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_title_build)).setTextSize(12);

        ((TextView) findViewById(R.id.gson_empty_min_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_empty_min_label)).setTextSize(12);
        ((TextView) findViewById(R.id.gson_empty_max_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_empty_max_label)).setTextSize(12);
        ((TextView) findViewById(R.id.gson_empty_avg_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_empty_avg_label)).setTextSize(12);

        ((TextView) findViewById(R.id.gson_fields_min_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_fields_min_label)).setTextSize(12);
        ((TextView) findViewById(R.id.gson_fields_max_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_fields_max_label)).setTextSize(12);
        ((TextView) findViewById(R.id.gson_fields_avg_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_fields_avg_label)).setTextSize(12);

        ((TextView) findViewById(R.id.gson_full_min_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_full_min_label)).setTextSize(12);
        ((TextView) findViewById(R.id.gson_full_max_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_full_max_label)).setTextSize(12);
        ((TextView) findViewById(R.id.gson_full_avg_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.gson_full_avg_label)).setTextSize(12);

        ((TextView) findViewById(R.id.jackson_title_empty)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_title_empty)).setTextSize(12);

        ((TextView) findViewById(R.id.jackson_title_fields)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_title_fields)).setTextSize(12);

        ((TextView) findViewById(R.id.jackson_title_full)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_title_full)).setTextSize(12);

        ((TextView) findViewById(R.id.jackson_title_login)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_title_login)).setTextSize(12);

        ((TextView) findViewById(R.id.jackson_title_build)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_title_build)).setTextSize(12);

        ((TextView) findViewById(R.id.jackson_empty_min_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_empty_min_label)).setTextSize(12);
        ((TextView) findViewById(R.id.jackson_empty_max_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_empty_max_label)).setTextSize(12);
        ((TextView) findViewById(R.id.jackson_empty_avg_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_empty_avg_label)).setTextSize(12);

        ((TextView) findViewById(R.id.jackson_fields_min_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_fields_min_label)).setTextSize(12);
        ((TextView) findViewById(R.id.jackson_fields_max_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_fields_max_label)).setTextSize(12);
        ((TextView) findViewById(R.id.jackson_fields_avg_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_fields_avg_label)).setTextSize(12);

        ((TextView) findViewById(R.id.jackson_full_min_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_full_min_label)).setTextSize(12);
        ((TextView) findViewById(R.id.jackson_full_max_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_full_max_label)).setTextSize(12);
        ((TextView) findViewById(R.id.jackson_full_avg_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.jackson_full_avg_label)).setTextSize(12);




        editRequest = (EditText) findViewById(R.id.value_requestcount);
        editRequest.setTypeface(robotoThin);
        editRequest.setTextSize(12);

        editEntity = (EditText) findViewById(R.id.value_entitycount);
        editEntity.setTypeface(robotoThin);
        editEntity.setTextSize(12);

        viewView = (TextView) findViewById(R.id.value_views);
        viewView.setTypeface(robotoThin);
        viewView.setTextSize(12);

        gson_loginView = (TextView) findViewById(R.id.gson_value_login);
        gson_loginView.setTypeface(robotoThin);
        gson_loginView.setTextSize(12);

        gson_buildView = (TextView) findViewById(R.id.gson_value_build);
        gson_buildView.setTypeface(robotoThin);
        gson_buildView.setTextSize(12);

        gson_minEmpty = (TextView) findViewById(R.id.gson_empty_min_value);
        gson_minEmpty.setTypeface(robotoThin);
        gson_minEmpty.setTextSize(12);

        gson_maxEmpty = (TextView) findViewById(R.id.gson_empty_max_value);
        gson_maxEmpty.setTypeface(robotoThin);
        gson_maxEmpty.setTextSize(12);

        gson_avgEmpty = (TextView) findViewById(R.id.gson_empty_avg_value);
        gson_avgEmpty.setTypeface(robotoThin);
        gson_avgEmpty.setTextSize(12);

        gson_minFields = (TextView) findViewById(R.id.gson_fields_min_value);
        gson_minFields.setTypeface(robotoThin);
        gson_minFields.setTextSize(12);

        gson_maxFields = (TextView) findViewById(R.id.gson_fields_max_value);
        gson_maxFields.setTypeface(robotoThin);
        gson_maxFields.setTextSize(12);

        gson_avgFields = (TextView) findViewById(R.id.gson_fields_avg_value);
        gson_avgFields.setTypeface(robotoThin);
        gson_avgFields.setTextSize(12);

        gson_minFull = (TextView) findViewById(R.id.gson_full_min_value);
        gson_minFull.setTypeface(robotoThin);
        gson_minFull.setTextSize(12);

        gson_maxFull = (TextView) findViewById(R.id.gson_full_max_value);
        gson_maxFull.setTypeface(robotoThin);
        gson_maxFull.setTextSize(12);

        gson_avgFull = (TextView) findViewById(R.id.gson_full_avg_value);
        gson_avgFull.setTypeface(robotoThin);
        gson_avgFull.setTextSize(12);

        jackson_loginView = (TextView) findViewById(R.id.jackson_value_login);
        jackson_loginView.setTypeface(robotoThin);
        jackson_loginView.setTextSize(12);

        jackson_buildView = (TextView) findViewById(R.id.jackson_value_build);
        jackson_buildView.setTypeface(robotoThin);
        jackson_buildView.setTextSize(12);

        jackson_minEmpty = (TextView) findViewById(R.id.jackson_empty_min_value);
        jackson_minEmpty.setTypeface(robotoThin);
        jackson_minEmpty.setTextSize(12);

        jackson_maxEmpty = (TextView) findViewById(R.id.jackson_empty_max_value);
        jackson_maxEmpty.setTypeface(robotoThin);
        jackson_maxEmpty.setTextSize(12);

        jackson_avgEmpty = (TextView) findViewById(R.id.jackson_empty_avg_value);
        jackson_avgEmpty.setTypeface(robotoThin);
        jackson_avgEmpty.setTextSize(12);

        jackson_minFields = (TextView) findViewById(R.id.jackson_fields_min_value);
        jackson_minFields.setTypeface(robotoThin);
        jackson_minFields.setTextSize(12);

        jackson_maxFields = (TextView) findViewById(R.id.jackson_fields_max_value);
        jackson_maxFields.setTypeface(robotoThin);
        jackson_maxFields.setTextSize(12);

        jackson_avgFields = (TextView) findViewById(R.id.jackson_fields_avg_value);
        jackson_avgFields.setTypeface(robotoThin);
        jackson_avgFields.setTextSize(12);

        jackson_minFull = (TextView) findViewById(R.id.jackson_full_min_value);
        jackson_minFull.setTypeface(robotoThin);
        jackson_minFull.setTextSize(12);

        jackson_maxFull = (TextView) findViewById(R.id.jackson_full_max_value);
        jackson_maxFull.setTypeface(robotoThin);
        jackson_maxFull.setTextSize(12);

        jackson_avgFull = (TextView) findViewById(R.id.jackson_full_avg_value);
        jackson_avgFull.setTypeface(robotoThin);
        jackson_avgFull.setTextSize(12);

        long viewEnd = new Date().getTime();
        viewTime = viewEnd - viewStart;

        long buildStart = new Date().getTime();
        client = new Client.Builder(key, secret, this.getApplicationContext()).setJsonFactory(AndroidJson.newCompatibleJsonFactory(AndroidJson.JSONPARSER.GSON)).build();
        long buildEnd = new Date().getTime();
        gson_buildTime = buildEnd - buildStart;
        updateViews();

        client.user().logout().execute();


        final long loginStart = new Date().getTime();


        client.user().login("hello", "hello", new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                long loginEnd = new Date().getTime();
                gson_loginTime = loginEnd - loginStart;
                updateViews();
                gson_setUpAndLaunch();

            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(SpeedTest.this, "uh oh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });

    }

    private void gson_setUpAndLaunch(){

        requestCount = Integer.valueOf(editRequest.getText().toString());
        entityCount = Integer.valueOf(editEntity.getText().toString());

        gson_emptyTimes = null;
        jackson_emptyTimes = null;
        updateViews();

        gson_emptyTimes = new long[requestCount];
        gson_fullTimes = new long[requestCount];
        gson_fieldsTimes = new long[requestCount];

        gson_loadAll();

    }

    private void gson_loadAll(){

        semaphore = requestCount;
        for (int i = 0; i < requestCount; i++) {
            final int x = i;

            gson_emptyTimes[x] = new Date().getTime();

            client.appData(twelveEmpty, GenericJson.class).get(new KinveyListCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson[] result) {
                    long emptyEnd = new Date().getTime();
                    gson_emptyTimes[x] = emptyEnd - gson_emptyTimes[x];
                    semaphore--;
                    if (semaphore == 0){
                        updateViews();
                        gson_loadFields();
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    Toast.makeText(SpeedTest.this, "uh oh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            });

        }




    }

    public void gson_loadFull(){

        semaphore = requestCount;
        for (int i = 0; i < requestCount; i++) {
            final int x = i;

            gson_fullTimes[x] = new Date().getTime();

            client.appData(twelveFull, GenericJson.class).get(new KinveyListCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson[] result) {
                    long fullEnd = new Date().getTime();
                    gson_fullTimes[x] = fullEnd - gson_fullTimes[x];
                    semaphore--;
                    if (semaphore == 0){
                        updateViews();
                        doneWithGSON();
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    Toast.makeText(SpeedTest.this, "uh oh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            });

        }


    }

    public void gson_loadFields(){

        semaphore = requestCount;
        for (int i = 0; i < requestCount; i++) {
            final int x = i;

            gson_fieldsTimes[x] = new Date().getTime();

            client.appData(twelveFields, GenericJson.class).get(new KinveyListCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson[] results) {
                    long fieldsEnd = new Date().getTime();
                    gson_fieldsTimes[x] = fieldsEnd - gson_fieldsTimes[x];
                    semaphore--;
                    if (semaphore == 0){
                        updateViews();
                        gson_loadFull();
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    Toast.makeText(SpeedTest.this, "uh oh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            });

        }


    }

    private void doneWithGSON(){





        long buildStart = new Date().getTime();
        client = new Client.Builder(key, secret, this.getApplicationContext()).setJsonFactory(AndroidJson.newCompatibleJsonFactory(AndroidJson.JSONPARSER.JACKSON)).build();
        long buildEnd = new Date().getTime();
        jackson_buildTime = buildEnd - buildStart;
        updateViews();

        client.user().logout().execute();


        final long loginStart = new Date().getTime();


        client.user().login("hello", "hello", new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                long loginEnd = new Date().getTime();
                jackson_loginTime = loginEnd - loginStart;
                updateViews();
                jackson_setUpAndLaunch();

            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(SpeedTest.this, "uh oh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });

    }

    private void jackson_setUpAndLaunch(){

        requestCount = Integer.valueOf(editRequest.getText().toString());
        entityCount = Integer.valueOf(editEntity.getText().toString());

        jackson_emptyTimes = null;
        updateViews();

        jackson_emptyTimes = new long[requestCount];
        jackson_fullTimes = new long[requestCount];
        jackson_fieldsTimes = new long[requestCount];

        jackson_loadAll();

    }

    private void jackson_loadAll(){

        semaphore = requestCount;
        for (int i = 0; i < requestCount; i++) {
            final int x = i;

            jackson_emptyTimes[x] = new Date().getTime();

            client.appData(twelveEmpty, GenericJson.class).get(new KinveyListCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson[] result) {
                    long emptyEnd = new Date().getTime();
                    jackson_emptyTimes[x] = emptyEnd - jackson_emptyTimes[x];
                    semaphore--;
                    if (semaphore == 0){
                        updateViews();
                        jackson_loadFields();
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    Toast.makeText(SpeedTest.this, "uh oh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            });

        }




    }

    public void jackson_loadFull(){

        semaphore = requestCount;
        for (int i = 0; i < requestCount; i++) {
            final int x = i;

            jackson_fullTimes[x] = new Date().getTime();

            client.appData(twelveFull, GenericJson.class).get(new KinveyListCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson[] result) {
                    long fullEnd = new Date().getTime();
                    jackson_fullTimes[x] = fullEnd - jackson_fullTimes[x];
                    semaphore--;
                    if (semaphore == 0){
                        updateViews();
                        doneWithJackson();
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    Toast.makeText(SpeedTest.this, "uh oh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            });

        }


    }

    public void jackson_loadFields(){

        semaphore = requestCount;
        for (int i = 0; i < requestCount; i++) {
            final int x = i;

            jackson_fieldsTimes[x] = new Date().getTime();

            client.appData(twelveFields, GenericJson.class).get(new KinveyListCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson[] result) {
                    long fieldsEnd = new Date().getTime();
                    jackson_fieldsTimes[x] = fieldsEnd - jackson_fieldsTimes[x];
                    semaphore--;
                    if (semaphore == 0){
                        updateViews();
                        jackson_loadFull();
                    }
                }

                @Override
                public void onFailure(Throwable error) {
                    Toast.makeText(SpeedTest.this, "uh oh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            });

        }


    }

    private void doneWithJackson(){




    }


    private void updateViews(){

        gson_buildView.setText("" + gson_buildTime);
        viewView.setText("" + viewTime);
        gson_loginView.setText("" + gson_loginTime);
        
        jackson_buildView.setText("" + jackson_buildTime);
        jackson_loginView.setText("" + jackson_loginTime);

//        if (jackson_emptyTimes == null){
//            jackson_minEmpty.setText("--");
//            jackson_maxEmpty.setText("--");
//            jackson_avgEmpty.setText("--");
//            jackson_minFields.setText("--");
//            jackson_maxFields.setText("--");
//            jackson_avgFields.setText("--");
//            jackson_minFull.setText("--");
//            jackson_maxFull.setText("--");
//            jackson_avgFull.setText("--");
//        }

        if (gson_emptyTimes == null){
            gson_minEmpty.setText("--");
            gson_maxEmpty.setText("--");
            gson_avgEmpty.setText("--");
            gson_minFields.setText("--");
            gson_maxFields.setText("--");
            gson_avgFields.setText("--");
            gson_minFull.setText("--");
            gson_maxFull.setText("--");
            gson_avgFull.setText("--");
            return;
        }





        long min = 10000000L;
        long max = 0L;
        long sum = 0L;

        for (long f : gson_emptyTimes) {
            sum += f;
            if (f > max) {
                max = f;
            }
            if (f < min) {
                min = f;
            }
        }

        if (sum != 0) {
            gson_minEmpty.setText("" + min);
            gson_maxEmpty.setText("" + max);
            gson_avgEmpty.setText("" + (sum / requestCount));
        }

        min = 10000000L;
        max = 0L;
        sum = 0L;

        for (long f : gson_fieldsTimes) {
            sum += f;
            if (f > max) {
                max = f;
            }
            if (f < min) {
                min = f;
            }
        }
        if (sum != 0) {
            gson_minFields.setText("" + min);
            gson_maxFields.setText("" + max);
            gson_avgFields.setText("" + (sum / requestCount));
        }



        min = 10000000L;
        max = 0L;
        sum = 0L;

        for (long f : gson_fullTimes) {
            sum += f;
            if (f > max) {
                max = f;
            }
            if (f < min) {
                min = f;
            }
        }

        if (sum != 0) {
            gson_minFull.setText("" + min);
            gson_maxFull.setText("" + max);
            gson_avgFull.setText("" + (sum / requestCount));
        }


        min = 10000000L;
        max = 0L;
        sum = 0L;
        
        if (jackson_emptyTimes == null){
        	jackson_minEmpty.setText("--");
        	jackson_maxEmpty.setText("--");
        	jackson_avgEmpty.setText("--");
        	jackson_minFields.setText("--");
        	jackson_maxFields.setText("--");
        	jackson_avgFields.setText("--");
        	jackson_minFull.setText("--");
        	jackson_maxFull.setText("--");
        	jackson_avgFull.setText("--");
            return;
        }
        

        for (long f : jackson_emptyTimes) {
            sum += f;
            if (f > max) {
                max = f;
            }
            if (f < min) {
                min = f;
            }
        }

        if (sum != 0) {
            jackson_minEmpty.setText("" + min);
            jackson_maxEmpty.setText("" + max);
            jackson_avgEmpty.setText("" + (sum / requestCount));
        }

        min = 10000000L;
        max = 0L;
        sum = 0L;

        for (long f : jackson_fieldsTimes) {
            sum += f;
            if (f > max) {
                max = f;
            }
            if (f < min) {
                min = f;
            }
        }
        if (sum != 0) {
            jackson_minFields.setText("" + min);
            jackson_maxFields.setText("" + max);
            jackson_avgFields.setText("" + (sum / requestCount));
        }



        min = 10000000L;
        max = 0L;
        sum = 0L;

        for (long f : jackson_fullTimes) {
            sum += f;
            if (f > max) {
                max = f;
            }
            if (f < min) {
                min = f;
            }
        }

        if (sum != 0) {
            jackson_minFull.setText("" + min);
            jackson_maxFull.setText("" + max);
            jackson_avgFull.setText("" + (sum / requestCount));
        }







    }


}
