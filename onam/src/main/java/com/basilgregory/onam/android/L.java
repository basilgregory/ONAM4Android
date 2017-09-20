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
        info = false;
    }

    public static L getInstance() {
        return lInstance == null ? new L() : lInstance;
    }

    static boolean verbose, info, warning, error;

    public void setVerbose(boolean verboseValue) {
        verbose = verboseValue;
    }

    public void setInfo(boolean infoValue) {
        info = infoValue;
    }

    public void setWarning(boolean warningValue) {
        warning = warningValue;
    }

    public void setError(boolean errorValue) {
        error = errorValue;
    }

    static void d(String message){
        if (!verbose) return;
        Log.d("ONAM/debug",message);
    }
    static void i(String message){
        if (!info) return;
        Log.d("ONAM/info",message);
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
