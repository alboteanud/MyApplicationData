package com.alboteanu.myapplicationdata.models;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.others.Utils;

public class ContactHolder extends RecyclerView.ViewHolder {
    private final TextView name;
    @NonNull
    public final ImageView sandglass;
    @NonNull
    public final ImageView bin;
    @NonNull
    private final TextView letterIcon;
    public final CheckBox checkBox;


    public ContactHolder(@NonNull View contact_layout) {
        super(contact_layout);
        letterIcon = (TextView) contact_layout.findViewById(R.id.textViewLetter);
        name = (TextView) contact_layout.findViewById(R.id.contactNameText);
        sandglass = (ImageView) contact_layout.findViewById(R.id.icon_sandglass);
        bin = (ImageView) contact_layout.findViewById(R.id.icon_bin);
        checkBox = (CheckBox) contact_layout.findViewById(R.id.checkBoxSelect);

    }

    public void bindContact(@NonNull Contact contact, @NonNull Drawable shape_oval) {
        name.setText(contact.name);
        final int color = Utils.getColorFromString(contact.name);
        shape_oval.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        letterIcon.setBackground(shape_oval);
        letterIcon.setText(contact.name.substring(0, 1));
//        contact_layout.setTag(position);
//        bin.setTag(position);
//        checkBox.setTag(position);
//        sandglass.setTag(position);


    }



}
