package com.alboteanu.myapplicationdata.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Contact;

public class PostHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public TextView phone;
    public ImageView ball;

    public PostHolder(View itemView) {
        super(itemView);

        name = (TextView) itemView.findViewById(R.id.name);
        phone = (TextView) itemView.findViewById(R.id.phone);
        ball = (ImageView) itemView.findViewById(R.id.star);
    }

    public void bindToPost(Contact contact) {
        name.setText(contact.name);
        phone.setText(contact.phone);

    }


}
