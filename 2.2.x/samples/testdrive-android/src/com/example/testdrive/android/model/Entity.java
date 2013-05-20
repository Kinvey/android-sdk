package com.example.testdrive.android.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

public class Entity extends GenericJson {

	@Key("_id")
	private String title;

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
}
