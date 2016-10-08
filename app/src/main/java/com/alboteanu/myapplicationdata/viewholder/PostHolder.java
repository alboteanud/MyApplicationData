package com.alboteanu.myapplicationdata.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Post;

public class PostHolder extends RecyclerView.ViewHolder {

    public TextView text1;
    public TextView text2;

    public PostHolder(View itemView) {
        super(itemView);

        text1 = (TextView) itemView.findViewById(R.id.text1);
        text2 = (TextView) itemView.findViewById(R.id.text2);
    }

    public void bindToPost(Post post) {
        text1.setText(post.text2);
        text2.setText(post.text4);

//     itemView.findViewById(R.id.text2).setVisibility(View.GONE);
    }

    public void bindToPostSimple(Post post) {
        text1.setText(post.text2);
        itemView.findViewById(R.id.text2).setVisibility(View.GONE);
    }
}
