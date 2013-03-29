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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;

import com.kinvey.sample.ticketview.model.Ticket;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class TicketDetailsFragment extends SherlockFragment {

    private ListView commentListView;
    private ArrayAdapter<String> myAdapter;
    private Ticket myTicket;
    private TextView subject;
    private TextView description;
    private TextView requestedBy;
    private TextView requestedDate;
    private TextView status;


    public TicketDetailsFragment(Ticket ticket) {
        this.myTicket = ticket;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.ticket_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, myTicket.getComments());

        bindViews();
    }

    private void bindViews() {
        commentListView = (ListView) getActivity().findViewById(R.id.commentListView);
        commentListView.setAdapter(myAdapter);
        subject = (TextView) getActivity().findViewById(R.id.ticketSubject);
        description = (TextView) getActivity().findViewById(R.id.ticketDescription);
        requestedBy = (TextView) getActivity().findViewById(R.id.ticketRequestedBy);
        requestedDate = (TextView) getActivity().findViewById(R.id.ticketRequestedDate);
        status = (TextView) getActivity().findViewById(R.id.ticketStatus);
        setValues();
    }

    private void setValues() {
        subject.setText(myTicket.getSubject());
        description.setText(myTicket.getDescription());
        requestedBy.setText(myTicket.getRequestedBy());
        requestedDate.setText(myTicket.getRequestDate());
        status.setText(myTicket.getStatus());

    }
}
