package com.kinvey.sample.contentviewr;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class Contentviewr extends SherlockFragmentActivity{

    public static final String TAG = "contentviewr";



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contentviewr);
    }
}
