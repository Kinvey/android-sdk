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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class TicketCommentDialogFragment extends SherlockDialogFragment {

    public interface OnNewCommentListener {
        void onNewComment(int position, String comment);
    }

    private EditText comment;
    private int position;

    public TicketCommentDialogFragment(int position) {
        this.position = position;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.ticket_add_comment_dialog, null);
        comment = (EditText) view.findViewById(R.id.txt_comment);
        builder.setTitle("Add Comment");
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                OnNewCommentListener activity = (OnNewCommentListener) getActivity();
                activity.onNewComment(position, comment.getText().toString());
                getDialog().dismiss();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDialog().dismiss();
            }
        });

        builder.setCancelable(true);
        builder.setView(view);
        builder.create();

        return builder.create();
    }
}
