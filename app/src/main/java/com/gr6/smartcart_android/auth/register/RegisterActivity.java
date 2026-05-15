package com.gr6.smartcart_android.auth.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.auth.login.LoginActivity;
import com.gr6.smartcart_android.auth.request.RegisterRequest;
import com.gr6.smartcart_android.auth.request.ShopRegisterRequest;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.Constants;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.common.utils.Validator;

public class RegisterActivity extends BaseActivity {

    private LinearLayout cardBuyer;
    private LinearLayout cardSeller;
    private LinearLayout layoutShopRegister;

    private TextView txtBuyer;
    private TextView txtSeller;
    private TextView btnRegister;
    private TextView txtGoLogin;

    private EditText edtFullName;
    private EditText edtEmail;
    private EditText edtPhone;
    private EditText edtPassword;
    private EditText edtConfirmPassword;

    private EditText edtShopName;
    private EditText edtShopDescription;
    private EditText edtPickupAddress;

    private ImageView imgTogglePassword;
    private ImageView imgToggleConfirmPassword;

    private RegisterViewModel registerViewModel;

    private String selectedRole = Constants.ROLE_BUYER;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        initViews();
        initEvents();
        observeRegister();
        updateRoleUi(false);
    }

    private void initViews() {
        cardBuyer = findViewById(R.id.cardBuyer);
        cardSeller = findViewById(R.id.cardSeller);
        layoutShopRegister = findViewById(R.id.layoutShopRegister);

        txtBuyer = findViewById(R.id.txtBuyer);
        txtSeller = findViewById(R.id.txtSeller);
        btnRegister = findViewById(R.id.btnRegister);
        txtGoLogin = findViewById(R.id.txtGoLogin);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        edtShopName = findViewById(R.id.edtShopName);
        edtShopDescription = findViewById(R.id.edtShopDescription);
        edtPickupAddress = findViewById(R.id.edtPickupAddress);

        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        imgToggleConfirmPassword = findViewById(R.id.imgToggleConfirmPassword);
    }

    private void initEvents() {
        cardBuyer.setOnClickListener(v -> {
            selectedRole = Constants.ROLE_BUYER;
            updateRoleUi(true);
        });

        cardSeller.setOnClickListener(v -> {
            selectedRole = Constants.ROLE_SELLER;
            updateRoleUi(true);
        });

        imgTogglePassword.setOnClickListener(v ->
                togglePasswordField(edtPassword, imgTogglePassword, true)
        );

        imgToggleConfirmPassword.setOnClickListener(v ->
                togglePasswordField(edtConfirmPassword, imgToggleConfirmPassword, false)
        );

        btnRegister.setOnClickListener(v -> validateAndRegister());

        txtGoLogin.setOnClickListener(v -> openLogin());
    }

    private void observeRegister() {
        registerViewModel.getRegisterState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                showLongToast("Đăng ký thành công. Vui lòng đăng nhập.");
                openLogin();
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void updateRoleUi(boolean animate) {
        boolean isSeller = Constants.ROLE_SELLER.equals(selectedRole);

        cardBuyer.setBackgroundResource(isSeller
                ? R.drawable.bg_role_unselected
                : R.drawable.bg_role_selected
        );

        cardSeller.setBackgroundResource(isSeller
                ? R.drawable.bg_role_selected
                : R.drawable.bg_role_unselected
        );

        txtBuyer.setTextColor(ContextCompat.getColor(
                this,
                isSeller ? R.color.text_secondary : R.color.brand_primary
        ));

        txtSeller.setTextColor(ContextCompat.getColor(
                this,
                isSeller ? R.color.brand_primary : R.color.text_secondary
        ));

        if (isSeller) {
            showShopForm(animate);
        } else {
            hideShopForm(animate);
        }

        animateSelectedRole(isSeller ? cardSeller : cardBuyer);
    }

    private void showShopForm(boolean animate) {
        layoutShopRegister.setVisibility(View.VISIBLE);

        if (!animate) return;

        layoutShopRegister.setAlpha(0f);
        layoutShopRegister.setTranslationY(-18f);
        layoutShopRegister.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(180)
                .start();
    }

    private void hideShopForm(boolean animate) {
        if (!animate) {
            layoutShopRegister.setVisibility(View.GONE);
            return;
        }

        if (layoutShopRegister.getVisibility() != View.VISIBLE) return;

        layoutShopRegister.animate()
                .alpha(0f)
                .translationY(-18f)
                .setDuration(140)
                .withEndAction(() -> {
                    layoutShopRegister.setVisibility(View.GONE);
                    layoutShopRegister.setAlpha(1f);
                    layoutShopRegister.setTranslationY(0f);
                })
                .start();
    }

    private void animateSelectedRole(View view) {
        if (view == null) return;

        view.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(70)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start())
                .start();
    }

    private void validateAndRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (Validator.isEmpty(fullName)) {
            edtFullName.requestFocus();
            showToast("Vui lòng nhập họ và tên");
            return;
        }

        if (!Validator.isValidEmail(email)) {
            edtEmail.requestFocus();
            showToast("Email không hợp lệ");
            return;
        }

        if (!Validator.isValidPhone(phone)) {
            edtPhone.requestFocus();
            showToast("Số điện thoại không hợp lệ");
            return;
        }

        if (!Validator.isStrongPassword(password)) {
            edtPassword.requestFocus();
            showLongToast("Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
            return;
        }

        if (!Validator.isPasswordMatch(password, confirmPassword)) {
            edtConfirmPassword.requestFocus();
            showToast("Mật khẩu xác nhận không khớp");
            return;
        }

        if (Constants.ROLE_SELLER.equals(selectedRole)) {
            registerSeller(fullName, email, phone, password, confirmPassword);
        } else {
            registerBuyer(fullName, email, phone, password, confirmPassword);
        }
    }

    private void registerBuyer(
            String fullName,
            String email,
            String phone,
            String password,
            String confirmPassword
    ) {
        RegisterRequest request = new RegisterRequest(
                fullName,
                email,
                phone,
                password,
                confirmPassword
        );

        registerViewModel.registerBuyer(request);
    }

    private void registerSeller(
            String fullName,
            String email,
            String phone,
            String password,
            String confirmPassword
    ) {
        String shopName = edtShopName.getText().toString().trim();
        String shopDescription = edtShopDescription.getText().toString().trim();
        String pickupAddress = edtPickupAddress.getText().toString().trim();

        if (Validator.isEmpty(shopName)) {
            edtShopName.requestFocus();
            showToast("Vui lòng nhập tên shop");
            return;
        }

        if (Validator.isEmpty(shopDescription)) {
            edtShopDescription.requestFocus();
            showToast("Vui lòng nhập mô tả shop");
            return;
        }

        if (Validator.isEmpty(pickupAddress)) {
            edtPickupAddress.requestFocus();
            showToast("Vui lòng nhập địa chỉ lấy hàng");
            return;
        }

        ShopRegisterRequest request = new ShopRegisterRequest(
                fullName,
                email,
                phone,
                password,
                confirmPassword,
                shopName,
                shopDescription,
                pickupAddress
        );

        registerViewModel.registerSeller(request);
    }

    private void togglePasswordField(EditText editText, ImageView imageView, boolean isMainPassword) {
        imageView.animate()
                .scaleX(0.75f)
                .scaleY(0.75f)
                .alpha(0.55f)
                .rotationBy(12f)
                .setDuration(90)
                .withEndAction(() -> {
                    boolean visible;

                    if (isMainPassword) {
                        passwordVisible = !passwordVisible;
                        visible = passwordVisible;
                    } else {
                        confirmPasswordVisible = !confirmPasswordVisible;
                        visible = confirmPasswordVisible;
                    }

                    if (visible) {
                        editText.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );
                        imageView.setImageResource(R.drawable.ic_eye_off);
                    } else {
                        editText.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );
                        imageView.setImageResource(R.drawable.ic_eye);
                    }

                    editText.setSelection(editText.getText().length());

                    imageView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .rotation(0f)
                            .setDuration(120)
                            .start();
                })
                .start();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}