package com.alboteanu.myapplicationdata.others;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Contact;

import static com.alboteanu.myapplicationdata.R.drawable.shape_oval;

public class ContactHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public ImageView clepsidra;
    public TextView letterIcon;

    public ContactHolder(View itemView) {
        super(itemView);

        letterIcon = (TextView) itemView.findViewById(R.id.textViewLetter);
        name = (TextView) itemView.findViewById(R.id.name);
        clepsidra = (ImageView) itemView.findViewById(R.id.clepsidra_empty_icon);
    }

    public void bindContact(Contact contact) {
        name.setText(contact.name);
    }

}
