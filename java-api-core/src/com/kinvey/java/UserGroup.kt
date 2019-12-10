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

package com.kinvey.java

import com.google.api.client.json.GenericJson
import com.google.api.client.util.Key
import com.google.common.base.Preconditions

import java.io.IOException
import java.util.ArrayList
import java.util.Arrays

import com.kinvey.java.AbstractClient
import com.kinvey.java.core.AbstractKinveyJsonClientRequest
import com.kinvey.java.core.KinveyClientRequestInitializer
import com.kinvey.java.model.KinveyMetaData


/**
 * @author edwardf
 * @since 2.0
 */
open class UserGroup
/**
 *
 * Constructor to access Kinvey's UserGroup management.
 *
 * @param client - an instance of Kinvey AbstractClient, configured for the application
 * @param initializer
 */
(private val client: AbstractClient<*>?, private val requestInitializer: KinveyClientRequestInitializer?) {


    /**
     * This class is used to represent an individual user involved in the UserGroup operation.
     *
     * The only modifiable field is _id, which is of the user, and the other vales are hardcoded.
     */
    class GroupUser : GenericJson {

        @Key("_type")
        private val type = "KinveyRef"

        @Key("_collection")
        private val collection = "user"

        @Key("_id")
        var id: String? = null

        constructor() {}

        constructor(id: String) {
            this.id = id
        }
    }

    /**
     * This class is used to represent a Group, which can be modified with this UserGroup class.
     *
     */
    class Group : GenericJson {

        @Key("_id")
        var id: String? = null

        constructor() {}

        constructor(id: String) {
            this.id = id
        }

        companion object {

            @Key("_type")
            private val _type = "KinveyRef"

            @Key("_collection")
            private val _collection = "group"
        }
    }


    /**
     * This class represents a list of users, as well as a flag to indicate if all users should be involved with the User Group operation.
     *
     *
     */
    class UserSet : GenericJson {

        @Key
        var isAll: Boolean = false

        @Key
        var list: List<GroupUser>? = null

        constructor(allUsers: Boolean, userList: List<GroupUser>) {
            this.isAll = allUsers
            this.list = userList
        }

        constructor() {}

    }

    class UserGroupRequest : GenericJson() {
        @Key("_id")
        var id: String? = null

        @Key
        var users: UserSet? = null

        @Key
        var groups: List<Group>? = null
    }

    class UserGroupResponse : KinveyMetaData() {
        @Key("_id")
        var id: String? = null

        @Key
        var users: UserSet? = null

        @Key
        var groups: List<Group>? = null

    }


    init {
        Preconditions.checkNotNull(client, "client must not be null.")
        Preconditions.checkNotNull(requestInitializer, "initializer must not be null.")
    }


    //----------public API wrappers
    //Add this user to this group
    @Throws(IOException::class)
    fun addUserToGroupBlocking(groupID: String, userID: String, childGroupID: String): Update {
        return update(getUserGroupRequest(groupID, userID, childGroupID))
    }


    @Deprecated("Renamed to {@link #addUserToGroupBlocking(String, String, String)}")
    @Throws(IOException::class)
    fun addUserToGroup(groupID: String, userID: String, childGroupID: String): Update {
        return update(getUserGroupRequest(groupID, userID, childGroupID))
    }

    //Add this list of users to this group
    @Throws(IOException::class)
    fun addUserListToGroupBlocking(groupID: String, userIDs: List<String>, childGroupID: String): Update {
        return update(getUserGroupRequest(groupID, userIDs, childGroupID))
    }


    @Deprecated("{@link #addUserListToGroupBlocking(String, java.util.List, String)}")
    @Throws(IOException::class)
    fun addUserListToGroup(groupID: String, userIDs: List<String>, childGroupID: String): Update {
        return update(getUserGroupRequest(groupID, userIDs, childGroupID))
    }

    //Add this user to this list of groups


    @Deprecated("Renamed to {@link #addUserListToGroupListBlocking(String, java.util.List, java.util.List)}")
    @Throws(IOException::class)
    fun addUserToGroupList(groupID: String, userID: String, childGroupID: List<String>): Update {
        return update(getUserGroupRequest(groupID, userID, childGroupID))
    }

    //Add this list of users to this list of groups
    @Throws(IOException::class)
    fun addUserListToGroupListBlocking(groupID: String, userIDs: List<String>, childGroupIDs: List<String>): Update {
        return update(getUserGroupRequest(groupID, userIDs, childGroupIDs))
    }


    @Deprecated("Renamed to {@link #addUserListToGroupListBlocking(String, java.util.List, java.util.List)}")
    @Throws(IOException::class)
    fun addUserListToGroupList(groupID: String, userIDs: List<String>, childGroupIDs: List<String>): Update {
        return update(getUserGroupRequest(groupID, userIDs, childGroupIDs))
    }

    //Add all users to this group
    @Throws(IOException::class)
    fun addAllUsersToGroupBlocking(groupID: String, childGroupID: String): Update {
        return update(getUserGroupRequest(groupID, childGroupID))
    }


    @Deprecated("{@link #addAllUsersToGroupBlocking(String, String)}")
    @Throws(IOException::class)
    fun addAllUsersToGroup(groupID: String, childGroupID: String): Update {
        return update(getUserGroupRequest(groupID, childGroupID))
    }

    //Add all users to this group, with the other child groups
    @Throws(IOException::class)
    fun addAllUsersToGroupListBlocking(groupID: String, childGroupIDs: List<String>): Update {
        return update(getUserGroupRequest(groupID, childGroupIDs))
    }


    @Deprecated("{@link #addAllUsersToGroupListBlocking(String, java.util.List)}")
    @Throws(IOException::class)
    fun addAllUsersToGroupList(groupID: String, childGroupIDs: List<String>): Update {
        return update(getUserGroupRequest(groupID, childGroupIDs))
    }

    //------------------ protected helper methods
    protected fun getUserGroupRequest(groupID: String, childGroupIDs: List<String>): UserGroupRequest {
        Preconditions.checkNotNull(groupID, "groupID cannot be null.")

        val set = getUserSet(null)
        val groups = getGroupList(childGroupIDs)

        val req = buildRequest(set, groups)
        req.id = groupID
        return req
    }

    protected fun getUserGroupRequest(groupID: String, childGroupID: String): UserGroupRequest {
        Preconditions.checkNotNull(groupID, "groupID cannot be null.")

        val set = getUserSet(null)
        val groups = getGroupList(Arrays.asList(*arrayOf(childGroupID)))

        val req = buildRequest(set, groups)
        req.id = groupID
        return req
    }

    protected fun getUserGroupRequest(groupID: String, userIDs: List<String>, childGroupIDs: List<String>): UserGroupRequest {
        Preconditions.checkNotNull(userIDs, "userIDs cannot be  null.")
        Preconditions.checkNotNull(groupID, "groupID cannot be null.")

        val set = getUserSet(userIDs)
        val groups = getGroupList(childGroupIDs)

        val req = buildRequest(set, groups)
        req.id = groupID
        return req
    }

    protected fun getUserGroupRequest(groupID: String, userID: String, childGroupID: List<String>): UserGroupRequest {
        Preconditions.checkNotNull(userID, "userID cannot be null.")
        Preconditions.checkNotNull(groupID, "groupID cannot be null.")

        val set = getUserSet(Arrays.asList(*arrayOf(userID)))
        val groups = getGroupList(childGroupID)

        val req = buildRequest(set, groups)
        req.id = groupID
        return req
    }


    protected fun getUserGroupRequest(groupID: String, userIDs: List<String>, childGroupID: String): UserGroupRequest {
        Preconditions.checkNotNull(userIDs, "userIDS cannot be null.")
        Preconditions.checkNotNull(groupID, "groupID cannot be null.")

        val set = getUserSet(userIDs)
        val groups = getGroupList(Arrays.asList(*arrayOf(childGroupID)))

        val req = buildRequest(set, groups)
        req.id = groupID
        return req
    }

    protected fun getUserGroupRequest(groupID: String, userID: String, childGroupID: String): UserGroupRequest {
        Preconditions.checkNotNull(userID, "userID cannot be null.")
        Preconditions.checkNotNull(groupID, "groupID cannot be null.")

        val set = getUserSet(Arrays.asList(*arrayOf(userID)))
        val groups = getGroupList(Arrays.asList(*arrayOf(childGroupID)))

        val req = buildRequest(set, groups)
        req.id = groupID
        return req
    }


    protected fun buildRequest(set: UserSet, groups: List<Group>): UserGroupRequest {
        val req = UserGroupRequest()
        req.users = set
        req.groups = groups
        req.id = "G"
        return req
    }

    protected fun getGroupList(groupIDs: List<String>): ArrayList<Group> {
        val groups = ArrayList<Group>()
        for (s in groupIDs) {
            val group = Group()
            group.id = s
            groups.add(group)
        }
        return groups
    }

    protected fun getUserSet(userIDs: List<String>?): UserSet {
        val set = UserSet()
        if (userIDs == null) {
            set.isAll = true
            return set
        } else {
            set.isAll = false
        }

        val users = ArrayList<GroupUser>()
        for (s in userIDs) {
            val user = GroupUser()
            user.id = s
            users.add(user)
        }
        set.list = users
        return set
    }


    //------------------AbstractClient Request Wrappers
    @Throws(IOException::class)
    fun create(group: UserGroupRequest): Create {
        Preconditions.checkNotNull(group, "group must not be null.")
        val create = Create(group)
        this.client?.initializeRequest(create)
        return create
    }

    @Throws(IOException::class)
    fun retrieve(groupID: String): Retrieve {
        Preconditions.checkNotNull(groupID, "groupID must not be null.")
        val retrieve = Retrieve(groupID)
        client?.initializeRequest(retrieve)
        return retrieve
    }

    @Throws(IOException::class)
    fun update(group: UserGroupRequest): Update {
        Preconditions.checkNotNull(group, "group must not be null")
        val update = Update(group)
        client?.initializeRequest(update)
        return update
    }

    @Throws(IOException::class)
    fun delete(groupID: String): Delete {
        Preconditions.checkNotNull(groupID, "groupID must not be null.")
        val delete = Delete(groupID)
        client?.initializeRequest(delete)
        return delete
    }

    companion object {
        private val GROUP_APP_KEY_GROUP_ID_REST_PATH = "group/{appKey}/{groupID}"
        private val GROUP_APP_KEY_REST_PATH = "group/{appKey}/"
    }


    //--------------------AbstractClient Request Implementations


    /**
     * TODO (from REST API documentation  http://devcenter.kinvey.com/rest/guides/users#usergroupsdelete )
     * It's important that the app admin clean ACL metadata in the backend before deleting a group in order to prevent
     * a reincarnation of the same group from getting access based on old metadata.
     * TODO
     * Can figure out what this means by creating two users, assigning them both to a group, logging in as one of the users,
     * deleting the group, and then logging in as the other user and recreating a group with the same name that doesn't
     * include the other user and then see what happens.
     */
    inner class Delete internal constructor(@field:Key
                                            private val groupID: String) : AbstractKinveyJsonClientRequest<UserGroupResponse>(client, "DELETE", GROUP_APP_KEY_GROUP_ID_REST_PATH, null, UserGroupResponse::class.java) {

    }

    inner class Create internal constructor(group: UserGroupRequest) : AbstractKinveyJsonClientRequest<UserGroupResponse>(client, "POST", GROUP_APP_KEY_REST_PATH, group, UserGroupResponse::class.java) {

    }

    inner class Retrieve internal constructor(@field:Key
                                              private val groupID: String) : AbstractKinveyJsonClientRequest<UserGroupResponse>(client, "GET", GROUP_APP_KEY_GROUP_ID_REST_PATH, null, UserGroupResponse::class.java) {
    }

    inner class Update internal constructor(group: UserGroupRequest) : AbstractKinveyJsonClientRequest<UserGroupResponse>(client, "PUT", GROUP_APP_KEY_GROUP_ID_REST_PATH, group, UserGroupResponse::class.java) {

        @Key
        private val groupID: String?

        init {
            this.groupID = group.id
        }

    }

}