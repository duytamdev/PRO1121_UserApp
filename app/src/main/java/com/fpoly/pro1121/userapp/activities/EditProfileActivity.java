package com.fpoly.pro1121.userapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.fpoly.pro1121.userapp.R;
import com.fpoly.pro1121.userapp.utils.Utils;
import com.fpoly.pro1121.userapp.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextInputLayout tilName, tilPhone, tilLocation;
    CircleImageView imgAvt;
    EditText edtName, edtPhone, edtLocation;
    Button btnUpdate;
    String urlImageSelected="";
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        try {
                            ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
                            progressDialog.setMessage("loading....");
                            progressDialog.show();

                            assert data != null;
                            Uri uriImage = data.getData();
                            imgAvt.setImageURI(uriImage); // dom
                            StorageReference ref = FirebaseStorage.getInstance().getReference().child("imagesUser").child(UUID.randomUUID().toString());
                            UploadTask uploadTask = ref.putFile(uriImage);


                            Task<Uri> uriTask = uploadTask.continueWithTask(task -> {
                                if (!task.isSuccessful()) {
                                    throw Objects.requireNonNull(task.getException());
                                }

                                return ref.getDownloadUrl();
                            }).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    urlImageSelected = task.getResult().toString();
                                    progressDialog.dismiss();
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private User userCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getDataFireBase();
        initUI();
        initToolbar();
        actionUpdate();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_edit_profile);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_keyboard_backspace_24);
        toolbar.setTitle("Edit Profile");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private void actionUpdate() {
        Utils.addTextChangedListener(edtName, tilName, false);
        Utils.addTextChangedListener(edtPhone, tilPhone, false);
        Utils.addTextChangedListener(edtLocation, tilLocation, false);
        imgAvt.setOnClickListener(view -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            activityResultLauncher.launch(photoPickerIntent);
        });
        btnUpdate.setOnClickListener(view -> {
            try {
                String name = edtName.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String location = edtLocation.getText().toString().trim();
                String urlImage = urlImageSelected;
                if (name.isEmpty() || phone.isEmpty() || location.isEmpty()) {
                    return;
                }
                if (tilName.getError() != null || tilPhone.getError() != null || tilLocation.getError() != null) {
                    return;
                }
                userCurrentUser.setData(name, location, phone, urlImage);
                updateUserFireBase(userCurrentUser);
            } catch (Exception e) {
                Toast.makeText(EditProfileActivity.this, "Có lỗi " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // cập nhật dữ liệu user: urlImage,name,phoneNumber,location
    private void updateUserFireBase(User userCurrentUser) {
        db.collection("users").document(userCurrentUser.getId())
                // hàm cập nhật các field
                .update("name", userCurrentUser.getName(),
                        "phoneNumber", userCurrentUser.getPhoneNumber(),
                        "urlImage", userCurrentUser.getUrlImage(),
                        "location", userCurrentUser.getLocation())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Cập Nhật Thành Công ", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    // lấy dữ liệu currentUser đỗ lên view
    private void getDataFireBase() {
        db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            try {
                                Map<String, Object> data = document.getData();
                                assert data != null;
                                String id = (String) data.get("id");
                                String name = (String) data.get("name");
                                String urlImage = (String) data.get("urlImage");
                                String location = (String) data.get("location");
                                String phone = (String) data.get("phoneNumber");
                                assert urlImage != null;
                                if (urlImage.length() > 0) {
                                    urlImageSelected = urlImage;
                                }
                                userCurrentUser = new User(id, name, location, phone, urlImage);
                                // đưa dữ liệu lên view
                                DOMUser(userCurrentUser);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void initUI() {
        tilName = findViewById(R.id.til_name_edit_profile);
        tilPhone = findViewById(R.id.til_phone_edit_profile);
        tilLocation = findViewById(R.id.til_location_edit_profile);
        imgAvt = findViewById(R.id.img_EditProfile);
        edtName = findViewById(R.id.edt_name_edt_profile);
        edtPhone = findViewById(R.id.edt_edt_phone_profile);
        edtLocation = findViewById(R.id.edt_location_profile);
        btnUpdate = findViewById(R.id.btn_update_profile);
    }
    // khi lấy được dữ liệu currentUser tiến hành đưa dữ liệu lên views
    private void DOMUser(User user) {
        try {
            edtName.setText(user.getName());
            edtPhone.setText(user.getPhoneNumber());
            edtLocation.setText(user.getLocation());
            if (user.getUrlImage().length() > 0) {
                Glide.with(this)
                        .load(user.getUrlImage())
                        .centerCrop()
                        .into(imgAvt);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}