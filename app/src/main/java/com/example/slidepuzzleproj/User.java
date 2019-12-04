package com.example.slidepuzzleproj;

public class User {
    private static String userID = null;
    private static long seed = -1;

    public static void setID(String val){
        userID = val;
    }

    public static String getID(){
        return userID;
    }

    public static void setSeed(long s){
        seed = s;
    }
    public static long getSeed(){
        return seed;
    }
}
