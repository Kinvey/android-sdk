/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.sample.ticketview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.query.AbstractQuery;
import com.kinvey.sample.ticketview.auth.LoginActivity;
import com.kinvey.sample.ticketview.model.TicketCommentEntity;
import com.kinvey.sample.ticketview.model.TicketEntity;

public class TicketViewActivity extends SherlockFragmentActivity implements TicketCommentDialogFragment.OnNewCommentListener {
    private static final Level LOGGING_LEVEL = Level.FINEST;
    public static final String TAG = "TicketViewApplication";
    private ArrayList<TicketEntity> myList;
    private Client myClient;

    public interface RefreshCallback {
        public void refreshCallback();

    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // run the following comamnd to turn on verbose logging:
        //
        // adb shell setprop log.tag.HttpTransport DEBUG
        //
        Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);


        myClient = ((TicketViewApplication) getApplication()).getClient();
        if (!myClient.user().isUserLoggedIn()) {
            startLoginActivity();
            this.finish();
        } else {
            populateList();
        }
    }

    public ArrayList<TicketEntity> getTicketList() {
        return myList;
    }

    public void populateList() {

        if (myList == null) {
            AsyncAppData<TicketEntity> myAppData = myClient.appData("ticket",TicketEntity.class);
            Query myQuery = myAppData.query();
            myQuery.equals("status", "open");
            myAppData.get(myQuery, new KinveyListCallback<TicketEntity>() {
                @Override
                public void onSuccess(TicketEntity[] result) {
                    myList = new ArrayList(Arrays.asList(result));
                    ticketListFragment();
                }

                @Override
                public void onFailure(Throwable error)  {
                    Log.e(TAG, "Fatal Exception", error);
                    Toast.makeText(TicketViewActivity.this,"Couldn't get ticket list", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void populateList(final RefreshCallback callback) {

        AsyncAppData<TicketEntity> myAppData = myClient.appData("ticket",TicketEntity.class);
        Query myQuery = myAppData.query();
        myQuery.equals("status", "open");
        myAppData.get(myQuery, new KinveyListCallback<TicketEntity>() {
            @Override
            public void onSuccess(TicketEntity[] result) {
                myList.clear();
                myList.addAll(new ArrayList(Arrays.asList(result)));
                callback.refreshCallback();
            }

            @Override
            public void onFailure(Throwable error)  {
                Log.e(TAG, "Fatal Exception", error);
                Toast.makeText(TicketViewActivity.this,"Couldn't get ticket list", Toast.LENGTH_LONG).show();
            }
        });

    }

    public String getTag() {
        return TAG;
    }

    public void getComments(String ticketId, KinveyListCallback<TicketCommentEntity> callback) {
        AsyncAppData<TicketCommentEntity> myAppData = myClient.appData("ticketComment", TicketCommentEntity.class);
        Query myQuery = myAppData.query();
        myQuery.equals("ticketId", ticketId);
        myQuery.addSort("commentDate", AbstractQuery.SortOrder.DESC);
        myAppData.get(myQuery, callback);
    }

    public void saveTicket(TicketEntity ticket) {
        myClient.appData("ticket", TicketEntity.class).save(ticket, new KinveyClientCallback<TicketEntity>() {
            @Override
            public void onSuccess(TicketEntity result) {
                String ticketSaveResult = new StringBuilder()
                        .append("Ticket ")
                        .append(result.getSubject())
                        .append(" saved").toString();
                Log.i(TAG, ticketSaveResult + " saved");
                Toast.makeText(TicketViewActivity.this, ticketSaveResult, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Throwable error) {
                String failure = "Failed to save ticket";
                Log.e(TAG, failure, error);
                Toast.makeText(TicketViewActivity.this, failure, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void saveComment(TicketCommentEntity comment, final RefreshCallback callback) {
        myClient.appData("ticketComment", TicketCommentEntity.class).save(comment, new KinveyClientCallback<TicketCommentEntity>() {
            @Override
            public void onSuccess(TicketCommentEntity result) {
                String commentText = "Comment Saved";
                Log.i(TAG, commentText);
                Toast.makeText(TicketViewActivity.this, commentText, Toast.LENGTH_LONG).show();
                if (callback != null) {
                    callback.refreshCallback();
                }
            }

            @Override
            public void onFailure(Throwable error) {
                String commentText = "Failed to save coment";
                Log.e(TAG, commentText, error);
                Toast.makeText(TicketViewActivity.this, commentText, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    public void ticketListFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, new TicketListFragment());
        ft.addToBackStack(null);
        ft.commit();   
    }

    public void ticketDetailsFragment(int position) {
        TicketEntity detailsTicket = myList.get(position);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, new TicketDetailsFragment(detailsTicket, position));
        ft.addToBackStack(null);
        ft.commit();
    }

    void showCommentDialog(int position, RefreshCallback callback) {
        FragmentManager fm = getSupportFragmentManager();
        TicketCommentDialogFragment commentDialog = new TicketCommentDialogFragment(position, callback);
        commentDialog.setCancelable(true);
        commentDialog.show(fm, "ticket_add_comment_dialog");
    }

    @Override
    public void onNewComment(int position, String comment, RefreshCallback callback) {
        TicketEntity newCommentTicket = myList.get(position);
        TicketCommentEntity newComment = new TicketCommentEntity();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date date = new Date();
        String commentDate = dateFormat.format(date);
        newComment.setComment(comment);
        newComment.setTicketId(Integer.parseInt(newCommentTicket.getTicketId()));
        newComment.setCommentDate(commentDate);
        newComment.setCommentBy(myClient.user().get("name") != null ? myClient.user().get("name").toString() : null);
        saveComment(newComment, callback);
    }

    public String getClientUsername() {
        return myClient.user().getUsername();
    }

    private void logout() {
        myClient.user().logout().execute();
        startLoginActivity();
        this.finish();
    }

    private void startLoginActivity() {
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.ticket, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                logout();
            default: return false;
        }
    }
}
