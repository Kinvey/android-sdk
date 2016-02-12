/** 
 * Copyright (c) 2014, Kinvey, Inc. All rights reserved.
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
package com.kinvey.java;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kinvey.java.AbstractClient;
import com.kinvey.java.core.AbstractKinveyJsonClientRequest;
import com.kinvey.java.core.KinveyClientRequestInitializer;
import com.kinvey.java.model.KinveyMetaData;


/**
 * @author edwardf
 * @since 2.0
 */
public class UserGroup {


    /**
     * This class is used to represent an individual user involved in the UserGroup operation.
     *
     * The only modifiable field is _id, which is of the user, and the other vales are hardcoded.
     */
    protected static class GroupUser extends GenericJson {

        @Key("_type")
        private final String type = "KinveyRef";

        @Key("_collection")
        private final String collection = "user";

        @Key("_id")
        private String id;

        public GroupUser(){}

        public GroupUser(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    /**
     * This class is used to represent a Group, which can be modified with this UserGroup class.
     *
     */
    public static class Group extends GenericJson {

        @Key("_type")
        private static final String _type = "KinveyRef";

        @Key("_collection")
        private static final String _collection = "group";

        @Key("_id")
        private String id;

        public Group(){}

        public Group(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }


    /**
     * This class represents a list of users, as well as a flag to indicate if all users should be involved with the User Group operation.
     *
     *
     */
    protected static class UserSet extends GenericJson{

        @Key
        private boolean all;

        @Key
        private List<GroupUser> list;

        public UserSet(boolean allUsers, List<GroupUser> userList){
            this.all = allUsers;
            this.list = userList;
        }

        public UserSet(){}

        public boolean isAll() {
            return all;
        }

        public void setAll(boolean all) {
            this.all = all;
        }

        public List<GroupUser> getList() {
            return list;
        }

        public void setList(List<GroupUser> list) {
            this.list = list;
        }

    }

    protected static class UserGroupRequest extends GenericJson{
        @Key("_id")
        private String id;

        @Key
        private UserSet users;

        @Key
        private List<Group> groups;

        public UserGroupRequest(){}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public UserSet getUsers() {
            return users;
        }

        public void setUsers(UserSet users) {
            this.users = users;
        }

        public List<Group> getGroups() {
            return groups;
        }

        public void setGroups(List<Group> groups) {
            this.groups = groups;
        }
    }

    public static class UserGroupResponse extends KinveyMetaData{
        @Key("_id")
        private String id;

        @Key
        private UserSet users;

        @Key
        private List<Group> groups;

        public UserGroupResponse(){}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public UserSet getUsers() {
            return users;
        }

        public void setUsers(UserSet users) {
            this.users = users;
        }

        public List<Group> getGroups() {
            return groups;
        }

        public void setGroups(List<Group> groups) {
            this.groups = groups;
        }

    }



    private AbstractClient client;


    /**
     *
     * Constructor to access Kinvey's UserGroup management.
     *
     * @param client - an instance of Kinvey AbstractClient, configured for the application
     * @param initializer
     */
    protected UserGroup(AbstractClient client, KinveyClientRequestInitializer initializer) {
        Preconditions.checkNotNull(client, "client must not be null.");
        Preconditions.checkNotNull(initializer, "initializer must not be null.");
        this.client = client;
    }


    //----------public API wrappers
    //Add this user to this group
    public Update addUserToGroupBlocking(String groupID, String userID, String childGroupID) throws IOException{
        return update(getUserGroupRequest(groupID, userID, childGroupID));
    }

    /**
     * @deprecated Renamed to {@link #addUserToGroupBlocking(String, String, String)}
     */
    @Deprecated
    public Update addUserToGroup(String groupID, String userID, String childGroupID) throws IOException{
        return update(getUserGroupRequest(groupID, userID, childGroupID));
    }

    //Add this list of users to this group
    public Update addUserListToGroupBlocking(String groupID, List<String> userIDs, String childGroupID) throws IOException{
        return update(getUserGroupRequest(groupID, userIDs, childGroupID));
    }

    /**
     * @deprecated {@link #addUserListToGroupBlocking(String, java.util.List, String)}
     */
    @Deprecated
    public Update addUserListToGroup(String groupID, List<String> userIDs, String childGroupID) throws IOException{
        return update(getUserGroupRequest(groupID, userIDs, childGroupID));
    }

    //Add this user to this list of groups

    /**
     *
     * @deprecated Renamed to {@link #addUserListToGroupListBlocking(String, java.util.List, java.util.List)}
     */
    @Deprecated
    public Update addUserToGroupList(String groupID, String userID, List<String> childGroupID) throws IOException{
        return update(getUserGroupRequest(groupID, userID, childGroupID));
    }

    //Add this list of users to this list of groups
    public Update addUserListToGroupListBlocking(String groupID, List<String> userIDs, List<String> childGroupIDs)
            throws IOException{
        return update(getUserGroupRequest(groupID, userIDs, childGroupIDs));
    }

    /**
     * @deprecated Renamed to {@link #addUserListToGroupListBlocking(String, java.util.List, java.util.List)}
     */
    @Deprecated
    public Update addUserListToGroupList(String groupID, List<String> userIDs, List<String> childGroupIDs)
            throws IOException{
        return update(getUserGroupRequest(groupID, userIDs, childGroupIDs));
    }

    //Add all users to this group
    public Update addAllUsersToGroupBlocking(String groupID, String childGroupID) throws IOException{
        return update(getUserGroupRequest(groupID, childGroupID));
    }

    /**
     * @deprecated {@link #addAllUsersToGroupBlocking(String, String)}
     */
    @Deprecated
    public Update addAllUsersToGroup(String groupID, String childGroupID) throws IOException{
        return update(getUserGroupRequest(groupID, childGroupID));
    }

    //Add all users to this group, with the other child groups
    public Update addAllUsersToGroupListBlocking(String groupID, List<String> childGroupIDs) throws IOException{
        return update(getUserGroupRequest(groupID, childGroupIDs));
    }

    /**
     * @deprecated {@link #addAllUsersToGroupListBlocking(String, java.util.List)}
     */
    @Deprecated
    public Update addAllUsersToGroupList(String groupID, List<String> childGroupIDs) throws IOException{
        return update(getUserGroupRequest(groupID, childGroupIDs));
    }

    //------------------ protected helper methods
    protected UserGroupRequest getUserGroupRequest(String groupID, List<String> childGroupIDs) {
        Preconditions.checkNotNull(groupID, "groupID cannot be null.");

        UserSet set = getUserSet(null);
        ArrayList<Group> groups = getGroupList(childGroupIDs);

        UserGroupRequest req = buildRequest(set, groups);
        req.setId(groupID);
        return req;
    }

    protected UserGroupRequest getUserGroupRequest(String groupID, String childGroupID) {
        Preconditions.checkNotNull(groupID, "groupID cannot be null.");

        UserSet set = getUserSet(null);
        ArrayList<Group> groups = getGroupList(Arrays.asList(new String[]{childGroupID}));

        UserGroupRequest req = buildRequest(set, groups);
        req.setId(groupID);
        return req;
    }

    protected UserGroupRequest getUserGroupRequest(String groupID, List<String> userIDs, List<String> childGroupIDs) {
        Preconditions.checkNotNull(userIDs, "userIDs cannot be  null.");
        Preconditions.checkNotNull(groupID, "groupID cannot be null.");

        UserSet set = getUserSet(userIDs);
        ArrayList<Group> groups = getGroupList(childGroupIDs);

        UserGroupRequest req = buildRequest(set, groups);
        req.setId(groupID);
        return req;
    }

    protected UserGroupRequest getUserGroupRequest(String groupID, String userID, List<String> childGroupID) {
        Preconditions.checkNotNull(userID, "userID cannot be null.");
        Preconditions.checkNotNull(groupID, "groupID cannot be null.");

        UserSet set = getUserSet(Arrays.asList(new String[]{userID}));
        ArrayList<Group> groups = getGroupList(childGroupID);

        UserGroupRequest req = buildRequest(set, groups);
        req.setId(groupID);
        return req;
    }


    protected UserGroupRequest getUserGroupRequest(String groupID, List<String> userIDs, String childGroupID) {
        Preconditions.checkNotNull(userIDs, "userIDS cannot be null.");
        Preconditions.checkNotNull(groupID, "groupID cannot be null.");

        UserSet set = getUserSet(userIDs);
        ArrayList<Group> groups = getGroupList(Arrays.asList(new String[]{childGroupID}));

        UserGroupRequest req = buildRequest(set, groups);
        req.setId(groupID);
        return req;
    }

    protected UserGroupRequest getUserGroupRequest(String groupID, String userID, String childGroupID) {
        Preconditions.checkNotNull(userID, "userID cannot be null.");
        Preconditions.checkNotNull(groupID, "groupID cannot be null.");

        UserSet set = getUserSet(Arrays.asList(new String[]{userID}));
        ArrayList<Group> groups = getGroupList(Arrays.asList(new String[]{childGroupID}));

        UserGroupRequest req = buildRequest(set, groups);
        req.setId(groupID);
        return req;
    }


    protected UserGroupRequest buildRequest(UserSet set, List<Group> groups){
        UserGroupRequest req = new UserGroupRequest();
        req.setUsers(set);
        req.setGroups(groups);
        req.setId("G");
        return req;
    }

    protected ArrayList<Group> getGroupList(List<String> groupIDs){
        ArrayList<Group> groups = new ArrayList<Group>();
        for (String s : groupIDs){
            Group group = new Group();
            group.setId(s);
            groups.add(group);
        }
        return groups;
    }

    protected UserSet getUserSet(List<String> userIDs){
        UserSet set = new UserSet();
        if (userIDs == null){
            set.setAll(true);
            return set;
        }   else{
            set.setAll(false);
        }

        ArrayList<GroupUser> users = new ArrayList<GroupUser>();
        for (String s : userIDs){
            GroupUser user = new GroupUser();
            user.setId(s);
            users.add(user);
        }
        set.setList(users);
        return set;
    }


    //------------------AbstractClient Request Wrappers
    public Create create(UserGroupRequest group) throws IOException{
        Preconditions.checkNotNull(group, "group must not be null.");
        Create create = new Create(group);
        this.client.initializeRequest(create);
        return create;
    }

    public Retrieve retrieve(String groupID) throws IOException{
        Preconditions.checkNotNull(groupID, "groupID must not be null.");
        Retrieve retrieve = new Retrieve(groupID);
        client.initializeRequest(retrieve);
        return retrieve;
    }

    public Update update(UserGroupRequest group) throws IOException{
        Preconditions.checkNotNull(group, "group must not be null");
        Update update = new Update(group);
        client.initializeRequest(update);
        return update;
    }

    public Delete delete(String groupID) throws IOException{
        Preconditions.checkNotNull(groupID, "groupID must not be null.");
        Delete delete = new Delete(groupID);
        client.initializeRequest(delete);
        return delete;
    }


    //--------------------AbstractClient Request Implementations


    /**
     * TODO (from REST API documentation  http://devcenter.kinvey.com/rest/guides/users#usergroupsdelete )
     *  It's important that the app admin clean ACL metadata in the backend before deleting a group in order to prevent
     *  a reincarnation of the same group from getting access based on old metadata.
     *  TODO
     *  Can figure out what this means by creating two users, assigning them both to a group, logging in as one of the users,
     *  deleting the group, and then logging in as the other user and recreating a group with the same name that doesn't
     *  include the other user and then see what happens.
     */
    public class Delete extends AbstractKinveyJsonClientRequest<UserGroupResponse> {
        private static final String REST_PATH = "group/{appKey}/{groupID}";

        @Key
        private String groupID;

        Delete(String groupID) {
            super(client, "DELETE", REST_PATH, null, UserGroupResponse.class);
            this.groupID = groupID;
        }
    }

    public class Create extends AbstractKinveyJsonClientRequest<UserGroupResponse> {
        private static final String REST_PATH = "group/{appKey}/";

        Create(UserGroupRequest group) {
            super(client, "POST", REST_PATH, group, UserGroupResponse.class);
        }
    }

    public class Retrieve extends AbstractKinveyJsonClientRequest<UserGroupResponse> {
        private static final String REST_PATH = "group/{appKey}/{groupID}";

        @Key
        private String groupID;

        Retrieve(String groupID) {
            super(client, "GET", REST_PATH, null, UserGroupResponse.class);
            this.groupID = groupID;
        }
    }

    public class Update extends AbstractKinveyJsonClientRequest<UserGroupResponse> {
        private static final String REST_PATH = "group/{appKey}/{groupID}";

        @Key
        private String groupID;

        Update(UserGroupRequest group) {
            super(client, "PUT", REST_PATH, group, UserGroupResponse.class);
            this.groupID = group.getId();
        }
    }

}