package com.chetan.festa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private Button changePWD;
    private ImageButton backBtn;
    private EditText cuurentPwdIput, pwdInput, confirmPwdInput;

    private FirebaseAuth mAuth;
    final CustomDialog customDialog = new CustomDialog(SettingsActivity.this);

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        transparentStatusAndNavigation();

        mAuth = FirebaseAuth.getInstance();

        backBtn = (ImageButton) findViewById(R.id.setting_screen_back_button);
        changePWD = (Button) findViewById(R.id.changePasswordButtonSetting);
        cuurentPwdIput = (EditText) findViewById(R.id.currentPasswordInputSetting);
        pwdInput = (EditText) findViewById(R.id.newPasswordInputSetting);
        confirmPwdInput = (EditText) findViewById(R.id.confirmNewPasswordInputSetting);

        backBtn.setOnClickListener(this);
        changePWD.setOnClickListener(this);

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
            case R.id.setting_screen_back_button:
                finish();
                break;
            case R.id.changePasswordButtonSetting:
                onChangePasswordPress();
                break;
        }
    }

    private void onChangePasswordPress() {
        String oldPassword = cuurentPwdIput.getText().toString().trim();
        String password = pwdInput.getText().toString().trim();
        String confirmPassword = confirmPwdInput.getText().toString().trim();
        String email = mAuth.getCurrentUser().getEmail().toString().trim();

        if (oldPassword.isEmpty()) {
            cuurentPwdIput.setError("Current Password is required!");
            cuurentPwdIput.requestFocus();
            return;
        }

        if (oldPassword.length() < 6) {
            cuurentPwdIput.setError("Min password length should be 6 characters!");
            cuurentPwdIput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            pwdInput.setError("New Password is required!");
            pwdInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            pwdInput.setError("Min password length should be 6 characters!");
            pwdInput.requestFocus();
            return;
        }

        if (password.equals(oldPassword)) {
            pwdInput.setError("New password is same as old password! Please use another password!");
            pwdInput.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPwdInput.setError("Confirm new Password is required!");
            confirmPwdInput.requestFocus();
            return;
        }

        if (confirmPassword.length() < 6) {
            confirmPwdInput.setError("Min password length should be 6 characters!");
            confirmPwdInput.requestFocus();
            return;
        }

        if (!confirmPassword.equals(password)) {
            confirmPwdInput.setError("New password and confirm new password does not match!");
            confirmPwdInput.requestFocus();
            return;
        }

        customDialog.startLoadingDialog();

        // remove focus from edit-text
        cuurentPwdIput.clearFocus();
        pwdInput.clearFocus();
        confirmPwdInput.clearFocus();

        // hide keyboard if it is opened
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        // firebase event for change password
        FirebaseUser user = mAuth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // re-authenticate success
                        // update new password
                        user.updatePassword(password)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // password update success
                                        Toast.makeText(SettingsActivity.this, "Password changed successfully!", Toast.LENGTH_LONG).show();
                                        cuurentPwdIput.setText("");
                                        pwdInput.setText("");
                                        confirmPwdInput.setText("");
                                        customDialog.stopLoadingDialog();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @SuppressLint("RestrictedApi")
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // password update failure
                                        if (e.getMessage() == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") {
                                            // not connected to internet
                                            Toast.makeText(SettingsActivity.this, "You are not connected to internet!", Toast.LENGTH_LONG).show();
                                        } else {
                                            // something went wrong
                                            Toast.makeText(SettingsActivity.this, "Failed to change password! Please try again!", Toast.LENGTH_LONG).show();
                                            Log.e(LOG_TAG, "password update failure ===> " + e.getMessage());
                                        }
                                        customDialog.stopLoadingDialog();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // re-authenticate failure
                        if (e.getMessage() == "A network error (such as timeout, interrupted connection or unreachable host) has occurred.") {
                            // not connected to internet
                            Toast.makeText(SettingsActivity.this, "You are not connected to internet!", Toast.LENGTH_LONG).show();
                        } else if (e.getMessage() == "The password is invalid or the user does not have a password.") {
                            // current password entered wrong
                            cuurentPwdIput.setError("Current Password is wrong!");
                            cuurentPwdIput.requestFocus();
                            Toast.makeText(SettingsActivity.this, "Current password is wrong! Please enter correct password.", Toast.LENGTH_LONG).show();
                        }
                        customDialog.stopLoadingDialog();
                    }
                });

    }
}