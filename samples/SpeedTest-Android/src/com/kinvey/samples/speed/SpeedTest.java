package com.kinvey.samples.speed;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import java.util.ArrayList;
import java.util.Date;

public class SpeedTest extends Activity {

    private Client client;

    private String twelveEmpty = "test12Empty";
    private String twelveFields = "test12Fields";
    private String twelveFull = "test12Full";

    private String key = "kid_WynS2Fk0E";
    private String secret = "1c1004f225c448649ad17e68607476b1";

    private long buildTime = 0L;
    private long viewTime = 0L;
    private long loginTime = 0L;


    private long[] emptyTimes;


    private long[] fieldsTimes;

    private long[] fullTimes;

    private TextView buildView;
    private TextView viewView;
    private TextView loginView;

    private TextView minEmpty;
    private TextView maxEmpty;
    private TextView avgEmpty;

    private TextView minFields;
    private TextView maxFields;
    private TextView avgFields;

    private TextView minFull;
    private TextView maxFull;
    private TextView avgFull;

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
                setUpAndLaunch();
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

        ((TextView) findViewById(R.id.title_empty)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.title_empty)).setTextSize(12);

        ((TextView) findViewById(R.id.title_fields)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.title_fields)).setTextSize(12);

        ((TextView) findViewById(R.id.title_full)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.title_full)).setTextSize(12);

        ((TextView) findViewById(R.id.title_login)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.title_login)).setTextSize(12);

        ((TextView) findViewById(R.id.title_views)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.title_views)).setTextSize(12);

        ((TextView) findViewById(R.id.title_build)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.title_build)).setTextSize(12);

        ((TextView) findViewById(R.id.empty_min_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.empty_min_label)).setTextSize(12);
        ((TextView) findViewById(R.id.empty_max_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.empty_max_label)).setTextSize(12);
        ((TextView) findViewById(R.id.empty_avg_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.empty_avg_label)).setTextSize(12);

        ((TextView) findViewById(R.id.fields_min_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.fields_min_label)).setTextSize(12);
        ((TextView) findViewById(R.id.fields_max_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.fields_max_label)).setTextSize(12);
        ((TextView) findViewById(R.id.fields_avg_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.fields_avg_label)).setTextSize(12);

        ((TextView) findViewById(R.id.full_min_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.full_min_label)).setTextSize(12);
        ((TextView) findViewById(R.id.full_max_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.full_max_label)).setTextSize(12);
        ((TextView) findViewById(R.id.full_avg_label)).setTypeface(robotoThin);
        ((TextView) findViewById(R.id.full_avg_label)).setTextSize(12);


        editRequest = (EditText) findViewById(R.id.value_requestcount);
        editRequest.setTypeface(robotoThin);
        editRequest.setTextSize(12);

        editEntity = (EditText) findViewById(R.id.value_entitycount);
        editEntity.setTypeface(robotoThin);
        editEntity.setTextSize(12);

        viewView = (TextView) findViewById(R.id.value_views);
        viewView.setTypeface(robotoThin);
        viewView.setTextSize(12);

        loginView = (TextView) findViewById(R.id.value_login);
        loginView.setTypeface(robotoThin);
        loginView.setTextSize(12);

        buildView = (TextView) findViewById(R.id.value_build);
        buildView.setTypeface(robotoThin);
        buildView.setTextSize(12);

        minEmpty = (TextView) findViewById(R.id.empty_min_value);
        minEmpty.setTypeface(robotoThin);
        minEmpty.setTextSize(12);

        maxEmpty = (TextView) findViewById(R.id.empty_max_value);
        maxEmpty.setTypeface(robotoThin);
        maxEmpty.setTextSize(12);

        avgEmpty = (TextView) findViewById(R.id.empty_avg_value);
        avgEmpty.setTypeface(robotoThin);
        avgEmpty.setTextSize(12);

        minFields = (TextView) findViewById(R.id.fields_min_value);
        minFields.setTypeface(robotoThin);
        minFields.setTextSize(12);

        maxFields = (TextView) findViewById(R.id.fields_max_value);
        maxFields.setTypeface(robotoThin);
        maxFields.setTextSize(12);

        avgFields = (TextView) findViewById(R.id.fields_avg_value);
        avgFields.setTypeface(robotoThin);
        avgFields.setTextSize(12);

        minFull = (TextView) findViewById(R.id.full_min_value);
        minFull.setTypeface(robotoThin);
        minFull.setTextSize(12);

        maxFull = (TextView) findViewById(R.id.full_max_value);
        maxFull.setTypeface(robotoThin);
        maxFull.setTextSize(12);

        avgFull = (TextView) findViewById(R.id.full_avg_value);
        avgFull.setTypeface(robotoThin);
        avgFull.setTextSize(12);

        long viewEnd = new Date().getTime();
        viewTime = viewEnd - viewStart;

        long buildStart = new Date().getTime();
        client = new Client.Builder(key, secret, this.getApplicationContext()).build();
        long buildEnd = new Date().getTime();
        buildTime = buildEnd - buildStart;
        updateViews();

        client.user().logout().execute();


        final long loginStart = new Date().getTime();


        client.user().login("hello", "hello", new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                long loginEnd = new Date().getTime();
                loginTime = loginEnd - loginStart;
                updateViews();
                setUpAndLaunch();

            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(SpeedTest.this, "uh oh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });

    }

    private void setUpAndLaunch(){

        requestCount = Integer.valueOf(editRequest.getText().toString());
        entityCount = Integer.valueOf(editEntity.getText().toString());

        emptyTimes = null;
        updateViews();

        emptyTimes = new long[requestCount];
        fullTimes = new long[requestCount];
        fieldsTimes = new long[requestCount];

        loadAll();

    }

    private void loadAll(){

        semaphore = requestCount;
        for (int i = 0; i < requestCount; i++) {
            final int x = i;

            emptyTimes[x] = new Date().getTime();

            client.appData(twelveEmpty, GenericJson.class).get(new KinveyListCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson[] result) {
                    long emptyEnd = new Date().getTime();
                    emptyTimes[x] = emptyEnd - emptyTimes[x];
                    semaphore--;
                    if (semaphore == 0){
                        updateViews();
                        loadFields();
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

    public void loadFull(){

        semaphore = requestCount;
        for (int i = 0; i < requestCount; i++) {
            final int x = i;

            fullTimes[x] = new Date().getTime();

            client.appData(twelveFull, GenericJson.class).get(new KinveyListCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson[] result) {
                    long fullEnd = new Date().getTime();
                    fullTimes[x] = fullEnd - fullTimes[x];
                    semaphore--;
                    if (semaphore == 0){
                        updateViews();
                        done();
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

    public void loadFields(){

        semaphore = requestCount;
        for (int i = 0; i < requestCount; i++) {
            final int x = i;

            fieldsTimes[x] = new Date().getTime();

            client.appData(twelveFields, GenericJson.class).get(new KinveyListCallback<GenericJson>() {
                @Override
                public void onSuccess(GenericJson[] result) {
                    long fieldsEnd = new Date().getTime();
                    fieldsTimes[x] = fieldsEnd - fieldsTimes[x];
                    semaphore--;
                    if (semaphore == 0){
                        updateViews();
                        loadFull();
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

    private void done(){}

    private void updateViews(){

        buildView.setText("" + buildTime);
        viewView.setText("" + viewTime);
        loginView.setText("" + loginTime);

        if (emptyTimes == null){
            minEmpty.setText("--");
            maxEmpty.setText("--");
            avgEmpty.setText("--");
            minFields.setText("--");
            maxFields.setText("--");
            avgFields.setText("--");
            minFull.setText("--");
            maxFull.setText("--");
            avgFull.setText("--");
            return;
        }


        long min = 10000000L;
        long max = 0L;
        long sum = 0L;

        for (long f : emptyTimes) {
            sum += f;
            if (f > max) {
                max = f;
            }
            if (f < min) {
                min = f;
            }
        }

        if (sum != 0) {
            minEmpty.setText("" + min);
            maxEmpty.setText("" + max);
            avgEmpty.setText("" + (sum / requestCount));
        }

        min = 10000000L;
        max = 0L;
        sum = 0L;

        for (long f : fieldsTimes) {
            sum += f;
            if (f > max) {
                max = f;
            }
            if (f < min) {
                min = f;
            }
        }
        if (sum != 0) {
            minFields.setText("" + min);
            maxFields.setText("" + max);
            avgFields.setText("" + (sum / requestCount));
        }



        min = 10000000L;
        max = 0L;
        sum = 0L;

        for (long f : fullTimes) {
            sum += f;
            if (f > max) {
                max = f;
            }
            if (f < min) {
                min = f;
            }
        }

        if (sum != 0) {
            minFull.setText("" + min);
            maxFull.setText("" + max);
            avgFull.setText("" + (sum / requestCount));
        }




    }


}
