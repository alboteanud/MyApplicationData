package com.alboteanu.myapplicationdata.models;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.Utils;

public class ContactHolder extends RecyclerView.ViewHolder {
    public ImageView sandglass;
    public ImageView bin;
    public CheckBox checkBox;
    private TextView name;
    private TextView letterIcon;

    public ContactHolder(@NonNull View contact_layout) {
        super(contact_layout);
        letterIcon = (TextView) contact_layout.findViewById(R.id.textViewLetter);
        name = (TextView) contact_layout.findViewById(R.id.contactNameText);
        sandglass = (ImageView) contact_layout.findViewById(R.id.icon_sandglass);
        bin = (ImageView) contact_layout.findViewById(R.id.icon_bin);
        checkBox = (CheckBox) contact_layout.findViewById(R.id.checkBoxSelect);
    }

    public void bindContact(@NonNull Contact contact) {
        name.setText(contact.name);
        final int color = Utils.getColorFromString(contact.name);
        letterIcon.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        letterIcon.setText(contact.name.substring(0, 1));
    }

}
