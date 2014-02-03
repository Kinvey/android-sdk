package com.kinvey.sample.tictac;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.api.client.http.HttpTransport;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.sample.tictac.fragments.MenuFragment;

public class TicTac extends SherlockFragmentActivity {

	public static final String TAG = "TicTac";
	public static final String Collection = "GameResults";
	public static final int REQUEST_LOGIN = 0;
	private boolean showMenu = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.FINEST);
		// check if logged in-- if not, start user activity
		// else start main menu fragment
		if (TicTac.getClient(this).user().isUserLoggedIn()) {
			showMenu();
		} else {
			startUserActivity();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (showMenu) {
			showMenu();
		}
	}

	private void showMenu() {
		loadGame();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, new MenuFragment());
		ft.commit();
	}

	private void loadGame() {
		Query q = new Query();
		q.equals("_acl.creator", TicTac.getClient(this).user().getId());
		TicTac.getClient(this).appData(TicTac.Collection, GameEntity.class)
				.get(q, new KinveyListCallback<GameEntity>() {

					@Override
					public void onSuccess(GameEntity[] arg0) {
						if (arg0.length >= 1) {
							// player exists
							Log.v(TicTac.TAG, "games loaded!");

							arg0[0].setPlayerName(arg0[0].getPlayerName()
									.toString());
							arg0[0].getAcl().setGloballyReadable(true);
							setMyGame(arg0[0], true);

						} else {
							// new player
							Log.v(TicTac.TAG, "creating new games");

							GameEntity newOne = new GameEntity();
							newOne.setPlayerName(TicTac.getClient(TicTac.this)
									.user().getUsername());
							newOne.setTotalLoses(0);
							newOne.setTotalWins(0);
							newOne.setTotalTies(0);
							newOne.getAcl().setGloballyReadable(true);
							setMyGame(newOne, true);
						}
					}

					@Override
					public void onFailure(Throwable arg0) {
						Log.e(TicTac.TAG,
								"something went wrong -> " + arg0.getMessage());

					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_tic_tac, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_login:
			startUserActivity();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static Client getClient(Context context) {
		return ((TicTacApplication) context.getApplicationContext())
				.getClient();

	}

	private void startUserActivity() {
		Intent userIntent = new Intent(this, UserActivity.class);
		startActivityForResult(userIntent, REQUEST_LOGIN);

	}

	public void setMyGame(GameEntity myGame, boolean persist) {
		((TicTacApplication) getApplication()).setMyEntity(myGame, persist);
	}

	public GameEntity getMyGame() {
		return ((TicTacApplication) getApplication()).getMyEntity();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_LOGIN) {
			showMenu = true;
		}

	}
}
