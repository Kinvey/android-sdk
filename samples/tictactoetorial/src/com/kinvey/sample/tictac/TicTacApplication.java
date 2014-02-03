package com.kinvey.sample.tictac;

import android.app.Application;
import android.util.Log;

import com.kinvey.android.Client;
import com.kinvey.java.core.KinveyClientCallback;

public class TicTacApplication extends Application {

	private Client myClient = null;
	
	private GameEntity myEntity;

	@Override
	public void onCreate() {
		super.onCreate();

	}

	public Client getClient() {
		if (myClient == null) {
			myClient = new Client.Builder(getApplicationContext()).build();
		}
		return this.myClient;
	}

	public GameEntity getMyEntity() {
		return myEntity;
	}

	public void setMyEntity(GameEntity myEntity, boolean persist) {
		this.myEntity = myEntity;
		if (persist){
			getClient().appData(TicTac.Collection, GameEntity.class).save(myEntity, new KinveyClientCallback<GameEntity>() {
				
				@Override
				public void onSuccess(GameEntity arg0) {
					Log.i(TicTac.TAG, "successfully persisted!");
					TicTacApplication.this.myEntity = arg0;
				}
				
				@Override
				public void onFailure(Throwable arg0) {
					Log.e(TicTac.TAG, "something went wrong! -> " + arg0.getMessage());
					arg0.printStackTrace();
					
				}
			});
			
			
		}
	}

}
