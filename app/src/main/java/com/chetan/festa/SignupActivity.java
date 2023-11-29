package com.chetan.festa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.google.firebase.database.FirebaseDatabase;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView loginBtn;
    private EditText emailInput, passwordInput, confirmPasswordInput;
    private Button registerBtn;

    private FirebaseAuth mAuth;
    final CustomDialog customDialog = new CustomDialog(SignupActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        transparentStatusAndNavigation();

        mAuth = FirebaseAuth.getInstance();

        emailInput = (EditText) findViewById(R.id.emailInputRegister);
        passwordInput = (EditText) findViewById(R.id.passwordInputRegister);
        confirmPasswordInput = (EditText) findViewById(R.id.confirmPasswordInputRegister);
        registerBtn = (Button) findViewById(R.id.signupBtnRegister);
        loginBtn = (TextView) findViewById(R.id.loginBtnRegister);

        registerBtn.setOnClickListener(this);
        loginBtn.setOnClickListener(this);

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
            case R.id.signupBtnRegister:
                onRegisterPress();
                break;
            case R.id.loginBtnRegister:
                forLoginPress();
                break;
        }
    }

    private void onRegisterPress() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

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

        // create username vaiable after getting email string, otherwise it gives substring error
        String username = email.substring(0, email.indexOf("@"));

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

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Confirm Password is required!");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (confirmPassword.length() < 6) {
            confirmPasswordInput.setError("Min confirm password length should be 6 characters!");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (!confirmPassword.equals(password)) {
            confirmPasswordInput.setError("Password and confirm password does not match!");
            confirmPasswordInput.requestFocus();
            return;
        }

        customDialog.startLoadingDialog();
        // register user to firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            User user = new User(username, email);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignupActivity.this, "User is register successfully!", Toast.LENGTH_LONG).show();
                                                customDialog.stopLoadingDialog();
                                                finish();
                                            } else {
                                                Toast.makeText(SignupActivity.this, "Failed to register! try again!", Toast.LENGTH_LONG).show();
                                                customDialog.stopLoadingDialog();
                                            }
                                        }
                                    });
                        } else {
                            if (task.getException().getMessage() == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") {
                                // not connected to internet
                                Toast.makeText(SignupActivity.this, "You are not connected to internet!", Toast.LENGTH_LONG).show();
                            } else if (task.getException().getMessage() == "The email address is already in use by another account.") {
                                // email already have an account
                                Toast.makeText(SignupActivity.this, "This email address already have an account!", Toast.LENGTH_LONG).show();
                            } else {
                                // fail to register
                                Toast.makeText(SignupActivity.this, "Failed to register!", Toast.LENGTH_LONG).show();
                            }
                            Log.e(LOG_TAG,"SignupActivity Error ==> "+task.getException().getMessage());
                            customDialog.stopLoadingDialog();
                        }
                    }
                });
    }

    private void forLoginPress() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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