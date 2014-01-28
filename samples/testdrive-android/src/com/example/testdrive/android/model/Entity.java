/** 
 * Copyright (c) 2014, Kinvey, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.example.testdrive.android.model;

import android.app.Activity;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Value;

public class Entity extends GenericJson {

	@Key("_id")
	private String title;

    @Key
    private test ok = test.ONE;

	public Entity() {}
	
	public Entity(String title) {
		super();
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

    public test getOk() {
        return ok;
    }

    public void setOk(test ok) {
        this.ok = ok;
    }

    public enum test{@Value ONE, @Value TWO}

}
