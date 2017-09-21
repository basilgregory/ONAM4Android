package com.basilgregory.onam.android;

import android.util.Log;

/**
 * Created by donpeter on 9/7/17.
 */

public class L {

    private static L lInstance = null;

    private L() {
        warning = true;
        error = true;
        verbose = false;
        verboseInternal = false;
        debug = false;
    }

    public static L getInstance() {
        return lInstance == null ? new L() : lInstance;
    }

    private static boolean verbose, debug, warning, error, verboseInternal;

    public static void setDebug(boolean debug) {
        L.debug = debug;
    }

    public void setVerbose(boolean verboseValue) {
        verbose = verboseValue;
    }

    public static void setVerboseInternal(boolean verboseInternal) {
        L.verboseInternal = verboseInternal;
    }

    static void vi(String message){
        if (!verboseInternal) return;
        Log.d("ONAM/internal",message);
    }
    static void v(String message){
        if (!verbose) return;
        Log.d("ONAM/verbose",message);
    }
    static void d(String message){
        if (!debug) return;
        Log.d("ONAM/debug",message);
    }
    static void w(String message){
        if (!warning) return;
        Log.d("ONAM/warning",message);
    }
    static void e(String message){
        if (!error) return;
        Log.d("ONAM/error",message);
    }
}
