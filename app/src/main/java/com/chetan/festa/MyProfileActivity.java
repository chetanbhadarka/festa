package com.chetan.festa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

public class MyProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton backBtn;
    private TextView editBtn, profileName;
    private ImageView profileImage;
    private RecyclerView recyclerView;

    public String porfile_url = "null";

    MyProflieRecyclerAdapter myProflieRecyclerAdapter;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    public int getImage(String imageName) {
        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", this.getPackageName());
        return drawableResourceId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        transparentStatusAndNavigation();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users");

        backBtn = findViewById(R.id.myProfile_back_button);
        editBtn = findViewById(R.id.myProfile_edit_button);
        profileImage = findViewById(R.id.myProfile_image);
        profileName = findViewById(R.id.myProfile_username);
        recyclerView = findViewById(R.id.myProfile_recyclerView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(null);

        String userUid = mAuth.getCurrentUser().getUid();
        databaseReference.child(userUid).addValueEventListener(new ValueEventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username_data = snapshot.child("username").getValue().toString();
                boolean haveProfile = snapshot.hasChild("profileImage");
                if (haveProfile == true) {
                    porfile_url = snapshot.child("profileImage").getValue().toString();
                    RequestOptions options = new RequestOptions()
                            .fitCenter()
                            .placeholder(getImage("ic_user"))
                            .error(getImage("ic_user"));
                    Glide.with(getApplicationContext()).load(porfile_url).apply(options).into(profileImage);
                }
                profileName.setText(username_data);
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(LOG_TAG, "Error getting data" + error.getMessage());
            }
        });

        DatabaseReference postsRef = database.getReference().child("Posts");
        Query query = postsRef.orderByChild("userUid").equalTo(userUid);
        FirebaseRecyclerOptions<MyProfileModal> firebasePostsData = new FirebaseRecyclerOptions.Builder<MyProfileModal>().setQuery(query, MyProfileModal.class).build();

        myProflieRecyclerAdapter = new MyProflieRecyclerAdapter(firebasePostsData, getApplicationContext());
        recyclerView.setAdapter(myProflieRecyclerAdapter);

        backBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.myProfile_back_button:
                finish();
                break;
            case R.id.myProfile_edit_button:
                onEditBtnPress();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        myProflieRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myProflieRecyclerAdapter.stopListening();
    }

    private void onEditBtnPress() {
        Intent intent = new Intent(MyProfileActivity.this, EditMyProfileActivity.class);
        intent.putExtra("username", profileName.getText().toString());
        intent.putExtra("image", porfile_url);
        startActivity(intent);
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