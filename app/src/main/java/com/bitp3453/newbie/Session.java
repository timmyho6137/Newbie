package com.bitp3453.newbie;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Timmy Ho on 5/21/2017.
 */

public class Session {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;

    public Session(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("myapp", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLoggedIn(boolean loggedIn){
        editor.putBoolean("loggedInmode", loggedIn);
        editor.commit();
    }

    public void setUserName(String userName){
        editor.putString("name", userName);
        editor.commit();
    }

    public void setMatricNo(String matricNo){
        editor.putString("matric", matricNo);
        editor.commit();
    }

    public void setEmail(String email){
        editor.putString("email", email);
        editor.commit();
    }

    public void setPassword(String password){
        editor.putString("password", password);
        editor.commit();
    }

    public void setTokenStored(boolean stored){
        editor.putBoolean("tokenStored", stored);
        editor.commit();
    }

    public void setCategories(String categories){
        editor.putString("categories", categories);
        editor.commit();
    }

    public void setPenalty(boolean penalty){
        editor.putBoolean("penalty", penalty);
        editor.commit();
    }

    public boolean loggedIn(){
        return sharedPreferences.getBoolean("loggedInmode", false);
    }

    public String name(){
        return sharedPreferences.getString("name",null);
    }

    public String email(){
        return sharedPreferences.getString("email",null);
    }

    public String password(){
        return sharedPreferences.getString("password",null);
    }

    public String matricNo(){
        return sharedPreferences.getString("matric", null);
    }

    public String categories(){
        return sharedPreferences.getString("categories", null);
    }

    public boolean tokenStored(){
        return sharedPreferences.getBoolean("tokenStored", false);
    }

    public boolean penalty(){
        return sharedPreferences.getBoolean("penalty", false);
    }
}
