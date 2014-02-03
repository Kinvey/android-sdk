package com.kinvey.sample.tictac.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.sample.tictac.R;
import com.kinvey.sample.tictac.TicTac;

public class RegisterFragment extends SherlockFragment implements OnClickListener {
	
	
	private EditText username;
	private EditText password;
	private EditText passConf;
	private EditText email;
	private Button register;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		View v = inflater.inflate(R.layout.fragment_register, group, false);
	       bindViews(v);
	        return v;
	    }
	    
	    private void bindViews(View v){
	    	username = (EditText) v.findViewById(R.id.register_username);
	    	password = (EditText) v.findViewById(R.id.register_password);
	    	passConf = (EditText) v.findViewById(R.id.register_confirm_password);
	    	email = (EditText) v.findViewById(R.id.register_email);
	    	register = (Button) v.findViewById(R.id.register_button);
	    	register.setOnClickListener(this);
	    	
	    	
	    }

		@Override
		public void onClick(View v) {
			if (v == register){
				register();
				
			}
			
		}
		
		private void register(){
			
			
			final String user = username.getText().toString();
			final String pass = password.getText().toString();
			
			TicTac.getClient(getSherlockActivity()).user().put("email", email.getText().toString());

			
			TicTac.getClient(getSherlockActivity()).user().create(user, pass, new KinveyUserCallback() {
				
				@Override
				public void onSuccess(User arg0) {
					TicTac.getClient(getSherlockActivity()).user().login(user, pass, new KinveyUserCallback() {
						
						@Override
						public void onSuccess(User arg0) {
							Log.i(TicTac.TAG, "Logged in as: " + TicTac.getClient(getSherlockActivity()).user().getUsername());
							Log.i(TicTac.TAG, "Logged in as: " +arg0.getUsername());

							Intent extras = new Intent();
							extras.putExtra("username", arg0.getUsername());
							
							RegisterFragment.this.getSherlockActivity().setResult(2, extras);
							RegisterFragment.this.getSherlockActivity().finish();
							
							
					
						}
						
						@Override
						public void onFailure(Throwable arg0) {
							Log.i(TicTac.TAG,  "Error logging in: " + arg0.getMessage());
							
						}
					});					
				}
				
				@Override
				public void onFailure(Throwable arg0) {
					Log.i(TicTac.TAG,  "Error Creating: " + arg0.getMessage());
					
				}
			});
			
			
			
		}

}
