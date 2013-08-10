/** 
 * Copyright (c) 2013, Kinvey, Inc. All rights reserved.
 *
 * This software is licensed to you under the Kinvey terms of service located at
 * http://www.kinvey.com/terms-of-use. By downloading, accessing and/or using this
 * software, you hereby accept such terms of service  (and any agreement referenced
 * therein) and agree that you have read, understand and agree to be bound by such
 * terms of service and are of legal age to agree to such terms with Kinvey.
 *
 * This software contains valuable confidential and proprietary information of
 * KINVEY, INC and is subject to applicable licensing agreements.
 * Unauthorized reproduction, transmission or distribution of this file and its
 * contents is a violation of applicable laws.
 * 
 */
package com.kinvey.android;

import java.io.IOException;
import java.util.List;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.UserGroup;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.KinveyClientRequestInitializer;

/**
 * Wraps the {@link com.kinvey.java.UserGroup} public methods in asynchronous functionality using native Android AsyncTask.
 *
 * <p>
 * This functionality can be accessed through the {@link com.kinvey.android.Client#userGroup()} convenience method.
 * </p>
 *
 * <p>
 * This API is used to create and manage user groups for role-based permissions.  Methods in this API return results via a
 * {@link com.kinvey.java.core.KinveyClientCallback}.
 * </p>
 *
 * <p>
 * Sample Usage:
 * <pre>
    public void submit(View view) {
        kinveyClient.userGroup().addUserToGroup("13", "15", null new KinveyClientCallback<UserGroupResponse> () {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(UserGroupResponse u) { ... }
        });
    }
 * </pre>
 *
 * </p>
 *
 *
 * <p>This class is not thread-safe.</p>
 *
 * @author mjsalinger
 * @since 2.0
 */
public class AsyncUserGroup extends UserGroup {

    AsyncUserGroup(AbstractClient client, KinveyClientRequestInitializer initializer) {
        super(client, initializer);
    }

