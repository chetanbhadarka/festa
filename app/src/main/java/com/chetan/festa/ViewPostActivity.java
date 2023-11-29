package com.chetan.festa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

public class ViewPostActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView userImage, postImage;
    TextView userName, likeCount, timeStamp;
    ImageButton likeBtn, backBtn;

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", this.getPackageName());
        return drawableResourceId;
    }

    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        transparentStatusAndNavigation();

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Posts");

        userImage = findViewById(R.id.view_post_user_image);
        userName = findViewById(R.id.view_post_username_text);
        postImage = findViewById(R.id.view_post_postImage);
        likeBtn = findViewById(R.id.view_post_like_button);
        likeCount = findViewById(R.id.view_post_like_count);
        timeStamp = findViewById(R.id.view_post_timestamp_text);
        backBtn = findViewById(R.id.view_post_back_button);

        likeBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String user_profile = extras.getString("userImage");
            String user_name = extras.getString("userName");
            String post_image = extras.getString("postImage");
            int like_count = extras.getInt("likeCount");
            long time_stamp = extras.getLong("timeStamp");

            if (user_profile != "null") {
                Glide
                        .with(this)
                        .load(user_profile)
                        .placeholder(getImage("ic_user"))
                        .error(getImage("ic_user"))
                        .fitCenter()
                        .into(userImage);
            }
            userName.setText(user_name);

            Glide.with(this).load(post_image).fitCenter().into(postImage);

            likeCount.setText(String.valueOf(like_count));

            Date date = new Date(time_stamp);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy hh:mm:ss a");
            timeStamp.setText(simpleDateFormat.format(date));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_post_like_button:
                onLikeButtonPress();
                break;
            case R.id.view_post_back_button:
                finish();
                break;
        }
    }

    private void onLikeButtonPress() {
        Bundle extras = getIntent().getExtras();
        String post_uid = extras.getString("postUid");

        databaseReference.child(post_uid).child("likesCount")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int like_Count = Integer.parseInt(snapshot.getValue().toString());
                        likeCount.setText(String.valueOf(like_Count + 1));

                        databaseReference.child(post_uid).child("likesCount").setValue(like_Count + 1);
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(LOG_TAG, "error  ===> " + error.getMessage());
                    }
                });
    }

    private void transparentStatusAndNavigation() {
        //make full transparent statusBar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private void setWindowFlag(final int bits, boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

}