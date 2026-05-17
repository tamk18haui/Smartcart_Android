package com.gr6.smartcart_android.buyer.account.profile;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.account.profile.response.ProfileResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.gr6.smartcart_android.common.cloudinary.CloudinaryRepository;

public class ProfileActivity extends BaseActivity {

    private ImageView imgBack;
    private ImageView imgAvatarPreview;

    private TextView txtHeaderName;
    private TextView txtHeaderEmail;
    private TextView txtRole;
    private TextView btnSaveProfile;

    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPhoneNumber;
    private EditText edtAvatarUrl;

    private ProfileViewModel viewModel;
    private ProfileResponse currentProfile;

    private CloudinaryRepository cloudinaryRepository;
    private ActivityResultLauncher<String> pickImageLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        cloudinaryRepository = new CloudinaryRepository();
        setupImagePicker();

        initViews();
        initEvents();
        observeData();

        viewModel.loadProfile();
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;

                    imgAvatarPreview.setImageURI(uri);
                    uploadAvatarToCloudinary(uri);
                }
        );
    }

    private void uploadAvatarToCloudinary(Uri uri) {
        showLoading();

        cloudinaryRepository.uploadImage(this, uri, new CloudinaryRepository.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    hideLoading();

                    edtAvatarUrl.setText(imageUrl);
                    ImageLoader.loadCircle(ProfileActivity.this, imageUrl, imgAvatarPreview);

                    showToast("Upload ảnh thành công");
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    hideLoading();
                    showLongToast(message);
                });
            }
        });
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        imgAvatarPreview = findViewById(R.id.imgAvatarPreview);

        txtHeaderName = findViewById(R.id.txtHeaderName);
        txtHeaderEmail = findViewById(R.id.txtHeaderEmail);
        txtRole = findViewById(R.id.txtRole);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);
        edtAvatarUrl = findViewById(R.id.edtAvatarUrl);

        edtEmail.setEnabled(false);
    }

    private void initEvents() {
        imgBack.setOnClickListener(v -> finish());
        imgAvatarPreview.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSaveProfile.setOnClickListener(v -> validateAndUpdate());

        edtAvatarUrl.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                previewAvatar();
            }
        });
    }

    private void observeData() {
        viewModel.getProfileState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                currentProfile = state.getData();
                bindProfile(currentProfile);
            } else {
                showLongToast(state.getMessage());
            }
        });

        viewModel.getUpdateState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                currentProfile = state.getData();
                bindProfile(currentProfile);
                saveToSession(currentProfile);
                showToast("Cập nhật tài khoản thành công");
                finish();
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void bindProfile(ProfileResponse profile) {
        if (profile == null) return;

        edtFullName.setText(profile.getFullName());
        edtEmail.setText(profile.getEmail());
        edtPhoneNumber.setText(profile.getPhoneNumber());
        edtAvatarUrl.setText(profile.getAvatarUrl());

        txtHeaderName.setText(
                isEmpty(profile.getFullName()) ? "Người dùng SmartCart" : profile.getFullName()
        );

        txtHeaderEmail.setText(
                isEmpty(profile.getEmail()) ? "Chưa có email" : profile.getEmail()
        );

        txtRole.setText(
                isEmpty(profile.getRole()) ? "BUYER" : profile.getRole().toUpperCase()
        );

        loadAvatar(profile.getAvatarUrl());
    }

    private void validateAndUpdate() {
        String fullName = edtFullName.getText().toString().trim();
        String phoneNumber = edtPhoneNumber.getText().toString().trim();
        String avatarUrl = edtAvatarUrl.getText().toString().trim();

        if (isEmpty(fullName)) {
            edtFullName.requestFocus();
            showToast("Vui lòng nhập họ tên");
            return;
        }

        if (!isValidPhone(phoneNumber)) {
            edtPhoneNumber.requestFocus();
            showToast("Số điện thoại phải có 10 số và bắt đầu bằng 0");
            return;
        }

        viewModel.updateProfile(fullName, phoneNumber, avatarUrl);
    }

    private void previewAvatar() {
        String avatarUrl = edtAvatarUrl.getText().toString().trim();
        loadAvatar(avatarUrl);
    }

    private void loadAvatar(String avatarUrl) {
        if (isEmpty(avatarUrl)) {
            imgAvatarPreview.setImageResource(R.drawable.ic_user);
        } else {
            ImageLoader.loadCircle(this, avatarUrl, imgAvatarPreview);
        }
    }

    private void saveToSession(ProfileResponse profile) {
        if (profile == null) return;

        UserSession session = UserSession.getInstance(this);
        session.saveFullName(profile.getFullName());
        session.saveEmail(profile.getEmail());
        session.saveAvatarUrl(profile.getAvatarUrl());
        session.saveRole(profile.getRole());
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^0\\d{9}$");
    }
}
