package com.alboteanu.myapplicationdata.others;

import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;

import static android.R.attr.visibility;


public class MyAnimationListener  implements Animation.AnimationListener {
    private final View view;


    public MyAnimationListener(View v) {
        this.view = v;
    }

    @Override
    public void onAnimationStart(Animation animation) {
        new Handler().postDelayed( new Runnable() {

            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        }, 1000);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        view.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
