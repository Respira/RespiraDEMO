package com.respira.dimitri.respirademo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Dimitri on 6/09/2016.
 */
public class ApplicationWPM extends Application {
    private int WPM;
    static SharedPreferences sharedPreferences;


    public static void setDontShow(boolean value,Context context)
    {
        String key="dontshow";
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public static boolean getDontShow(Context context){

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getBoolean("dontshow",false)){
            return sharedPreferences.getBoolean("dontshow", false);
        }else
        {
            return sharedPreferences.getBoolean("dontshow", false);
        }
    }

    public static void SaveInt( int value, Context context){
        String key="wpm";
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    public static int LoadInt(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getInt("wpm", 0)<60){
            return 60;
        }else
        {
            return sharedPreferences.getInt("wpm", 0);
        }

    }
    public static boolean loadPermission(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean("permission",false)){
            return true;
        }else{
            return false;
        }
    }
    public static void savePermission(boolean value,Context context){
        String key="permission";
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public static int getCurrentWPM(Context context)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getInt("currentwpm", 0)==0){
            return 0;
        }else
        {
            return sharedPreferences.getInt("currentwpm", 0);
        }
    }
    public static void setCurrentWPM(int value, Context context){
        String key="currentwpm";
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    public static void saveGraph(Map<Long,Integer> map, Context context){
    try {

        JSONArray arr = new JSONArray();
        for(long index : map.keySet()) {
            JSONObject json = new JSONObject();
            json.put("sec", index);
            json.put("wpm", map.get(index));
            Log.e(String.valueOf(index), String.valueOf(map.get(index)));
            arr.put(json);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedGraph", arr.toString()).commit();
    } catch (JSONException exception) {
        // Do something with exception
    }
    }
    public static Map<Long,Integer> getGraph(Context context)
    {
        Map<Long, Integer> hash = new TreeMap<>();
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String data = sharedPreferences.getString("savedGraph",null);
            if(data!=null){

            JSONArray arr = new JSONArray(data);
            for(int i = 0; i < arr.length(); i++) {
                JSONObject json = arr.getJSONObject(i);
                hash.put(json.getLong("sec"), json.getInt("wpm"));
            }
            return hash;
            }else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return hash;
        }

    }
    public static void saveEmail(String email, Context context){
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", email).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getEmail(Context context)
    {
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String data = sharedPreferences.getString("email",null);
            if(data!=null){


                return data;
            }else {
                return null;
            }
    }catch(Exception e){
            e.printStackTrace();
            return null;
        }}   public static void saveUser(String user, Context context){
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", user).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getUser(Context context)
    {
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String data = sharedPreferences.getString("username",null);
            if(data!=null){


                return data;
            }else {
                return null;
            }
    }catch(Exception e){
            e.printStackTrace();
            return null;
        }}


public static void savePassword(String pwd, Context context){
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("pwd", pwd).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getPassword(Context context)
    {
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String data = sharedPreferences.getString("pwd",null);
            if(data!=null){


                return data;
            }else {
                return null;
            }
    }catch(Exception e){
            e.printStackTrace();
            return null;
        }

}
public static void saveToken(String token, Context context){
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("token", token).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getToken(Context context)
    {
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String data = sharedPreferences.getString("token",null);
            if(data!=null){


                return data;
            }else {
                return null;
            }
    }catch(Exception e){
            e.printStackTrace();
            return null;
        }

}
    public static void setLogin(boolean value,Context context)
    {
        String key="login";
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public static boolean getLogin(Context context){

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPreferences.getBoolean("login",false)){
            return sharedPreferences.getBoolean("login", false);
        }else
        {
            return sharedPreferences.getBoolean("login", false);
        }
    }











}
