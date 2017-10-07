package com.basilgregory.onam.android;

import android.util.Log;

/**
 * Created by donpeter on 9/7/17.
 */

class L {

    private static L lInstance = null;

    private L() {
        warning = true;
        error = true;
        verbose = false;
        verboseInternal = false;
        debug = false;
    }

    static L getInstance() {
        return lInstance == null ? new L() : lInstance;
    }

    private static boolean verbose, debug, warning, error, verboseInternal;

    void setDebug(boolean debug) {
        this.debug = debug;
    }

    void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    void setVerboseInternal(boolean verboseInternal) {
        this.verboseInternal = verboseInternal;
    }

    static void vi(String message){
        if (!verboseInternal || message == null || message.isEmpty()) return;
        Log.d("ONAM/internal",""+message);
    }
    static void v(String message){
        if (!verbose || message == null || message.isEmpty()) return;
        Log.d("ONAM/verbose",""+message);
    }
    static void d(String message){
        if (!debug || message == null || message.isEmpty()) return;
        Log.d("ONAM/debug",""+message);
    }
    static void w(String message){
        if (!warning || message == null || message.isEmpty()) return;
        Log.d("ONAM/warning",""+message);
    }
    static void e(String message){
        if (!error || message == null || message.isEmpty()) return;
        Log.d("ONAM/error",""+message);
    }
    static void e(Exception e){
        if (!error || e == null) return;
        e.printStackTrace();
    }
}
