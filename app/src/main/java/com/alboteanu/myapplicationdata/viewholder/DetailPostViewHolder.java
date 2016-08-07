package com.alboteanu.myapplicationdata.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.PostDetails;

public class DetailPostViewHolder extends RecyclerView.ViewHolder {

    public EditText text1;
    public EditText text2;

    public DetailPostViewHolder(View itemView) {
        super(itemView);

        text1 = (EditText) itemView.findViewById(R.id.edit_text1);
        text2 = (EditText) itemView.findViewById(R.id.edit_text2);
    }

    public void bindToDetailPost(PostDetails postDetails) {
        text1.setText(postDetails.text1);
        text2.setText(postDetails.text2);
    }
}
