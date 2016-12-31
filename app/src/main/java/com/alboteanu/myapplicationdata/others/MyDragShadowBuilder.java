package com.alboteanu.myapplicationdata.others;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

public class MyDragShadowBuilder extends View.DragShadowBuilder {

    // The com.alboteanu.myapplicationdata.others.sandGlassDragEventListener shadow image, defined as a drawable thing
    private static Drawable shadow;
    private static Drawable drawable;


    // Defines the constructor for myDragShadowBuilder
    public MyDragShadowBuilder(View v) {

        // Stores the View parameter passed to myDragShadowBuilder.
        super(v);

        // Creates a draggable image that will fill the Canvas provided by the system.
        shadow = new ColorDrawable(Color.LTGRAY);
        ImageView imageView = (ImageView) v;
//        imageView.setColorFilter(Color.BLACK);
        drawable = imageView.getDrawable();
    }

    // Defines a callback that sends the com.alboteanu.myapplicationdata.others.sandGlassDragEventListener shadow dimensions and touch point back to the
    // system.
    @Override
    public void onProvideShadowMetrics (@NonNull Point size, @NonNull Point touch) {
        // Defines local variables
        int width, height;

        // Sets the width of the shadow to half the width of the original View
        width = getView().getWidth() ;

        // Sets the height of the shadow to half the height of the original View
        height = getView().getHeight();

        // The com.alboteanu.myapplicationdata.others.sandGlassDragEventListener shadow is a ColorDrawable. This sets its dimensions to be the same as the
        // Canvas that the system will provide. As a result, the com.alboteanu.myapplicationdata.others.sandGlassDragEventListener shadow will fill the
        // Canvas.
        shadow.setBounds(0, 0, width, height);
        drawable.setBounds(0, 0, width, height);

        // Sets the size parameter's width and height values. These get back to the system
        // through the size parameter.
        size.set(width, height);

        // Sets the touch point's position to be in the middle of the com.alboteanu.myapplicationdata.others.sandGlassDragEventListener shadow
        touch.set(width / 2, height / 2);
    }

    // Defines a callback that draws the com.alboteanu.myapplicationdata.others.sandGlassDragEventListener shadow in a Canvas that the system constructs
    // from the dimensions passed in onProvideShadowMetrics().
    @Override
    public void onDrawShadow(@NonNull Canvas canvas) {

        // Draws the ColorDrawable in the Canvas passed in from the system.
        shadow.draw(canvas);
        drawable.draw(canvas);
    }
}
