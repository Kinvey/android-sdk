package com.kinvey.sample.ticketview;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.api.client.http.HttpTransport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.AppData;
import com.kinvey.java.Query;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.sample.ticketview.model.Ticket;

public class TicketViewActivity extends SherlockFragmentActivity implements TicketCommentDialogFragment.OnNewCommentListener {
    private static final Level LOGGING_LEVEL = Level.FINEST;
    public static final String TAG = "TicketViewApplication";
    private ArrayList<Ticket> myList;
    private Client myClient;

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

        bindViews();

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

    private void bindViews() {
        //populateList();
    }

    public ArrayList<Ticket> getTicketList() {
        return myList;
    }

    public void populateList() {

        if (myList == null) {
            AsyncAppData<Ticket> myAppData = myClient.appData("ticket",Ticket.class);
            Query myQuery = myAppData.query();
            myQuery.equals("status", "open");
            myAppData.get(myQuery, new KinveyListCallback<Ticket>() {
                @Override
                public void onSuccess(Ticket[] result) {
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

    public void saveTicket(Ticket ticket) {
        myClient.appData("ticket", Ticket.class).save(ticket, new KinveyClientCallback<Ticket>() {
            @Override
            public void onSuccess(Ticket result) {
                Toast.makeText(TicketViewActivity.this, "Ticket " + result.getSubject() + " saved", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Failed to save ticket", error);
            }
        });
    }
    
    public void ticketListFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, new TicketListFragment());
        ft.commit();   
    }

    public void ticketDetailsFragment(int position) {
        Ticket detailsTicket = myList.get(position);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, new TicketDetailsFragment(detailsTicket));
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
        Ticket newCommentTicket = myList.get(position);
        newCommentTicket.addComment(comment);
        saveTicket(newCommentTicket);
    }
}
