package dev.srikanth.applock.preferences;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import dev.srikanth.applock.interfaces.IPreferences;

public abstract class Preferences implements IPreferences {

	protected SharedPreferences preferences;
	protected SharedPreferences.Editor editor;
//	public  Preferences(){
//		this.preferences =PreferenceManager.getDefaultSharedPreferences(CurrentActiviy.getActivity());
//				editor = preferences.edit();
//	}
	@SuppressLint("CommitPrefEdits")
	@Override
	public void setPreferenceObject(SharedPreferences preferences) {
		this.preferences = preferences;
		editor = preferences.edit();
	}

}
