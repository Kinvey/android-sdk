package com.example.testdrive.android.model;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Value;
import com.kinvey.java.offline.OfflineGenericJson;

public class Entity extends OfflineGenericJson {

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
