package com.kinvey.sample.contentviewr;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;

import android.os.Parcelable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.offline.SqlLiteOfflineStore;
import com.kinvey.java.User;
import com.kinvey.java.offline.OfflinePolicy;
import com.kinvey.sample.contentviewr.model.ContentType;
import com.kinvey.sample.contentviewr.model.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Contentviewr extends SherlockFragmentActivity{

    public static final String TAG = "contentviewr";
    public static final String MARKET_COLLECTION = "Markets";
    public static final String TYPE_COLLECTION = "ContentTypes";
    public static final String CONTENT_COLLECTION = "Content";
    private static final int PRELOAD_COUNT = 2; //this is used as semaphore, should match number of collections to preload

    private String selectedTarget;

    private long lastBackPressAt = 0;

    private int preLoadSemaphore;

    private List<Target> targets;
    private HashMap<String, ContentType> contentTypes;

    private LinearLayout loading;
    private ListView drawer;
    private DrawerLayout drawerLayout;
    private DrawerAdapter adapter;
    private ActionBarDrawerToggle drawerToggle;
    private Typeface roboto;

    private ContentTypePager pager;

    //these two are for horizontal
    private FrameLayout listbox;
    private FrameLayout viewbox;
    //this one is for portrait
    private FrameLayout fullContent;


    public Client getClient(){
        return ((ContentViewrApplication)getApplication()).getClient();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contentviewr);
        Log.i(TAG, "contentviewr got oncreate");

        if (savedInstanceState != null){


            String[] contentKeys = savedInstanceState.getStringArray("contentKeys"); // contentTypes.keySet().toArray(new String[contentTypes.keySet().size()]);
            Parcelable[] parcecontents = savedInstanceState.getParcelableArray("contentValues");// contentTypes.values().toArray(new String[contentTypes.values().size()]);

            ContentType[] contentValues = Arrays.copyOf(parcecontents, parcecontents.length, ContentType[].class);


            contentTypes = new HashMap<String, ContentType>();

            for (int i = 0; i < contentValues.length; i++){

                contentTypes.put(contentKeys[i], contentValues[i]);
            }

            //saved.putStringArray("contentKeys", contentKeys);
            //saved.putStringArray("contentValues", contentValues);

        }

        fullContent = (FrameLayout) findViewById(R.id.content_full);
        listbox = (FrameLayout) findViewById(R.id.content_left);
        viewbox = (FrameLayout) findViewById(R.id.content_right);

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
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);


        ((ContentViewrApplication)getApplication()).loadClient(new KinveyUserCallback() {
            @Override
            public void onSuccess(User result) {
                preload();
            }

            @Override
            public void onFailure(Throwable error) {
                showLogin();
            }
        });
        getClient().enableDebugLogging();

        if (!getClient().user().isUserLoggedIn()){
            showLogin();
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "contentviewr got onresume");
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    protected void onSaveInstanceState(Bundle saved) {
        super.onSaveInstanceState(saved);
        if (contentTypes == null){
            return;
        }
        String[] contentKeys = contentTypes.keySet().toArray(new String[contentTypes.keySet().size()]);
        ContentType[] contentValues = contentTypes.values().toArray(new ContentType[contentTypes.values().size()]);

        saved.putStringArray("contentKeys", contentKeys);
        saved.putParcelableArray("contentValues", contentValues);
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

        //if (!getClient().push().isPushEnabled()){
            getClient().push().initialize(getApplication());
        //}
        preLoadSemaphore = PRELOAD_COUNT;

        AsyncAppData<Target> targetAppData = getClient().appData(MARKET_COLLECTION, Target.class);
        targetAppData.setOffline(OfflinePolicy.LOCAL_FIRST, new SqlLiteOfflineStore(getApplication()));

        targetAppData.get(new KinveyListCallback<Target>() {
            @Override
            public void onSuccess(Target[] result) {
                targets = Arrays.asList(result);
                selectedTarget = getTargetList().get(0);
                getSupportActionBar().setTitle(selectedTarget);


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

        AsyncAppData<ContentType> contentAppData = getClient().appData(TYPE_COLLECTION, ContentType.class);
        contentAppData.setOffline(OfflinePolicy.LOCAL_FIRST, new SqlLiteOfflineStore(getApplication()));
        contentAppData.get(new KinveyListCallback<ContentType>() {
            @Override
            public void onSuccess(ContentType[] result) {
                contentTypes = new HashMap<String, ContentType>();
                long id = 0;
                for (ContentType c : result){
                    c.setUniqueID(id++);
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
                        getClient().user().update(null);
                    }

                }else{
                    getClient().user().put("ordering", Arrays.asList(contentTypes.keySet().toArray(new String[0])));
                    getClient().user().update(null);
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

//    @Override
//    public void onBackPressed() {
//
////        if (fullContent.getVisibility() == View.VISIBLE){
////            fullContent.setVisibility(View.GONE);
////            return;
////        }
//
//        long currentTime = System.currentTimeMillis();
//        if (currentTime - lastBackPressAt > 5000) {
//            Toast.makeText(getBaseContext(), "back again to exit", Toast.LENGTH_LONG).show();
//            lastBackPressAt = currentTime;
//        } else {
//            super.onBackPressed();
//        }
//    }


    private void showPager(){
        if (getBaseContext() == null){
            return;
        }

        loading.setVisibility(View.GONE);

        adapter = new DrawerAdapter(this, getDrawerContents(), (LayoutInflater) getSystemService(
                Activity.LAYOUT_INFLATER_SERVICE));
        drawer.setAdapter(adapter);
        drawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //TODO this is awkward, need a better way-- perhaps delegate onclick event to contentType item in list
                //TODO the problem is how this is tightly coupled with what is displayed, and the order of the items

                if (position == 0){
                    return;
                }

                if (position - 1 < getTargetList().size()){
                    selectedTarget = getTargetList().get(position - 1);
                    pager.refresh();
                    drawerLayout.closeDrawer(drawer);
                    getSupportActionBar().setTitle(selectedTarget);
                }


                if (position == getDrawerContents().size() - 2){
                    //replaceFragment(new LoginFragment(), true);
                    showFull(new LoginFragment());
                    drawerLayout.closeDrawer(drawer);
                }else if (position == getDrawerContents().size() - 1){
                    //replaceFragment(new NotificationFragment(), true);
                    //showWindow(new NotificationFragment());
                    showFull(new NotificationFragment());
                    drawerLayout.closeDrawer(drawer);
                }
            }
        });

        pager = new ContentTypePager();
        //replaceFragment(pager, false);
        showList(pager);


    }

    private void showLogin(){
          getClient().user().login("hello", "hello", new KinveyUserCallback() {
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

    public void replaceFragment(SherlockFragment newOne, int id, String descriptor,  boolean backstack){
        if (getBaseContext() == null){
            return;
        }

        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        tr.replace(id, newOne, descriptor);

        if (backstack){
            tr.addToBackStack(descriptor);
        }
        if (!isFinishing()){
            tr.commitAllowingStateLoss();
        }
    }

    public void addFragment(SherlockFragment newOne, int id, String descriptor,  boolean backstack){
        if (getBaseContext() == null){
            return;
        }

        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        tr.add(id, newOne, descriptor);

        if (backstack){
            tr.addToBackStack(descriptor);
        }
        if (!isFinishing()){
            tr.commitAllowingStateLoss();
        }
    }
    public void showList(SherlockFragment newOne){
        if (listbox != null){
            fullContent.setVisibility(View.GONE);
            replaceFragment(newOne, listbox.getId(), "list", false);

        }else{
            replaceFragment(newOne, fullContent.getId(), "list", false);
        }

    }

    public void showWindow(SherlockFragment newOne){
        if (getBaseContext() == null){
            return;
        }

        if (viewbox != null){
            fullContent.setVisibility(View.GONE);
            replaceFragment(newOne, viewbox.getId(), "window", false);
        }else{
            addFragment(newOne, fullContent.getId(), "window", true);
        }
//        return;
//        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
//
//
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
//            tr.replace(viewbox.getId(), newOne, "content");
//        }else{
//            tr.replace(fullContent.getId(), newOne, "content");
//        }
//
//        tr.addToBackStack("back");
//        if (!isFinishing()){
//            tr.commitAllowingStateLoss();
//        }
    }

    public void showFull(SherlockFragment newOne){
        replaceFragment(newOne, fullContent.getId(), "full", true);
//        return;
//        if (getBaseContext() == null){
//            return;
//        }
//        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
//        tr.replace(R.id.content_full, newOne, "content");
//        tr.addToBackStack("back");
//        if (!isFinishing()){
//            tr.commitAllowingStateLoss();
//
//        }

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

        ContentType markets = new ContentType();
        markets.setDisplayName("Markets");
        markets.setLabel(true);

        drawer.add(markets);

        for (Target t : getTargets()){
            ContentType targ = new ContentType();
            targ.setDisplayName(t.getName());
            targ.setSetting(true);
            drawer.add(targ);
        }

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


