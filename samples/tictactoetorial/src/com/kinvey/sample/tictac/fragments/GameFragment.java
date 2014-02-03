package com.kinvey.sample.tictac.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.kinvey.sample.tictac.R;
import com.kinvey.sample.tictac.TicTac;
import com.kinvey.sample.tictac.component.CellView;
import com.kinvey.sample.tictac.component.CellView.CellState;
import com.kinvey.sample.tictac.fragments.EndGameDialog.EndGameDialogListener;

public class GameFragment extends SherlockFragment implements OnTouchListener {

	private CellView[][] gameState;

	private static final int n = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSherlockActivity().supportInvalidateOptionsMenu();

		setHasOptionsMenu(true);
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		View v = (LinearLayout) inflater.inflate(R.layout.fragment_tic_tac,
				group, false);
		gameState = new CellView[n][n];
		bindViews(v);
		newGame();

		return v;
	}

	private void bindViews(View v) {

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {

				int id = getResources().getIdentifier("tictac_box" + i + j,
						"id", getSherlockActivity().getPackageName());
				Log.i(TicTac.TAG, "loading view: " + "tictac_box" + i + j
						+ " and " + (v == null));

				gameState[i][j] = (CellView) v.findViewById(id);
				gameState[i][j].setTouchListenDelegate(this);
			}

		}

	}

	private void newGame() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {

				gameState[i][j].setState(CellState.EMPTY);
			}

		}

	}

	private boolean checkConditions() {
		// if this counter gets to n or -n, the player won or the computer won
		// respectively.

		// To optimize this, remove embedded loops and run them serially. n^2 ->
		// 2n

		int[] sumsHorizontal = new int[n];
		int[] sumsVertical = new int[n];
		int[] sumsDiag = new int[2];

		int index = 0;

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				sumsHorizontal[i] += gameState[i][j].getValue();
				sumsVertical[i] += gameState[j][i].getValue();
				if (i == j) {
					sumsDiag[0] += gameState[i][j].getValue();
				}
				if (i == n - j && j == 0 + i) { // TODO this doesn't work
					sumsDiag[1] += gameState[i][j].getValue();
				}
				// n- i, 0 + i

			}
		}
		int[] sums = combineArrays(sumsDiag,
				combineArrays(sumsHorizontal, sumsVertical));

		for (int i = 0; i < sums.length; i++) {
			if (sums[i] == n) {
				gameOver(WINNER.PLAYER);
				return true;
			} else if (sums[i] == -n) {
				gameOver(WINNER.COMPUTER);
				return true;

			}

		}

		List<Point> open = getFreeCells();
		if (open.size() == 0) {
			// StaleMate!
			gameOver(WINNER.STALEMATE);
		}

		return false;

	}

	public void gameOver(WINNER winner) {
		FragmentManager fm = getSherlockActivity().getSupportFragmentManager();
		final EndGameDialog endGameFragment = new EndGameDialog(winner, getSherlockActivity());
		endGameFragment.setListener(new EndGameDialogListener() {

			@Override
			public void onQuit() {
				endGameFragment.dismiss();
				FragmentTransaction ft = getSherlockActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(android.R.id.content, new MenuFragment());
				ft.commit();

			}

			@Override
			public void onNew() {
				newGame();

				endGameFragment.dismiss();
			}
		});
		endGameFragment.show(fm, "fragment_edit_name");

	}

	private void enemyMove() {
		List<Point> openPoints = getFreeCells();

		if (openPoints.size() > 0) {

			int x;
			Random rand = new Random();

			x = rand.nextInt(openPoints.size());

			gameState[openPoints.get(x).x][openPoints.get(x).y]
					.setState(CellState.O);
		}

	}

	private static int[] combineArrays(int[] a, int[] b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		int[] r = new int[a.length + b.length];
		System.arraycopy(a, 0, r, 0, a.length);
		System.arraycopy(b, 0, r, a.length, b.length);
		return r;

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.i(TicTac.TAG, "Touch received");
		if (!checkConditions()) {
			enemyMove();
			checkConditions();
		}
		return false;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.fragment_game, menu);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_newgame:
			newGame();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private List<Point> getFreeCells() {
		List<Point> openPoints = new ArrayList<Point>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (gameState[i][j].getValue() == 0) {
					openPoints.add(new Point(i, j));
				}
			}
		}
		return openPoints;
	}

    public enum WINNER {
        PLAYER("You won!"), COMPUTER("You lost!"), STALEMATE("It's a tie!");
        private String display;

        WINNER(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return this.display;
        }
    }

}
