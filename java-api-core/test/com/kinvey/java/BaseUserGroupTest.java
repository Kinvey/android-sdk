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

import com.kinvey.java.core.KinveyMockUnitTest;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author edwardf
 * @since 2.0
 */
public class BaseUserGroupTest extends KinveyMockUnitTest {

    private static final String ID = "_id";
    private static final String USERS = "users";
    private static final String LIST = "list";

    public void testUserGroupUpdate(){
        UserGroup group = new UserGroup(getClient(), getKinveyRequestInitializer());
        try{
            UserGroup.Update update = group.addUserToGroupBlocking("Group1", "user1", "subgroup1");
            assertNotNull(update);
            assertEquals("Group1", ((GenericJson) update.getJsonContent()).get(ID));
            assertEquals(1, ((ArrayList)((GenericJson)(((GenericJson) update.getJsonContent()).get(USERS))).get(LIST)).size());
            assertEquals("user1", ((GenericJson)((ArrayList)((GenericJson)(((GenericJson) update.getJsonContent()).get(USERS))).get(LIST)).get(0)).get(ID));
        }catch (IOException io){
            fail("IO -> " + io.getMessage());
        }
    }

    public void testUserGroupRetrieve(){
        UserGroup group = new UserGroup(getClient(), getKinveyRequestInitializer());
        try{
            UserGroup.Retrieve ret = group.retrieve("Group1");
            assertNotNull(ret);
            assertEquals("Group1", ret.get("groupID"));
        }catch (IOException io){
            fail("IO -> " + io.getMessage());
        }
    }

    public void testUserGroupDelete(){
        UserGroup group = new UserGroup(getClient(), getKinveyRequestInitializer());
        try{
            UserGroup.Delete delete = group.delete("Group1");
            assertNotNull(delete);
            assertEquals("Group1", delete.get("groupID"));
        }catch (IOException io){
            fail("IO -> " + io.getMessage());
        }
    }

}
