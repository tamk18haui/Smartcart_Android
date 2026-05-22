package com.gr6.smartcart_android.auth.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.auth.register.RegisterActivity;
import com.gr6.smartcart_android.auth.response.LoginResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.common.utils.Validator;
import com.gr6.smartcart_android.navigation.RoleRouterActivity;

public class LoginActivity extends BaseActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private ImageView imgTogglePassword;
    private CheckBox cbRemember;
    private TextView btnLogin;
    private TextView btnGoogle;
    private TextView btnFacebook;
    private TextView txtForgotPassword;
    private TextView txtGoRegister;

    private boolean passwordVisible = false;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        initViews();
        initEvents();
        observeLogin();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        cbRemember = findViewById(R.id.cbRemember);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtGoRegister = findViewById(R.id.txtGoRegister);
    }

    private void initEvents() {
        imgTogglePassword.setOnClickListener(v -> togglePassword());

        btnLogin.setOnClickListener(v -> validateAndLogin());

        btnGoogle.setOnClickListener(v ->
                showToast("Đăng nhập Google sẽ làm sau")
        );

        btnFacebook.setOnClickListener(v ->
                showToast("Đăng nhập Facebook sẽ làm sau")
        );

        txtForgotPassword.setOnClickListener(v ->
                showToast("Quên mật khẩu sẽ làm sau")
        );

        txtGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void observeLogin() {
        loginViewModel.getLoginState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                handleLoginSuccess(state.getData());
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void validateAndLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!Validator.isValidEmail(email)) {
            edtEmail.requestFocus();
            showToast("Email không hợp lệ");
            return;
        }

        if (!Validator.isValidPassword(password)) {
            edtPassword.requestFocus();
            showToast("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        loginViewModel.login(email, password);
    }

    private void handleLoginSuccess(LoginResponse loginResponse) {
        if (loginResponse == null) {
            showToast("Dữ liệu đăng nhập không hợp lệ");
            return;
        }

        String token = loginResponse.getToken();

        if (Validator.isEmpty(token)) {
            showToast("Server chưa trả token");
            return;
        }

        LoginResponse.UserDto user = loginResponse.getUser();

        if (user == null || user.getUserId() == null) {
            showLongToast("Login thành công nhưng chưa nhận được userId. Kiểm tra response API login.");
            android.util.Log.e("LOGIN_USER_ID", "user hoặc userId null");
            return;
        }

        TokenManager.getInstance(this).saveToken(token);

        UserSession session = UserSession.getInstance(this);
        session.saveUserId(user.getUserId());
        session.saveFullName(user.getFullName());
        session.saveEmail(user.getEmail());
        session.saveAvatarUrl(user.getAvatarUrl());

        String role = loginResponse.getRole();

        if (!Validator.isEmpty(role)) {
            session.saveRole(role);
        }

        android.util.Log.d("LOGIN_USER_ID", "Saved userId = " + user.getUserId());

        showToast("Đăng nhập thành công");

        Intent intent = new Intent(this, RoleRouterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void togglePassword() {
        imgTogglePassword.animate()
                .scaleX(0.75f)
                .scaleY(0.75f)
                .alpha(0.55f)
                .rotationBy(12f)
                .setDuration(90)
                .withEndAction(() -> {
                    passwordVisible = !passwordVisible;

                    if (passwordVisible) {
                        edtPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );
                        imgTogglePassword.setImageResource(R.drawable.ic_eye_off);
                    } else {
                        edtPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );
                        imgTogglePassword.setImageResource(R.drawable.ic_eye);
                    }

                    edtPassword.setSelection(edtPassword.getText().length());

                    imgTogglePassword.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .rotation(0f)
                            .setDuration(120)
                            .start();
                })
                .start();
    }
}