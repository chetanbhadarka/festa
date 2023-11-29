package com.chetan.festa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.lang.reflect.Method;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton moreBtn;
    private ProgressBar progressBar;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;

    FirebaseAuth mAuth;
    FirebaseDatabase database;

    HomeRecyclerAdapter homeRecyclerAdapter;
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "myPref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        moreBtn = findViewById(R.id.more_button);
        recyclerView = findViewById(R.id.home_recycler_view);
        floatingActionButton = findViewById(R.id.new_post);
        progressBar = findViewById(R.id.home_screen_progress);

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(null);

        floatingActionButton.setOnClickListener(this);
        moreBtn.setOnClickListener(this);

        Query query = FirebaseDatabase.getInstance().getReference().child("Posts").orderByChild("timeStamp");
        FirebaseRecyclerOptions<HomeModal> firebasePostData = new FirebaseRecyclerOptions.Builder<HomeModal>().setQuery(query, HomeModal.class).build();

        homeRecyclerAdapter = new HomeRecyclerAdapter(firebasePostData, getApplicationContext());
//        progressBar.setVisibility(View.GONE);
//        recyclerView.setVisibility(View.VISIBLE);
//        floatingActionButton.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(homeRecyclerAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.more_button:
                onMoreButtonPress();
                break;
            case R.id.new_post:
                onCreateNewPostPress();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        homeRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        homeRecyclerAdapter.stopListening();
    }

    private void onCreateNewPostPress() {
        Intent intent = new Intent(HomeActivity.this, CreateNewPostActivity.class);
        startActivity(intent);
    }

    private void onMoreButtonPress() {
        PopupMenu popupMenu = new PopupMenu(HomeActivity.this, moreBtn);

        // get menu icons
        try {
            Method method = popupMenu.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            method.setAccessible(true);
            method.invoke(popupMenu.getMenu(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }


        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setGravity(Gravity.END);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.profile_menu:
                        onMyProfilePress();
                        break;
                    case R.id.setting_menu:
                        onSettingPress();
                        break;
                    case R.id.logout_menu:
                        onLogoutPress();
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void onMyProfilePress() {
        Intent intent = new Intent(HomeActivity.this, MyProfileActivity.class);
        startActivity(intent);
    }

    private void onSettingPress() {
        Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void onLogoutPress() {
        FirebaseAuth.getInstance().signOut();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

}