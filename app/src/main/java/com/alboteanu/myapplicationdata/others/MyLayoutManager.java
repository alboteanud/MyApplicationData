package com.alboteanu.myapplicationdata.others;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;


public class MyLayoutManager extends LinearLayoutManager {
    private static final String tag = "MyLayoutManager";
    private Parcelable state;

    public MyLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        Log.d(tag, "onLayoutCompleted " + state.toString());
        if (this.state != null && state.getItemCount() > 0) {
            onRestoreInstanceState(this.state);
            this.state = null;
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        Log.d(tag, "onRestoreInstanceState ");
        this.state = state;
    }
}
