package com.chetan.festa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton backBtn;
    private TextView profileName;
    private ImageView profileImage;
    private RecyclerView recyclerView;

    UserProfileRecyclerAdapter userProfileRecyclerAdapter;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", this.getPackageName());
        return drawableResourceId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        transparentStatusAndNavigation();

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Posts");

        backBtn = findViewById(R.id.userProfile_back_button);
        profileImage = findViewById(R.id.userProfile_image);
        profileName = findViewById(R.id.userProfile_username);
        recyclerView = findViewById(R.id.userProfile_recyclerView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String user_name = extras.getString("username");
            String user_profile = extras.getString("image");

            if (user_name != null) profileName.setText(user_name);
            if (user_profile != "null") {
                RequestOptions options = new RequestOptions()
                        .fitCenter()
                        .placeholder(getImage("ic_user"))
                        .error(getImage("ic_user"));
                Glide.with(getApplicationContext()).load(user_profile).apply(options).into(profileImage);
            }
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(null);

        Query query = databaseReference.orderByChild("userUid").equalTo(extras.getString("userUid"));
        FirebaseRecyclerOptions<UserProfileModal> firebasePostsData = new FirebaseRecyclerOptions.Builder<UserProfileModal>().setQuery(query, UserProfileModal.class).build();

        userProfileRecyclerAdapter = new UserProfileRecyclerAdapter(firebasePostsData, getApplicationContext());
        recyclerView.setAdapter(userProfileRecyclerAdapter);

        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.userProfile_back_button:
                finish();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        userProfileRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        userProfileRecyclerAdapter.stopListening();
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