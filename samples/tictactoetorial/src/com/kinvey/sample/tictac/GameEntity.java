package com.kinvey.sample.tictac;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.java.model.KinveyMetaData;

public class GameEntity extends GenericJson{
	

	
	@Key
	private int totalWins;
	@Key
	private int totalLoses;
	@Key
	private int totalTies;
    @Key("_acl")
    private KinveyMetaData.AccessControlList acl;
    @Key
    private String playerName;
    @Key(KinveyMetaData.JSON_FIELD_NAME)
    private KinveyMetaData meta;
    
    
    private Bitmap gravatar;
    
    private GravatarCallback callback = null;
   
	
	
	public GameEntity(){
		meta = new KinveyMetaData();
		acl = new KinveyMetaData.AccessControlList();
	}

	public int getTotalWins() {
		return totalWins;
	}

	public void setTotalWins(int totalWins) {
		this.totalWins = totalWins;
	}

	public int getTotalLoses() {
		return totalLoses;
	}

	public void setTotalLoses(int totalLoses) {
		this.totalLoses = totalLoses;
	}

	public int getTotalTies() {
		return totalTies;
	}

	public void setTotalTies(int totalTies) {
		this.totalTies = totalTies;
	}
	
	public void addWin(){
		this.totalWins++;
	}
	public void addLose(){
		this.totalLoses++;
		
	}
	public void addTie(){
		this.totalTies++;
		
	}

	public KinveyMetaData.AccessControlList getAcl() {
		return acl;
	}

	public void setAcl(KinveyMetaData.AccessControlList acl) {
		this.acl = acl;
	}

	public String getPlayerName() {
		return playerName;
	}

	/* Set the player name and calculate the url of it's gravatar.
	 * 
	 * And kick off an async task to download the gravatar and set it.
	 * 
	 */
	public void setPlayerName(String playerName) {
		
		this.playerName = playerName;
		if (playerName == null){
			return;
		}
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] digest = digester.digest(playerName.getBytes());

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }

            String url = new String("http://www.gravatar.com/avatar/" + sb.toString() + ".jpg?d=identicon");
            android.util.Log.v(TicTac.TAG, playerName + " = " + url);
            new DownloadAvatarTask().execute(url);
       } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
		
	}
	
	public Bitmap getGravatar() {
		return gravatar;
	}

	public void setGravatar(Bitmap gravatar) {
		this.gravatar = gravatar;
	}

	public GravatarCallback getCallback() {
		return callback;
	}

	public void setCallback(GravatarCallback callback) {
		this.callback = callback;
	}

	private class DownloadAvatarTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			try {
				setGravatar(BitmapFactory.decodeStream((InputStream) new URL(
						params[0]).getContent()));
			} catch (MalformedURLException e) {
				Log.e(TicTac.TAG, "url for avatar download is bad", e);
			} catch (IOException e) {
				Log.e(TicTac.TAG, "failed to download avatar", e);
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(Void v){
			if (getCallback() != null){
				getCallback().gravatarBack();
			}
			
		}
		
	
		

	}

    public interface GravatarCallback{
        void gravatarBack();
    }
	

}
