/*
 *  Copyright (c) 2016, Kinvey, Inc. All rights reserved.
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
package com.kinvey.android

import java.io.IOException

import com.kinvey.java.AbstractClient
import com.kinvey.java.UserGroup
import com.kinvey.java.core.KinveyClientCallback
import com.kinvey.java.core.KinveyClientRequestInitializer

/**
 * Wraps the [com.kinvey.java.UserGroup] public methods in asynchronous functionality using native Android AsyncTask.
 *
 *
 *
 * This functionality can be accessed through the [com.kinvey.android.Client.userGroup] convenience method.
 *
 *
 *
 *
 * This API is used to create and manage user groups for role-based permissions.  Methods in this API return results via a
 * [com.kinvey.java.core.KinveyClientCallback].
 *
 *
 *
 *
 * Sample Usage:
 * <pre>
 * public void submit(View view) {
 * kinveyClient.userGroup().addUserToGroup("13", "15", null new KinveyClientCallback<UserGroupResponse> () {
 * public void onFailure(Throwable t) { ... }
 * public void onSuccess(UserGroupResponse u) { ... }
 * });
 * }
</UserGroupResponse></pre> *
 *
 *
 *
 *
 *
 * This class is not thread-safe.
 *
 * @author mjsalinger
 * @since 2.0
 */
class AsyncUserGroup internal constructor(client: AbstractClient<*>, initializer: KinveyClientRequestInitializer) : UserGroup(client, initializer) {

    /**
     * Asynchronous request to add user to a group
     *
     *
     * Constructs an asynchronous request to add a user to a particular user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userGroup().addUserToGroup("13", "14", null, new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param groupID a [java.lang.String] group ID to add a user to.
     * @param userID a [java.lang.String] user ID to add to a group.
     * @param childGroupID a [java.lang.String] child group ID to add a user to.
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     * @param <T> a T object.
    </T> */
    fun addUserToGroup(groupID: String, userID: String, childGroupID: String,
                           callback: KinveyClientCallback<UserGroupResponse>) {
        update(getUserGroupRequest(groupID, userID, childGroupID), callback)
    }

    /**
     * Asynchronous request to add a list of users to a group
     *
     *
     * Constructs an asynchronous request to add a list of users to a particular user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userGroup().addUserListToGroup("13", userIDList, null, new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param groupID a [java.lang.String] group ID to add list of users to.
     * @param userIDs a [java.util.List] of Strings containing userIDs to add to groups
     * @param childGroupID a [java.lang.String] object containing a child ID.
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     * @param <T> a T object.
    </T> */
    fun addUserListToGroup(groupID: String, userIDs: List<String>, childGroupID: String,
                               callback: KinveyClientCallback<UserGroupResponse>) {
        update(getUserGroupRequest(groupID, userIDs, childGroupID), callback)
    }

    /**
     * Asynchronous request to add a user to a list of groups
     *
     *
     * Constructs an asynchronous request to add a user to a list of user group IDs. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userGroup().addUserToGroupList("13", "125", myGroupList, new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param groupID a [java.lang.String] group ID to add a user to.
     * @param userID a [java.lang.String] user ID to add.
     * @param childGroupID a [java.util.List] List of child group IDs.
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     * @param <T> a T object.
    </T> */
    fun addUserToGroupList(groupID: String, userID: String, childGroupID: List<String>,
                               callback: KinveyClientCallback<UserGroupResponse>) {
        update(getUserGroupRequest(groupID, userID, childGroupID), callback)
    }

    /**
     * Asynchronous request to add a user list to a list of groups
     *
     *
     * Constructs an asynchronous request to add a list of users to a list of user group IDs. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userGroup().addUserListToGroupList("13", "125", myGroupList, new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param groupID a [java.lang.String] group ID to add users to.
     * @param userIDs a [java.util.List] of Strings containing User IDs.
     * @param childGroupIDs a [java.util.List] child group IDs.
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     * @param <T> a T object.
    </T> */
    fun addUserListToGroupList(groupID: String, userIDs: List<String>, childGroupIDs: List<String>,
                                   callback: KinveyClientCallback<UserGroupResponse>) {
        update(getUserGroupRequest(groupID, userIDs, childGroupIDs), callback)
    }

    /**
     * Asynchronous request to add all users to a specific group
     *
     *
     * Constructs an asynchronous request to add all users of an app to a user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userGroup().addAllUsersToGroup("13", "125", new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
     * </pre>
     * </p>
     *
     * groupID a { java.lang.String} group ID to add all users to.
     * childGroupID a { java.lang.String} child group to add all users to.
     * callback a { com.kinvey.java.core.KinveyClientCallback} object.
     * <T> a T object.`
    </pre> */
    fun addAllUsersToGroup(groupID: String, childGroupID: String, callback: KinveyClientCallback<UserGroupResponse>) {
        update(getUserGroupRequest(groupID, childGroupID), callback)
    }

