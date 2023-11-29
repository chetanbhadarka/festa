package com.chetan.festa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.security.PrivateKey;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailInput;
    private Button resetBtn;
    private TextView backToLogin;

    private FirebaseAuth mAuth;
    final CustomDialog customDialog = new CustomDialog(ForgotPasswordActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        transparentStatusAndNavigation();

        mAuth = FirebaseAuth.getInstance();

        emailInput = (EditText) findViewById(R.id.emailInputForgotPassword);
        resetBtn = (Button) findViewById(R.id.resetPasswordBtnForgotPassword);
        backToLogin = (TextView) findViewById(R.id.loginBtnForgotPassword);

        resetBtn.setOnClickListener(this);
        backToLogin.setOnClickListener(this);
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
            case R.id.resetPasswordBtnForgotPassword:
                onResetPasswordPress();
                break;
            case R.id.loginBtnForgotPassword:
                forLoginPress();
                break;
        }
    }

    private void onResetPasswordPress() {
        String email = emailInput.getText().toString().trim();

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

        customDialog.startLoadingDialog();
        // forgot password
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPasswordActivity.this, "Check your email to reset your password!", Toast.LENGTH_LONG).show();
                    customDialog.stopLoadingDialog();
                    finish();
                }else{
                    if (task.getException().getMessage() == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") {
                        Toast.makeText(ForgotPasswordActivity.this, "You are not connected to internet!", Toast.LENGTH_LONG).show();
                    }else if (task.getException().getMessage() == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                        Toast.makeText(ForgotPasswordActivity.this, "There is no user with this email!", Toast.LENGTH_LONG).show();
                    }
                    Log.e(LOG_TAG,"ForgotPasswordActivity Error ==> "+task.getException().getMessage());
                    customDialog.stopLoadingDialog();
                }
            }
        });
    }

    private void forLoginPress() {
        finish();
    }
}