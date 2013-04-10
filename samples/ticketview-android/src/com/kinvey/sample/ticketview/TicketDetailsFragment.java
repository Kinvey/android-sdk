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
package com.kinvey.sample.ticketview;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.text.format.DateUtils;
import android.text.format.DateFormat;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.sample.ticketview.model.TicketCommentEntity;
import com.kinvey.sample.ticketview.model.TicketEntity;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class TicketDetailsFragment extends SherlockFragment implements TicketViewActivity.RefreshCallback {

    private ListView commentListView;
    private ArrayAdapter<String> myAdapter;
    private TicketEntity myTicket;
    private TextView subject;
    private TextView description;
    private TextView requestedBy;
    private TextView requestedDate;
    private TextView status;
    private ArrayList<String> comments = new ArrayList<String>();
    private int position;


    public TicketDetailsFragment(TicketEntity ticket, int position) {
        this.myTicket = ticket;
        this.position = position;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return  inflater.inflate(R.layout.ticket_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getComments();
    }

    private void getComments() {
        ((TicketViewActivity)getActivity()).getComments(myTicket.getTicketId(), new KinveyListCallback<TicketCommentEntity>() {
            @Override
            public void onSuccess(TicketCommentEntity[] result) {
                ArrayList<TicketCommentEntity> tempList = new ArrayList<TicketCommentEntity>(Arrays.asList(result));
                Collections.sort(tempList, new CommentComparator());
                Collections.reverse(tempList);
                ArrayList<String> commentList = new ArrayList<String>();
                for (TicketCommentEntity entity : tempList) {
                    try {
                        commentList.add(new SimpleDateFormat().format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                                .parse(entity.getCommentDate())) + " -" + (entity.getCommentBy() != null ? entity.getCommentBy() : "") + "- " + entity.getComment());
                    } catch (ParseException ex) {
                        Log.e(((TicketViewActivity)getActivity()).getTag(),"Parse Exception on comment Date", ex);
                        commentList.add(" -- " + entity.getComment());
                    }

                }
                comments = commentList;
                bindViews();
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(((TicketViewActivity) getActivity()).getTag(), "Error retrieving comments");
            }
        });
    }

    private void bindViews() {
        myAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, comments);
        commentListView = (ListView) getActivity().findViewById(R.id.commentListView);
        commentListView.setAdapter(myAdapter);
        subject = (TextView) getActivity().findViewById(R.id.ticketSubject);
        description = (TextView) getActivity().findViewById(R.id.ticketDescription);
        requestedBy = (TextView) getActivity().findViewById(R.id.ticketRequestedBy);
        requestedDate = (TextView) getActivity().findViewById(R.id.ticketRequestedDate);
        status = (TextView) getActivity().findViewById(R.id.ticketStatus);
        setValues();
    }

    @Override
    public void refreshCallback() {
        setValues();
        getComments();
        myAdapter.notifyDataSetChanged();
    }

    private void setValues() {
        subject.setText(myTicket.getSubject());
        description.setText(myTicket.getDescription());
        requestedBy.setText(myTicket.getRequestedBy());

        try {
            requestedDate.setText(new SimpleDateFormat().format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(myTicket.getRequestDate())));


        } catch (ParseException ex) {
            Log.e(((TicketViewActivity)getActivity()).getTag(),"Parse Exception on request Date", ex);
            requestedDate.setText("");
        }
        status.setText(myTicket.getStatus());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.add_comment:
                ((TicketViewActivity) getActivity()).showCommentDialog(position, this);
                return true;
            case R.id.close_ticket:
                closeTicket();
                return true;
            case android.R.id.home:
                ((TicketViewActivity) getActivity()).ticketListFragment();
                return true;
            default: return false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ticket_details, menu);
    }

    private void closeTicket() {
        myTicket.setStatus("closed");
        ((TicketViewActivity) getActivity()).saveTicket(myTicket);
        setValues();
    }

    private class CommentComparator implements Comparator<TicketCommentEntity> {
        @Override
        public int compare(TicketCommentEntity o1, TicketCommentEntity o2) {
            return o1.getCommentDate().compareTo(o2.getCommentDate());
        }
    }
}
