package com.chetan.festa;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class MyProflieRecyclerAdapter extends FirebaseRecyclerAdapter<MyProfileModal, MyProflieRecyclerAdapter.MyHolder> {
    private Context context;

    public MyProflieRecyclerAdapter(@NonNull FirebaseRecyclerOptions<MyProfileModal> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MyHolder holder, int position, @NonNull MyProfileModal model) {
        Glide
                .with(holder.gridImage.getContext())
                .load(model.getPostImage()) // postImage is the grid Image
                .centerCrop()
                .into(holder.gridImage);

        holder.gridImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewPostActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userImage", model.getProfileImage());
                intent.putExtra("userName", model.getUsername());
                intent.putExtra("postImage", model.getPostImage());
                intent.putExtra("postUid", model.getPostUid());
                intent.putExtra("likeCount", model.getLikesCount());
                intent.putExtra("timeStamp", model.getTimeStamp());
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.custom_image_grid_view, parent, false);

        return new MyHolder(view);
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView gridImage;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            gridImage = (ImageView) itemView.findViewById(R.id.image_grid_view_imageView);
        }
    }
}
