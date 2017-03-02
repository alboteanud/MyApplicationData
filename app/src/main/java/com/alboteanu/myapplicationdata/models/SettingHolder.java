package com.alboteanu.myapplicationdata.models;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;

public class SettingHolder extends RecyclerView.ViewHolder {
    private final TextView textView1;
    private final TextView textView2;


    public SettingHolder(@NonNull View layout) {
        super(layout);
        textView1 = (TextView) layout.findViewById(R.id.text1);
        textView2 = (TextView) layout.findViewById(R.id.text2);

    }

    public void bindView(SettingModel settingModel) {
        textView1.setText(settingModel.title);
        textView2.setText(settingModel.custom_message);


    }



}
