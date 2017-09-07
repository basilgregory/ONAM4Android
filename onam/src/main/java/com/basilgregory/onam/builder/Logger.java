package com.basilgregory.onam.builder;

import android.util.Log;

/**
 * Created by donpeter on 9/7/17.
 */

public class Logger {

    static class InternalLogger{
        static void v(String message){
            Log.v("ONAM.INTERNAL","<<< ---------  START :  for internal usage ------ >>>");
            Log.v("ONAM.INTERNAL",message);
            Log.v("ONAM.INTERNAL","<<< ---------  END : for internal usage ------ >>>");
        }
        static void d(String message){
            Log.d("ONAM.INTERNAL","<<< ---------  START :  for internal usage ------ >>>");
            Log.d("ONAM.INTERNAL",message);
            Log.d("ONAM.INTERNAL","<<< ---------  END : for internal usage ------ >>>");
        }
        static void w(String message){
            Log.w("ONAM.INTERNAL","<<< ---------  START :  for internal usage ------ >>>");
            Log.w("ONAM.INTERNAL",message);
            Log.w("ONAM.INTERNAL","<<< ---------  END : for internal usage ------ >>>");
        }
        static void e(String message){
            Log.e("ONAM.INTERNAL","<<< ---------  START :  for internal usage ------ >>>");
            Log.e("ONAM.INTERNAL",message);
            Log.e("ONAM.INTERNAL","<<< ---------  END : for internal usage ------ >>>");
        }
    }

    static void v(String message){
        Log.v("ONAM",message);
    }
    static void d(String message){
        Log.d("ONAM",message);
    }
    static void w(String message){
        Log.w("ONAM",message);
    }
    static void e(String message){
        Log.e("ONAM",message);
    }
}