    /**
     * Asynchronous request to add all users to a list of user groups
     *
     *
     * Constructs an asynchronous request to add all users to a list of user group IDs. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userGroup().addAllUsersToGroupList("13", myGroupIDs, new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param groupID a [java.lang.String] group ID to add all users to.
     * @param childGroupIDs a [java.util.List] list of child IDs to add all users to.
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     * @param <T> a T object.
    </T> */
    fun addAllUsersToGroupList(groupID: String, childGroupIDs: List<String>, callback: KinveyClientCallback<UserGroupResponse>) {
        update(getUserGroupRequest(groupID, childGroupIDs), callback)
    }

    /**
     * Asynchronous request to create a user group
     *
     *
     * Constructs an asynchronous request to createa a user group. Takes a UserGroupRequest object and returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `UserGroup request = kinveyClient.userGroup().getUserGroupRequest(myGroupID, myChildGroupID);
     * kinveyClient.userGroup().create(request, new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param group a UserGroupRequest object containing the group to create.
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     * @param <T> a T object.
    </T> */
    fun create(group: UserGroup.UserGroupRequest, callback: KinveyClientCallback<UserGroupResponse>) {
        Create(group, callback).execute()
    }

    /**
     * Asynchronous request to retrieve a user group
     *
     *
     * Constructs an asynchronous request to retrieve a user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userGroup().retrieve(myGroupID, new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param groupID a [java.lang.String] group ID to retrieve.
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     * @param <T> a T object.
    </T> */
    fun retrieve(groupID: String, callback: KinveyClientCallback<UserGroupResponse>) {
        Retrieve(groupID, callback).execute()
    }

    /**
     * Asynchronous request to update a user group
     *
     *
     * Constructs an asynchronous request to retrieve a user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `UserGroup request = kinveyClient.userGroup().getUserGroupRequest(myGroupID, myChildGroupID);
     * kinveyClient.userGroup().update(reqeuest, new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
     * </pre>
     * </p>
     *
     * group a UserGroupRequest containing the group to be updated.
     * callback a { com.kinvey.java.core.KinveyClientCallback} object.
     * <T> a T object.`
    </pre> */
    fun update(group: UserGroup.UserGroupRequest, callback: KinveyClientCallback<UserGroupResponse>) {
        Update(group, callback).execute()
    }

    /**
     * Asynchronous request to delete a user group
     *
     *
     * Constructs an asynchronous request to delete a user group. Returns a UserGroupResponse via
     * a KinveyClientCallback.
     *
     *
     *
     * Sample Usage:
     * <pre>
     * `kinveyClient.userGroup().delete(reqeuest, new KinveyClientCallback<UserGroupResponse>() {
     * public void onFailure(Throwable t) { ... }
     * public void onSuccess(UserGroupResponse r) { ... }
     * });
    ` *
    </pre> *
     *
     *
     * @param groupID a [java.lang.String] group ID to be deleted.
     * @param callback a [com.kinvey.java.core.KinveyClientCallback] object.
     * @param <T> a T object.
    </T> */
    fun delete(groupID: String, callback: KinveyClientCallback<UserGroupResponse>) {
        Delete(groupID, callback).execute()
    }

    private inner class Create internal constructor(internal var request: UserGroupRequest, callback: KinveyClientCallback<UserGroupResponse>) : AsyncClientRequest<UserGroupResponse>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): UserGroupResponse? {
            return this@AsyncUserGroup.create(request).execute()
        }
    }

    private inner class Retrieve internal constructor(internal var groupID: String, callback: KinveyClientCallback<UserGroupResponse>) : AsyncClientRequest<UserGroupResponse>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): UserGroupResponse? {
            return this@AsyncUserGroup.retrieve(groupID).execute()
        }
    }

    private inner class Update internal constructor(internal var request: UserGroupRequest, callback: KinveyClientCallback<UserGroupResponse>) : AsyncClientRequest<UserGroupResponse>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): UserGroupResponse? {
            return this@AsyncUserGroup.update(request).execute()
        }
    }

    private inner class Delete internal constructor(internal var groupID: String, callback: KinveyClientCallback<UserGroupResponse>) : AsyncClientRequest<UserGroupResponse>(callback) {

        @Throws(IOException::class)
        override fun executeAsync(): UserGroupResponse? {
            return this@AsyncUserGroup.delete(groupID).execute()
        }
    }
}
