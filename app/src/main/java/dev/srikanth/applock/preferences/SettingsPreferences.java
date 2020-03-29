package dev.srikanth.applock.preferences;

import dev.srikanth.applock.utils.Constants;


public class SettingsPreferences extends Preferences{
	
	private static SettingsPreferences instance;
	private SettingsPreferences(){
		
	}
	
	public static SettingsPreferences getInstance(){
		if(instance == null)
			instance = new SettingsPreferences();
		return instance;
	}

	public void setLockedApps(String name){
		editor.putString(Constants.LOCKEDAPPS, name);
		editor.commit();
	}
	
	public String getLockedApps(){
		return preferences.getString(Constants.LOCKEDAPPS, "");
	}

	public void setPreviousApp(String name){
		editor.putString(Constants.PREVIOUSAPP, name);
		editor.commit();
	}

	public String getPreviousApp(){
		return preferences.getString(Constants.PREVIOUSAPP, "");
	}

}
