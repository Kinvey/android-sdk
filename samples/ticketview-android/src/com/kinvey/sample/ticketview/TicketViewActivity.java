package com.kinvey.sample.ticketview;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.api.client.http.HttpTransport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
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
            myClient.user().login(new KinveyUserCallback() {
                @Override
                public void onSuccess(User result) {
                    populateList();
                }

                @Override
                public void onFailure(Throwable error) {
                    Log.e(TAG, "Fatal Error", error);
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
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
                }
            });
        }

    }

    public String getTag() {
        return TAG;
    }

    public void getComments(String ticketId, KinveyListCallback<TicketCommentEntity> callback) {
        AsyncAppData<TicketCommentEntity> myAppData = myClient.appData("ticketComment", TicketCommentEntity.class);
        Query myQuery = myAppData.query();
        myQuery.equals("ticketId", ticketId);
        myAppData.get(myQuery, callback);
    }

    public void saveTicket(TicketEntity ticket) {
        myClient.appData("ticket", TicketEntity.class).save(ticket, new KinveyClientCallback<TicketEntity>() {
            @Override
            public void onSuccess(TicketEntity result) {
                Log.i(TAG, "Ticket " + result.getSubject() + " saved");
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Failed to save ticket", error);
            }
        });
    }

    public void saveComment(TicketCommentEntity comment) {
        myClient.appData("ticketComment", TicketCommentEntity.class).save(comment, new KinveyClientCallback<TicketCommentEntity>() {
            @Override
            public void onSuccess(TicketCommentEntity result) {
                Log.i(TAG, "Comment " + result.getCommentId() + " saved");
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Failed to save comment", error);
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

    void showCommentDialog(int position) {
        FragmentManager fm = getSupportFragmentManager();
        TicketCommentDialogFragment commentDialog = new TicketCommentDialogFragment(position);
        commentDialog.setCancelable(true);
        commentDialog.show(fm, "ticket_add_comment_dialog");
    }

    @Override
    public void onNewComment(int position, String comment) {
        TicketEntity newCommentTicket = myList.get(position);
        TicketCommentEntity newComment = new TicketCommentEntity();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'h:m:ss.SZ");
        Date date = new Date();
        newComment.setComment(comment);
        newComment.setTicketId(Integer.parseInt(newCommentTicket.getTicketId()));
        newComment.setCommentDate(dateFormat.format(date));
        newComment.setCommentBy("mikes");
        saveComment(newComment);
    }
}
