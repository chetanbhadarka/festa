package com.chetan.festa;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PageFetcherSnapshotState;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

public class HomeRecyclerAdapter extends FirebaseRecyclerAdapter<HomeModal, HomeRecyclerAdapter.MyHolder> {
    private Context context;

    public int getImage(String imageName) {
        int drawableResourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        return drawableResourceId;
    }

    public HomeRecyclerAdapter(@NonNull FirebaseRecyclerOptions<HomeModal> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull MyHolder holder, int position, @NonNull HomeModal model) {
        holder.userName.setText(model.getUsername());
        holder.likesCount.setText(String.valueOf(model.getLikesCount()));
        Date date = new Date(model.getTimeStamp());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy hh:mm:ss a");
        holder.timeStamp.setText(simpleDateFormat.format(date));
        if (model.getProfileImage() != "null") {
            Glide
                    .with(holder.userImage.getContext())
                    .load(model.getProfileImage())
                    .placeholder(getImage("ic_user"))
                    .error(getImage("ic_user"))
                    .fitCenter()
                    .into(holder.userImage);
        }
        Glide
                .with(holder.postImage.getContext())
                .load(model.getPostImage())
                .fitCenter()
                .into(holder.postImage);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference("Posts");

        String postUid = model.getPostUid();

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child(postUid).child("likesCount").setValue(model.getLikesCount() + 1);
            }
        });
        holder.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("username", model.getUsername());
                intent.putExtra("image", model.getProfileImage()); // user Image
                intent.putExtra("userUid", model.getUserUid());
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.custom_post_view, parent, false);

        return new MyHolder(view);
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView userImage, postImage;
        TextView userName, likesCount, timeStamp;
        ImageButton likeButton;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            userImage = (ImageView) itemView.findViewById(R.id.post_view_user_image);
            userName = (TextView) itemView.findViewById(R.id.post_view_username_text);
            postImage = (ImageView) itemView.findViewById(R.id.post_view_post_image);
            likesCount = (TextView) itemView.findViewById(R.id.post_view_like_count);
            timeStamp = (TextView) itemView.findViewById(R.id.post_view_timestamp_text);
            likeButton = (ImageButton) itemView.findViewById(R.id.post_view_like_button);
        }
    }
}