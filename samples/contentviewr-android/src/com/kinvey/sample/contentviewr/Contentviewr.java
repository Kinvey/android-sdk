package com.kinvey.sample.contentviewr;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.AsyncUser;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.offline.SqlLiteOfflineStore;
import com.kinvey.java.AppData;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.offline.OfflinePolicy;
import com.kinvey.sample.contentviewr.dslv.DragSortController;
import com.kinvey.sample.contentviewr.dslv.DragSortListView;
import com.kinvey.sample.contentviewr.model.ContentType;
import com.kinvey.sample.contentviewr.model.ContentUser;
import com.kinvey.sample.contentviewr.model.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Contentviewr extends SherlockFragmentActivity{

    public static final String TAG = "contentviewr";
    public static final String TARGET_COLLECTION = "Targets";
    public static final String TYPE_COLLECTION = "ContentTypes";
    public static final String CONTENT_COLLECTION = "Content";
    private static final int PRELOAD_COUNT = 2; //this is used as semaphore, should match number of collections to preload

    private String selectedTarget;

    private Client client;
    private int preLoadSemaphore;

    private List<Target> targets;
//    private List<ContentType> contentTypes;
    private HashMap<String, ContentType> contentTypes;

    private LinearLayout loading;
    private ListView drawer;
    private DrawerLayout drawerLayout;
    private DrawerAdapter adapter;
    private ActionBarDrawerToggle drawerToggle;
    private Typeface roboto;


    public Client getClient(){
        if (client == null){
            client = new Client.Builder(getApplicationContext()).build();
        }
        return client;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contentviewr);

        roboto = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
        drawer = (ListView) findViewById(R.id.left_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        loading = (LinearLayout) findViewById(R.id.content_loadingbox);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);


        getActionBar().setDisplayShowTitleEnabled(false);

        client = new Client.Builder(getApplicationContext()).setRetrieveUserCallback(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                preload();
            }

            @Override
            public void onFailure(Throwable error) {
                showLogin();
            }
        }).setUserClass(ContentUser.class).build();
        client.enableDebugLogging();

        if (!client.user().isUserLoggedIn()){
            showLogin();
        }
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            if (drawerLayout.isDrawerOpen(drawer)) {
                drawerLayout.closeDrawer(drawer);
            } else {
                drawerLayout.openDrawer(drawer);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private void preload(){
        preLoadSemaphore = PRELOAD_COUNT;

        AsyncAppData<Target> targetAppData = client.appData(TARGET_COLLECTION, Target.class);
        //targetAppData.setOffline(OfflinePolicy.LOCAL_FIRST, new SqlLiteOfflineStore(getApplication()));

        targetAppData.get(new KinveyListCallback<Target>() {
            @Override
            public void onSuccess(Target[] result) {
                targets = Arrays.asList(result);
                preLoadSemaphore = preLoadSemaphore - 1;
                if (preLoadSemaphore == 0) {
                    showPager();
                }

            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(Contentviewr.this, "Something went wrong -> " + error, Toast.LENGTH_SHORT);
                Log.e(TAG, "something went wrong -> " + error);
                error.printStackTrace();
            }
        });

        AsyncAppData<ContentType> contentAppData = client.appData(TYPE_COLLECTION, ContentType.class);
        //contentAppData.setOffline(OfflinePolicy.LOCAL_FIRST, new SqlLiteOfflineStore(getApplication()));
        contentAppData.get(new KinveyListCallback<ContentType>() {
            @Override
            public void onSuccess(ContentType[] result) {
                contentTypes = new HashMap<String, ContentType>();
                for (ContentType c : result){
                    contentTypes.put(c.getName(), c);
                }

                boolean needsUpdate = false;
                if (getClient().user().containsKey("ordering")){
                    for (String s : (List<String>) getClient().user().get("ordering")){
                        if (!contentTypes.keySet().contains(s)){
                            ((List<String>) getClient().user().get("ordering")).remove(s);
                            needsUpdate = true;
                        }
                    }

                    for (String s : contentTypes.keySet()){
                        if (!((List<String>) getClient().user().get("ordering")).contains(s)){
                            ((List<String>) getClient().user().get("ordering")).add(s);
                            needsUpdate = true;

                        }

                    }

                    if (needsUpdate){
                        client.user().update(null);
                    }

                }else{
                    getClient().user().put("ordering", Arrays.asList(contentTypes.keySet().toArray(new String[0])));
                    client.user().update(null);
                }

                preLoadSemaphore = preLoadSemaphore - 1;
                if (preLoadSemaphore == 0) {
                    showPager();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                Toast.makeText(Contentviewr.this, "Something went wrong -> " + error, Toast.LENGTH_SHORT);
                Log.e(TAG, "something went wrong -> " + error);
                error.printStackTrace();
            }
        });
    }


    private void showPager(){

        loading.setVisibility(View.GONE);

        adapter = new DrawerAdapter(this, getDrawerContents(), (LayoutInflater) getSystemService(
                Activity.LAYOUT_INFLATER_SERVICE));
        drawer.setAdapter(adapter);

        ArrayAdapter<String> listnav = new ArrayAdapter<String>(getSupportActionBar().getThemedContext(), R.layout.sherlock_spinner_dropdown_item, getTargetList());
        listnav.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);


        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(listnav, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                selectedTarget = getTargetList().get(itemPosition);
                return false;
            }
        });

        replaceFragment(new ContentTypePager(), false);


    }

    private void showLogin(){
          client.user().login("hello", "hello", new KinveyUserCallback() {
              @Override
              public void onSuccess(User result) {
                  preload();
              }

              @Override
              public void onFailure(Throwable error) {
                  Util.Error(Contentviewr.this, error);
              }
          });


    }

    public void replaceFragment(SherlockFragment newOne, boolean backstack){

        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        tr.replace(R.id.content_frame, newOne, "content");
        if (backstack){
            tr.addToBackStack("back");
        }
        tr.commitAllowingStateLoss();



    }

    public List<Target> getTargets(){
        return this.targets;
    }

    public HashMap<String, ContentType> getContentTypes(){
        return this.contentTypes;
    }

    public List<ContentType> getDrawerContents(){
        List<ContentType> drawer = new ArrayList<ContentType>();


        ContentType settings = new ContentType();
        settings.setDisplayName("Settings");
        settings.setLabel(true);

        ContentType account = new ContentType();
        account.setDisplayName("Account");
        account.setSetting(true);

        ContentType notifications = new ContentType();
        notifications.setDisplayName("Notifications");
        notifications.setSetting(true);

        drawer.add(settings);
        drawer.add(account);
        drawer.add(notifications);

        return drawer;


    }



    public List<String> getTargetList(){
        List<String> ret = new ArrayList<String>();
        for (Target t : getTargets()){
            ret.add(t.getName());
        }
        return ret;
    }


    public String getSelectedTarget() {
        return selectedTarget;
    }


    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {




        }
    };

    public class DrawerAdapter extends ArrayAdapter<ContentType> {

        public static final int TYPE_TOTAL_COUNT = 5;

        private LayoutInflater mInflater;

        public DrawerAdapter(Context context, List<ContentType> objects, LayoutInflater inf) {
            // NOTE: I pass an arbitrary textViewResourceID to the super
            // constructor-- Below I override
            // getView(...), which causes the underlying adapter to ignore this
            // field anyways, it is just needed in the constructor.
            super(context, 0, objects);
            this.mInflater = inf;

        }

        @Override
        public int getViewTypeCount(){
            return 3;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {



            ContentType rowData = getItem(position);


            if(rowData.isLabel()){
                return getLabelView(position, convertView, parent);
            }else if (rowData.isSetting()){
                return getSettingView(position, convertView, parent);
            }else{
                return getContentTypeView(position, convertView, parent);
            }

        }


        public View getSettingView(int position, View convertView, ViewGroup parent){

            TextView name = null;

            ContentType rowData = getItem(position);

            SettingViewHolder holder = null;

            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.row_setting, null);
                holder = new SettingViewHolder(convertView);
                convertView.setTag(holder);
            }

            holder = (SettingViewHolder) convertView.getTag();

            name = holder.getName();

            name.setText(rowData.getDisplayName());

            return convertView;
        }


        public View getLabelView(int position, View convertView, ViewGroup parent){

            TextView name = null;

            ContentType rowData = getItem(position);

            LabelViewHolder holder = null;

            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.row_label, null);
                holder = new LabelViewHolder(convertView);
                convertView.setTag(holder);
            }

            holder = (LabelViewHolder) convertView.getTag();

            name = holder.getName();

            name.setText(rowData.getDisplayName());

            return convertView;
        }

        public View getContentTypeView(int position, View convertView, ViewGroup parent){

            TextView name = null;
            TextView subtext = null;

            ContentType rowData = getItem(position);

            ContentTypeViewHolder holder = null;

            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.row_content_type, null);
                holder = new ContentTypeViewHolder(convertView);
                convertView.setTag(holder);
            }

            holder = (ContentTypeViewHolder) convertView.getTag();

            name = holder.getName();
            subtext = holder.getSubtext();

            name.setText(rowData.getDisplayName());
            subtext.setText("");

            return convertView;
        }


        /**
         * This pattern is used as an optimization for Android ListViews.
         *
         * Since every row uses the same layout, the View object itself can be
         * recycled, only the data/content of the row has to be updated.
         *
         * This allows for Android to only inflate enough Row Views to fit on
         * screen, and then they are recycled. This allows us to avoid creating
         * a new view for every single row, which can have a negative effect on
         * performance (especially with large lists on large screen devices).
         *
         */
        private class ContentTypeViewHolder {
            private View row;

            private TextView name = null;
            private TextView subtext = null;
            private ImageView drag = null;

            public ContentTypeViewHolder(View row) {
                this.row = row;
            }

            public TextView getName() {
                if (name == null) {
                    name = (TextView) row.findViewById(R.id.row_type_name);
                    name.setTypeface(roboto);
                }
                return name;
            }

            public TextView getSubtext() {
                if (subtext == null) {
                    subtext = (TextView) row.findViewById(R.id.row_type_details);
                }
                return subtext;
            }

            public ImageView getDrag(){
                if (drag == null){
                    drag = (ImageView) row.findViewById(R.id.drag_handle);
                }
                return drag;
            }
        }

        private class LabelViewHolder{
            private View row;

            private TextView name;

            public LabelViewHolder(View row){
                this.row = row;
            }

            public TextView getName(){
                if (name == null){
                    name = (TextView) row.findViewById(R.id.row_label_name);
                    name.setTypeface(roboto);
                }

                return name;
            }


        }

        private class SettingViewHolder{
            private View row;

            private TextView name;

            public SettingViewHolder(View row){
                this.row = row;
            }

            public TextView getName(){
                if (name == null){
                    name = (TextView) row.findViewById(R.id.row_setting_name);
                    name.setTypeface(roboto);
                }

                return name;
            }


        }



    }

}