    /**
     * Asynchronous request to add user to a group
     * <p>
     * Constructs an asynchronous request to add a user to a particular user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
        kinveyClient.userGroup().addUserToGroup("13", "14", null, new KinveyClientCallback<UserGroupResponse>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(UserGroupResponse r) { ... }
        });
    }
     * </pre>
     * </p>
     *
     * @param groupID a {@link java.lang.String} group ID to add a user to.
     * @param userID a {@link java.lang.String} user ID to add to a group.
     * @param childGroupID a {@link java.lang.String} child group ID to add a user to.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void addUserToGroup(String groupID, String userID, String childGroupID,
                                   KinveyClientCallback<T> callback){
        update(getUserGroupRequest(groupID, userID, childGroupID), callback);
    }

    /**
     * Asynchronous request to add a list of users to a group
     * <p>
     * Constructs an asynchronous request to add a list of users to a particular user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
        kinveyClient.userGroup().addUserListToGroup("13", userIDList, null, new KinveyClientCallback<UserGroupResponse>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(UserGroupResponse r) { ... }
        });
    }
     * </pre>
     * </p>
     *
     * @param groupID a {@link java.lang.String} group ID to add list of users to.
     * @param userIDs a {@link java.util.List} of Strings containing userIDs to add to groups
     * @param childGroupID a {@link java.lang.String} object containing a child ID.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void addUserListToGroup(String groupID, List<String> userIDs, String childGroupID,
                                       KinveyClientCallback<T> callback) {
        update(getUserGroupRequest(groupID, userIDs, childGroupID), callback);
    }

    /**
     * Asynchronous request to add a user to a list of groups
     * <p>
     * Constructs an asynchronous request to add a user to a list of user group IDs. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
        kinveyClient.userGroup().addUserToGroupList("13", "125", myGroupList, new KinveyClientCallback<UserGroupResponse>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(UserGroupResponse r) { ... }
        });
    }
     </pre>
     </p>
     *
     * @param groupID a {@link java.lang.String} group ID to add a user to.
     * @param userID a {@link java.lang.String} user ID to add.
     * @param childGroupID a {@link java.util.List} List of child group IDs.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void addUserToGroupList(String groupID, String userID, List<String> childGroupID,
                                     KinveyClientCallback<T> callback) {
        update(getUserGroupRequest(groupID,userID,childGroupID),callback);
    }

    /**
     * Asynchronous request to add a user list to a list of groups
     * <p>
     * Constructs an asynchronous request to add a list of users to a list of user group IDs. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     {@code
        kinveyClient.userGroup().addUserListToGroupList("13", "125", myGroupList, new KinveyClientCallback<UserGroupResponse>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(UserGroupResponse r) { ... }
        });
     }
     </pre>
     </p>
     *
     * @param groupID a {@link java.lang.String} group ID to add users to.
     * @param userIDs a {@link java.util.List} of Strings containing User IDs.
     * @param childGroupIDs a {@link java.util.List} child group IDs.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void addUserListToGroupList(String groupID, List<String> userIDs, List<String> childGroupIDs,
                                           KinveyClientCallback<T> callback) {
        update(getUserGroupRequest(groupID, userIDs, childGroupIDs), callback);
    }

    /**
     * Asynchronous request to add all users to a specific group
     * <p>
     * Constructs an asynchronous request to add all users of an app to a user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
        kinveyClient.userGroup().addAllUsersToGroup("13", "125", new KinveyClientCallback<UserGroupResponse>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(UserGroupResponse r) { ... }
        });
    </pre>
    </p>
     *
     * @param groupID a {@link java.lang.String} group ID to add all users to.
     * @param childGroupID a {@link java.lang.String} child group to add all users to.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void addAllUsersToGroup(String groupID, String childGroupID, KinveyClientCallback<T> callback)
           {
         update(getUserGroupRequest(groupID, childGroupID), callback);
    }

    /**
     * Asynchronous request to add all users to a list of user groups
     * <p>
     * Constructs an asynchronous request to add all users to a list of user group IDs. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
        kinveyClient.userGroup().addAllUsersToGroupList("13", myGroupIDs, new KinveyClientCallback<UserGroupResponse>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(UserGroupResponse r) { ... }
        });
    }
     </pre>
     </p>
     *
     * @param groupID a {@link java.lang.String} group ID to add all users to.
     * @param childGroupIDs a {@link java.util.List} list of child IDs to add all users to.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void addAllUsersToGroupList(String groupID, List<String> childGroupIDs, KinveyClientCallback<T> callback)
           {
         update(getUserGroupRequest(groupID, childGroupIDs), callback);
    }

    /**
     * Asynchronous request to create a user group
     * <p>
     * Constructs an asynchronous request to createa a user group. Takes a UserGroupRequest object and returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
        UserGroup request = kinveyClient.userGroup().getUserGroupRequest(myGroupID, myChildGroupID);
        kinveyClient.userGroup().create(request, new KinveyClientCallback<UserGroupResponse>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(UserGroupResponse r) { ... }
         });
    }
     </pre>
     </p>
     *
     * @param group a UserGroupRequest object containing the group to create.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void create(UserGroupRequest group, KinveyClientCallback<T> callback){
        new Create(group, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to retrieve a user group
     * <p>
     * Constructs an asynchronous request to retrieve a user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     {@code
        kinveyClient.userGroup().retrieve(myGroupID, new KinveyClientCallback<UserGroupResponse>() {
             public void onFailure(Throwable t) { ... }
             public void onSuccess(UserGroupResponse r) { ... }
     });
     }
     </pre>
     </p>
     *
     * @param groupID a {@link java.lang.String} group ID to retrieve.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void retrieve(String groupID, KinveyClientCallback<T> callback){
        new Retrieve(groupID, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to update a user group
     * <p>
     * Constructs an asynchronous request to retrieve a user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
        UserGroup request = kinveyClient.userGroup().getUserGroupRequest(myGroupID, myChildGroupID);
        kinveyClient.userGroup().update(reqeuest, new KinveyClientCallback<UserGroupResponse>() {
             public void onFailure(Throwable t) { ... }
             public void onSuccess(UserGroupResponse r) { ... }
        });
    </pre>
    </p>
     *
     * @param group a UserGroupRequest containing the group to be updated.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void update(UserGroupRequest group, KinveyClientCallback<T> callback){
        new Update(group, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    /**
     * Asynchronous request to delete a user group
     * <p>
     * Constructs an asynchronous request to delete a user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     * </p>
     * <p>
     * Sample Usage:
     * <pre>
     * {@code
        kinveyClient.userGroup().delete(reqeuest, new KinveyClientCallback<UserGroupResponse>() {
            public void onFailure(Throwable t) { ... }
            public void onSuccess(UserGroupResponse r) { ... }
        });
    }
     </pre>
     </p>
     *
     * @param groupID a {@link java.lang.String} group ID to be deleted.
     * @param callback a {@link com.kinvey.java.core.KinveyClientCallback} object.
     * @param <T> a T object.
     */
    public <T> void delete(String groupID, KinveyClientCallback<T> callback) {
        new Delete(groupID, callback).execute(AsyncClientRequest.ExecutorType.KINVEYSERIAL);
    }

    private class Create extends AsyncClientRequest<UserGroupResponse> {
        UserGroupRequest request;

        private Create(UserGroupRequest group, KinveyClientCallback callback) {
            super(callback);
            this.request = group;
        }

        @Override
        protected UserGroupResponse executeAsync() throws IOException {
            return AsyncUserGroup.this.create(request).execute();
        }
    }

    private class Retrieve extends AsyncClientRequest<UserGroupResponse> {
        String groupID;

        private Retrieve(String groupID, KinveyClientCallback callback) {
            super(callback);
            this.groupID = groupID;
        }

        @Override
        protected UserGroupResponse executeAsync() throws IOException {
            return AsyncUserGroup.this.retrieve(groupID).execute();
        }
    }

    private class Update extends AsyncClientRequest<UserGroupResponse> {
        UserGroupRequest request;

        private Update(UserGroupRequest group, KinveyClientCallback callback) {
            super(callback);
            this.request = group;
        }

        @Override
        protected UserGroupResponse executeAsync() throws IOException {
            return AsyncUserGroup.this.update(request).execute();
        }
    }

    private class Delete extends AsyncClientRequest<UserGroupResponse> {
        String groupID;

        private Delete(String groupID, KinveyClientCallback callback) {
            super(callback);
            this.groupID = groupID;
        }

        @Override
        protected UserGroupResponse executeAsync() throws IOException {
            return AsyncUserGroup.this.delete(groupID).execute();
        }
    }
}
