package com.fpoly.pro1121.userapp;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharePreference {

    SharedPreferences sharedPreferences;
    private static final String NAME_SHARE = "share";
    SharedPreferences.Editor editor;
    private static MySharePreference instance;
    public static synchronized MySharePreference getInstance(Context context) {
        if(instance==null){
            instance = new MySharePreference(context);
        }
        return instance;
    }
    private MySharePreference(Context context) {
        sharedPreferences = context.getSharedPreferences(NAME_SHARE,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key,value);
        editor.commit();
    }
    public boolean getBoolean(String key) {
      return  sharedPreferences.getBoolean(key,false);
    }
}
