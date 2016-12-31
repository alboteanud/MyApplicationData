package com.alboteanu.myapplicationdata.others;

import android.animation.Animator;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

/**
 * Created by albot on 20.12.2016.
 */

public class MyAnimationListener  implements Animation.AnimationListener {
    private View view;
    private Handler handler;
    private int visibility;


    public MyAnimationListener(View v, int visibility) {
        this.view = v;
        this.visibility = visibility;
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
