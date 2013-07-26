/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.samples.statusshare.fragments;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.model.KinveyReference;
import com.kinvey.samples.statusshare.R;
import com.kinvey.samples.statusshare.StatusShare;
import com.kinvey.samples.statusshare.model.CommentEntity;
import com.kinvey.samples.statusshare.model.UpdateEntity;


/**
 * @author edwardf
 * @since 2.0
 */
public class CommentEditFragment extends KinveyFragment {

    private TextView title;
    private EditText comment;

    private UpdateEntity parent;


    public static CommentEditFragment newInstance(UpdateEntity parent){
        CommentEditFragment ret = new CommentEditFragment();
        ret.setHasOptionsMenu(true);
        ret.setParent(parent);
        return ret;
    }

    private CommentEditFragment(){}

    @Override
    public int getViewID() {
        return R.layout.fragment_edit_comment;
    }

    @Override
    public void bindViews(View v) {
        title = (TextView) v.findViewById(R.id.comment_title);
        comment = (EditText) v.findViewById(R.id.comment_text);

        title.setTypeface(getRoboto());
        comment.setTypeface(getRoboto());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_edit_share, menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send_post:
                saveComment();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveComment(){

        if (getSherlockActivity() != null && getSherlockActivity().getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(getSherlockActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getSherlockActivity().getCurrentFocus().getWindowToken(), 0);
        }

        final CommentEntity ent = new CommentEntity(comment.getText().toString());
        ent.getAcl().setGloballyReadable(true);
        ent.setAuthor(getClient().user().getUsername());

        KinveyReference updateAuthor = new KinveyReference(parent.getAuthor().getCollection(), parent.getAuthor().getId());
        parent.setAuthor(updateAuthor);
        parent.resetCommentReferences();



//        getClient().appData(StatusShare.COL_UPDATES, UpdateEntity.class).getEntity(parent.getId(), new KinveyClientCallback<UpdateEntity>() {
//            @Override
//            public void onSuccess(UpdateEntity result) {

                getClient().appData(StatusShare.COL_COMMENTS, CommentEntity.class).save(ent, new KinveyClientCallback<CommentEntity>() {
                    @Override
                    public void onSuccess(CommentEntity commentEntity) {
                        if (getSherlockActivity() == null){
                            return;
                        }
                        parent.addComment(commentEntity);

                        getClient().appData(StatusShare.COL_UPDATES, UpdateEntity.class).save(parent, new KinveyClientCallback<UpdateEntity>() {
                            @Override
                            public void onSuccess(UpdateEntity updateEntity) {
                                if (getSherlockActivity() == null){
                                    return;
                                }
                                if (getSherlockActivity() != null){
                                    ((StatusShare)getSherlockActivity()).setShareList(null);
                                    ((StatusShare)getSherlockActivity()).replaceFragment(new ShareListFragment(), false);
                                }

                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Log.e(StatusShare.TAG, "error adding update entity -> ", throwable);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e(StatusShare.TAG, "error adding comment -> ", throwable);
                    }
                });           // }

//            @Override
//            public void onFailure(Throwable error) {
//                Log.e(StatusShare.TAG, "error adding update entity -> ", error);
//            }
       // });





    }

    public UpdateEntity getParent() {
        return parent;
    }

    public void setParent(UpdateEntity parent) {
        this.parent = parent;
    }
}
