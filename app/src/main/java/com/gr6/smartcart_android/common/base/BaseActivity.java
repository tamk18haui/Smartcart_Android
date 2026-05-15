package com.gr6.smartcart_android.common.base;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gr6.smartcart_android.R;

public class BaseActivity extends AppCompatActivity {

    private Dialog loadingDialog;
    private Toast currentToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void showToast(String message) {
        if (message == null || message.trim().isEmpty()) return;

        if (currentToast != null) {
            currentToast.cancel();
        }

        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    protected void showLongToast(String message) {
        if (message == null || message.trim().isEmpty()) return;

        if (currentToast != null) {
            currentToast.cancel();
        }

        currentToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        currentToast.show();
    }

    protected void showLoading() {
        if (isFinishing()) return;

        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);

        FrameLayout container = new FrameLayout(this);
        int size = dp(86);
        container.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        container.setPadding(dp(22), dp(22), dp(22), dp(22));
        container.setBackgroundResource(R.drawable.bg_loading_box);

        ProgressBar progressBar = new ProgressBar(this);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                dp(42),
                dp(42)
        );
        progressParams.gravity = Gravity.CENTER;
        container.addView(progressBar, progressParams);

        loadingDialog.setContentView(container);

        Window window = loadingDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        loadingDialog.show();
    }

    protected void hideLoading() {
        if (loadingDialog == null) return;

        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    protected int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    protected boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}