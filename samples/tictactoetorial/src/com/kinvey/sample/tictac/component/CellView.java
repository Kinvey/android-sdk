package com.kinvey.sample.tictac.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.kinvey.sample.tictac.R;

public class CellView extends ImageView implements OnTouchListener {

	public enum CellState {
		X, O, EMPTY
	}

	private int xRes = R.drawable.x;
	private int oRes = R.drawable.o;
	private int eRes = R.drawable.empty;
	
	private OnTouchListener touchListenDelegate = null;

	private CellState state = CellState.EMPTY;


	public CellView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.CellView, 0, 0);
		
		xRes = ta.getResourceId(R.styleable.CellView_xImage, R.drawable.x);
		oRes = ta.getResourceId(R.styleable.CellView_oImage, R.drawable.o);
		eRes = ta.getResourceId(R.styleable.CellView_emptyImage, R.drawable.empty);
		ta.recycle();


		init();
	}

	private void init() {
		setState(CellState.EMPTY);
		this.setFocusableInTouchMode(true);
		this.setOnTouchListener(this);

	}

	public void setState(CellState state) {
		if (state == CellState.X) {
			setImageResource(xRes);
		} else if (state == CellState.O) {
			setImageResource(oRes);
		} else if (state == CellState.EMPTY) {
			setImageResource(eRes);
		}
		this.state = state;
	}

	public int getValue() {
		if (state == CellState.X) {
			return 1;
		} else if (state == CellState.O) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (state == CellState.EMPTY){
			setState(CellState.X);
			if (getTouchListenDelegate() != null){
				touchListenDelegate.onTouch(v, event);
				
			}
			
			
			
		}
		return false;
	}

	public OnTouchListener getTouchListenDelegate() {
		return touchListenDelegate;
	}

	public void setTouchListenDelegate(OnTouchListener touchListenDelegate) {
		this.touchListenDelegate = touchListenDelegate;
	}

	

}
