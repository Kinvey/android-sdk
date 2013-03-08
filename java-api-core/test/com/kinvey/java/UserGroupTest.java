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
package com.kinvey.java;


import com.google.api.client.json.GenericJson;

import com.kinvey.java.UserGroup;
import com.kinvey.java.core.KinveyMockUnitTest;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author edwardf
 * @since 2.0
 */
public class UserGroupTest extends KinveyMockUnitTest {




    public void testUserGroupUpdate(){

        UserGroup group = new UserGroup(mockClient, getKinveyRequestInitializer());

        try{
            UserGroup.Update update = group.addUserToGroup("Group1", "user1", "subgroup1");
            assertNotNull(update);
            assertEquals("Group1", ((GenericJson) update.getJsonContent()).get("_id"));
            assertEquals(1, ((ArrayList)((GenericJson)(((GenericJson) update.getJsonContent()).get("users"))).get("list")).size());
            assertEquals("user1", ((GenericJson)((ArrayList)((GenericJson)(((GenericJson) update.getJsonContent()).get("users"))).get("list")).get(0)).get("_id"));
        }catch (IOException io){
            fail("IO -> " + io.getMessage());
        }
    }

    public void testUserGroupRetrieve(){
        UserGroup group = new UserGroup(mockClient, getKinveyRequestInitializer());
        try{
            UserGroup.Retrieve ret = group.retrieve("Group1");
            assertNotNull(ret);
            assertEquals("Group1", ret.get("groupID"));
        }catch (IOException io){
            fail("IO -> " + io.getMessage());
        }
    }

    public void testUserGroupDelete(){
        UserGroup group = new UserGroup(mockClient, getKinveyRequestInitializer());
        try{
            UserGroup.Delete delete = group.delete("Group1");
            assertNotNull(delete);
            assertEquals("Group1", delete.get("groupID"));
        }catch (IOException io){
            fail("IO -> " + io.getMessage());
        }


    }




}
