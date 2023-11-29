package com.chetan.festa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView signupBtn, forgotPwdBtn;
    private Button loginBtn;
    private EditText emailInput, passwordInput;

    private FirebaseAuth mAuth;
    final CustomDialog customDialog = new CustomDialog(LoginActivity.this);

    SharedPreferences sharedPreferences;

    private static final String SHARED_PREF_NAME = "myPref";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        transparentStatusAndNavigation();

        sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        String stored_email = sharedPreferences.getString(KEY_EMAIL, null);
        String stored_username = sharedPreferences.getString(KEY_USERNAME, null);

        if(stored_email != null && stored_username != null){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        emailInput = (EditText) findViewById(R.id.emailInputLogin);
        passwordInput = (EditText) findViewById(R.id.passwordInputLogin);
        loginBtn = (Button) findViewById(R.id.loginBtnLogin);
        forgotPwdBtn = (TextView) findViewById(R.id.forgotPasswordBtnLogin);
        signupBtn = (TextView) findViewById(R.id.signupBtnLogin);

        loginBtn.setOnClickListener(this);
        forgotPwdBtn.setOnClickListener(this);
        signupBtn.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginBtnLogin:
                onLoginPress();
                break;
            case R.id.forgotPasswordBtnLogin:
                forForgotPasswordPress();
                break;
            case R.id.signupBtnLogin:
                forRegisterPress();
                break;
        }
    }

    private void onLoginPress() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email is required!");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please provide valid email!");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required!");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Min password length should be 6 characters!");
            passwordInput.requestFocus();
            return;
        }

        customDialog.startLoadingDialog();
        // login user to firabse
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    String UID = mAuth.getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference().child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String email_database = snapshot.child("email").getValue().toString();
                                String username_database = snapshot.child("username").getValue().toString();

                                // set data o sharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(KEY_EMAIL, email_database);
                                editor.putString(KEY_USERNAME, username_database);
                                editor.apply();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    customDialog.stopLoadingDialog();
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (task.getException().getMessage() == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") {
                        // not connected to internet
                        Toast.makeText(LoginActivity.this, "You are not connected to internet!", Toast.LENGTH_LONG).show();
                    } else if (task.getException().getMessage() == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                        // not found user with that email
                        Toast.makeText(LoginActivity.this, "User is not registerd with this email!", Toast.LENGTH_LONG).show();
                    } else if (task.getException().getMessage() == "The password is invalid or the user does not have a password.") {
                        // password is incorrect
                        Toast.makeText(LoginActivity.this, "Your password is incorrect!", Toast.LENGTH_LONG).show();
                    }
                    Log.e(LOG_TAG, "Error ==> " + task.getException().getMessage());
                    customDialog.stopLoadingDialog();
                }
            }
        });
    }

    private void forForgotPasswordPress() {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void forRegisterPress() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}