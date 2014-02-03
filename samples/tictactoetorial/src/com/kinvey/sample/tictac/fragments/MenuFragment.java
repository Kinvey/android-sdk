package com.kinvey.sample.tictac.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.query.AbstractQuery.SortOrder;
import com.kinvey.sample.tictac.GameEntity;
import com.kinvey.sample.tictac.GameEntity.GravatarCallback;
import com.kinvey.sample.tictac.R;
import com.kinvey.sample.tictac.TicTac;
import com.kinvey.sample.tictac.component.LeaderAdapter;

public class MenuFragment extends SherlockFragment implements OnClickListener,
		GravatarCallback {

	private TableLayout leaderBoard;

	private ListView leaderList;
	private Button newGame;

	private List<GameEntity> leaders;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (leaders == null || leaders.size() == 0) {
			leaders = new ArrayList<GameEntity>();
		}
		populateViews();


	}

	private void populateViews() {

		Query top = new Query();
		top.addSort("totalWins", SortOrder.DESC);
		top.setLimit(5);
		leaders.clear();


		TicTac.getClient(getSherlockActivity())
				.appData(TicTac.Collection, GameEntity.class)
				.get(top, new KinveyListCallback<GameEntity>() {

					@Override
					public void onSuccess(GameEntity[] arg0) {
						for (int i = 0; i < arg0.length; i++) {

							arg0[i].setPlayerName(arg0[i].getPlayerName());
							arg0[i].setCallback(MenuFragment.this);
							leaders.add(arg0[i]);

						}
						Log.i(TicTac.TAG, "leaderboard has: " + leaders.size());
						updateBoard();

					}

					@Override
					public void onFailure(Throwable arg0) {
						Toast.makeText(getSherlockActivity(),
								"something went wrong -> " + arg0.getMessage(),
								Toast.LENGTH_SHORT).show();

					}
				});

	}

	private void updateBoard() {

		if (getSherlockActivity() == null) {
			return;
		}

		leaderList.setAdapter(new LeaderAdapter(getSherlockActivity(), leaders,
				(LayoutInflater) getSherlockActivity().getSystemService(
						Activity.LAYOUT_INFLATER_SERVICE)));

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		View v = inflater.inflate(R.layout.fragment_menu, group, false);
		bindViews(v);
		return v;
	}

	private void bindViews(View v) {
		// leaderBoard = (TableLayout) v.findViewById(R.id.menu_leaderboard);
		leaderList = (ListView) v.findViewById(R.id.menu_list);
		newGame = (Button) v.findViewById(R.id.menu_newgame);
		newGame.setOnClickListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		if (v == newGame) {
			FragmentTransaction ft = getSherlockActivity()
					.getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, new GameFragment());
			ft.addToBackStack("Game");
			ft.commit();
		}
	}

	@Override
	public void gravatarBack() {
		// Don't do this, instead follow tutorial here:
		// http://evancharlton.com/thoughts/lazy-loading-images-in-a-listview
		updateBoard();
	}

}
