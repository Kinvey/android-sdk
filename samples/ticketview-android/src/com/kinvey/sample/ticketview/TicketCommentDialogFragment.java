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
        void onNewComment(int position, String comment, TicketViewActivity.RefreshCallback callback);
    }

    private EditText comment;
    private int position;
    private TicketViewActivity.RefreshCallback callback;

    public TicketCommentDialogFragment(int position, TicketViewActivity.RefreshCallback refreshCallback) {
        this.position = position;
        this.callback = refreshCallback;
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
                activity.onNewComment(position, comment.getText().toString(), callback);
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
