package com.alboteanu.myapplicationdata.others;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Contact;

import java.util.Calendar;

public class ContactHolder extends RecyclerView.ViewHolder {
    @NonNull
    public final View contact_layout;
    private TextView name;
    @NonNull
    public final ImageView sandglass;
    @NonNull
    public final ImageView bin;
    @NonNull
    private final TextView letterIcon;
    public CheckBox checkBox;


    public ContactHolder(@NonNull View contact_layout) {
        super(contact_layout);
        this.contact_layout = contact_layout;
        letterIcon = (TextView) contact_layout.findViewById(R.id.textViewLetter);
        name = (TextView) contact_layout.findViewById(R.id.contactNameText);
        sandglass = (ImageView) contact_layout.findViewById(R.id.icon_sandglass);
        bin = (ImageView) contact_layout.findViewById(R.id.icon_bin);
        checkBox = (CheckBox) contact_layout.findViewById(R.id.checkBoxSelectContact);

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
