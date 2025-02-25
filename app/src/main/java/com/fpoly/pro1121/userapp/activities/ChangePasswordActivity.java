package com.fpoly.pro1121.userapp.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fpoly.pro1121.userapp.R;
import com.fpoly.pro1121.userapp.utils.Utils;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextInputLayout tilOldPassword, tilNewPassword, tilReNewPassword;
    EditText edtOldPassword, edtNewPassword, edtReNewPassword;
    Button btnUpdatePassword;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        initUI();
        initToolbar();
        events();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_change_password);
        toolbar.setTitle("Change Password");
        toolbar.setNavigationIcon(R.drawable.ic_baseline_keyboard_backspace_24);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

    }

    private void events() {
        Utils.addTextChangedListenerPass(edtOldPassword, tilOldPassword);
        Utils.addTextChangedListenerPass(edtNewPassword, tilNewPassword);
        Utils.addTextChangedListenerPass(edtReNewPassword, tilReNewPassword);
        btnUpdatePassword.setOnClickListener(view -> {
            String oldPassword = edtOldPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String reNewPassword = edtReNewPassword.getText().toString().trim();
            if (oldPassword.isEmpty() || newPassword.isEmpty() || reNewPassword.isEmpty()) {
                return;
            }
            if (tilOldPassword.getError() != null || tilNewPassword.getError() != null || tilReNewPassword.getError() != null) {
                return;
            }
            if (!newPassword.equals(reNewPassword)) {
                edtNewPassword.setError("Password không trùng khớp");
                edtReNewPassword.setError("Password không trùng khớp");
                return;
            }
            onChangePassword(oldPassword, newPassword);
        });
    }

    private void initUI() {
        tilOldPassword = findViewById(R.id.til_old_password);
        tilNewPassword = findViewById(R.id.til_new_password);
        tilReNewPassword = findViewById(R.id.til_re_new_password);
        edtNewPassword = findViewById(R.id.edt_new_password);
        edtOldPassword = findViewById(R.id.edt_old_password);
        edtReNewPassword = findViewById(R.id.edt_re_new_password);
        btnUpdatePassword = findViewById(R.id.btn_update_password);
    }

    public void onChangePassword(String oldPassword, final String newPassword) {
        ProgressDialog progressDialog = new ProgressDialog(ChangePasswordActivity.this);
        progressDialog.setMessage("Changing password...");
        progressDialog.show();

        String currentEmail = Objects.requireNonNull(firebaseUser.getEmail());
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, oldPassword);

        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseUser.updatePassword(newPassword)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this, "Password was changed successfully", Toast.LENGTH_LONG).show();
                                        new Handler().postDelayed(() -> {
                                            finish();
                                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                                        }, 1500);
                                    }
                                    progressDialog.dismiss();
                                });
                    } else {
                        tilOldPassword.setError("Mật khẩu cũ không chính xác");
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}