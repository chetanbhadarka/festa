package com.chetan.festa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateNewPostActivity extends AppCompatActivity implements View.OnClickListener {

    public ImageButton backBtn;
    public ImageView imageView;
    public Button chooseImage, savePostBtn, canclePostBtn;

    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 100;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_post);
        transparentStatusAndNavigation();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        backBtn = findViewById(R.id.create_post_screen_back_button);
        chooseImage = findViewById(R.id.create_post_screen_choose_button);
        savePostBtn = findViewById(R.id.create_post_screen_save_button);
        canclePostBtn = findViewById(R.id.create_post_screen_cancle_button);
        imageView = findViewById(R.id.create_post_screen_image_view);

        backBtn.setOnClickListener(this);
        chooseImage.setOnClickListener(this);
        savePostBtn.setOnClickListener(this);
        canclePostBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_post_screen_back_button:
                onBackPressed();
                break;
            case R.id.create_post_screen_choose_button:
                onChoosePhotoPress();
                break;
            case R.id.create_post_screen_save_button:
                onPostUploadPress();
                break;
            case R.id.create_post_screen_cancle_button:
                onPostCanclePress();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void onPostUploadPress() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading Post ...");
            progressDialog.show();

            Long tsLong = System.currentTimeMillis();
            String currentUserUid = mAuth.getCurrentUser().getUid();
            StorageReference ref = storageReference.child("posts/" + currentUserUid + "_" + tsLong.toString());

            // uploading post image
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // get download url
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    // get current user
                                    databaseReference.child(currentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String email_data = snapshot.child("email").getValue().toString();
                                            String username_data = snapshot.child("username").getValue().toString();
                                            Boolean haveProfile = snapshot.hasChild("profileImage");
                                            String profileImage_data = "null";
                                            if (haveProfile == true) {
                                                profileImage_data = snapshot.child("profileImage").getValue().toString();
                                            }

                                            // creating blank push for uid
                                            String postUid = database.getReference().child("Posts").push().getKey();

                                            // post data object
                                            CreatePost createPost = new CreatePost(email_data, username_data, currentUserUid, profileImage_data, uri.toString(), 0, ServerValue.TIMESTAMP, postUid.toString());

                                            // creating post document in realtime database
                                            database.getReference().child("Posts").child(postUid).setValue(createPost)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                // adding postId to user
                                                                DatabaseReference postDataRef = databaseReference.child(currentUserUid).child("postData");
                                                                // getting last key of postsUid array from users
                                                                postDataRef
                                                                        .runTransaction(new Transaction.Handler() {
                                                                            @NonNull
                                                                            @Override
                                                                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                                                                String lastKey = "-1";
                                                                                for (MutableData child : currentData.getChildren()) {
                                                                                    lastKey = child.getKey();
                                                                                }

                                                                                String nextKey = String.valueOf(Integer.parseInt(lastKey) + 1);
                                                                                Map<String, Object> map = new HashMap<>();
                                                                                map.put(nextKey, postUid);

                                                                                // adding new postId to existing array of PostData
                                                                                postDataRef.updateChildren(map)
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                progressDialog.dismiss();
                                                                                                Toast.makeText(CreateNewPostActivity.this, "Post Uploaded Successfully!", Toast.LENGTH_LONG).show();
                                                                                                finish();
                                                                                            }
                                                                                        })
                                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                progressDialog.dismiss();
                                                                                                Toast.makeText(CreateNewPostActivity.this, "Post is not added into user's account!", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        });

                                                                                return null;
                                                                            }

                                                                            @Override
                                                                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                                                                progressDialog.dismiss();
                                                                            }
                                                                        });

//
                                                            } else {
                                                                // error while setting-up posts collection data
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progressDialog.dismiss();
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressDialog.dismiss();
                                        }
                                    });

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast.makeText(CreateNewPostActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });

        } else {
            Toast.makeText(this, "Please select image to upload post!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPostCanclePress() {
        onBackPressed();
    }

    private void onChoosePhotoPress() {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                imageView.setBackgroundColor(Color.rgb(255, 255, 255));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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