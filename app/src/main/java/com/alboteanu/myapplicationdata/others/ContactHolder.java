package com.alboteanu.myapplicationdata.others;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Contact;

public class ContactHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public ImageView clepsidra;
    public TextView letterIcon;
    public CheckBox checkBox;

    public ContactHolder(View itemView) {
        super(itemView);

        letterIcon = (TextView) itemView.findViewById(R.id.textViewLetter);
        name = (TextView) itemView.findViewById(R.id.name);
        clepsidra = (ImageView) itemView.findViewById(R.id.clepsidra_empty_icon);
        checkBox = (CheckBox) itemView.findViewById(R.id.checkBoxSelectContact);
    }

    public void bindContact(Contact contact, Drawable shape_oval) {
        name.setText(contact.name);

        shape_oval.setColorFilter(Utils.getColorFromString(contact.name), PorterDuff.Mode.SRC_ATOP);
        letterIcon.setBackground(shape_oval);
        letterIcon.setText(contact.name.substring(0, 1));
    }

}
