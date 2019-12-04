package com.example.slidepuzzleproj;

public class User {
    private static String userID = null;

    public static void setID(String val){
        userID = val;
    }

    public static String getID(){
        return userID;
    }

}
