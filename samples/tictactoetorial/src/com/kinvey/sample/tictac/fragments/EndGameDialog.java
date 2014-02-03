package com.kinvey.sample.tictac.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.kinvey.sample.tictac.GameEntity;
import com.kinvey.sample.tictac.R;
import com.kinvey.sample.tictac.TicTac;
import com.kinvey.sample.tictac.fragments.GameFragment.WINNER;

public class EndGameDialog extends SherlockDialogFragment {

	private TextView mEditText;

	private Button newGame;
	private Button quit;

	private EndGameDialogListener listener;

	WINNER winner;

	public interface EndGameDialogListener {
		void onQuit();

		void onNew();

	}
	
	public EndGameDialog(){}

	public EndGameDialog(WINNER winner, SherlockFragmentActivity act) {
		this.winner = winner;
		GameEntity e = ((TicTac) act).getMyGame();

		if (winner == WINNER.PLAYER) {
			e.addWin();
		} else if (winner == WINNER.COMPUTER) {
			e.addLose();
		} else {
			e.addTie();
		}
		((TicTac) act).setMyGame(e, true);

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_endgame, container);
		mEditText = (TextView) view.findViewById(R.id.endgame_result);
		newGame = (Button) view.findViewById(R.id.button_new);
		quit = (Button) view.findViewById(R.id.button_quit);
		getDialog().setTitle("Game Over!");
		mEditText.setText(winner.getDisplay());

		newGame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onNew();
				}
			}
		});

		quit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onQuit();
				}
			}
		});

		return view;
	}

	public EndGameDialogListener getListener() {
		return listener;
	}

	public void setListener(EndGameDialogListener listener) {
		this.listener = listener;
	}

}
