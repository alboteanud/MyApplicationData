package com.alboteanu.myapplicationdata.others;

import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;


public class MyLayoutManager extends LinearLayoutManager {
    Parcelable savedState;

    public MyLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (savedState == null) {
            savedState = state;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onRestoreInstanceState(savedState);
                }
            }, 300);
        }
    }

}
