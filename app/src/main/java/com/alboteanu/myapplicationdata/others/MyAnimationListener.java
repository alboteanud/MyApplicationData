package com.alboteanu.myapplicationdata.others;

import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;

/**
 * Created by albot on 20.12.2016.
 */

public class MyAnimationListener  implements Animation.AnimationListener {
    private final View view;
    private final Handler handler;
    private final int visibility;


    public MyAnimationListener(View v) {
        this.view = v;
        this.visibility = View.GONE;
        handler = new Handler();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        handler.postDelayed( new Runnable() {

            @Override
            public void run() {
                view.setVisibility(visibility);
            }
        }, 1400);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        view.setVisibility(visibility);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
