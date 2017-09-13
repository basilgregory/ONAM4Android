package com.basilgregory.onamsample.listeners;

import android.view.View;

/**
 * Created by donpeter on 9/13/17.
 */

public interface ClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);

}
