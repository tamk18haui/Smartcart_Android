package com.gr6.smartcart_android.seller.product;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.math.BigDecimal;

public class ShippingActivity extends BaseActivity {

    private EditText edtWeight;
    private EditText edtLength;
    private EditText edtWidth;
    private EditText edtHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shipping);
        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        edtWeight = findViewById(R.id.edtWeight);
        edtLength = findViewById(R.id.edtLength);
        edtWidth = findViewById(R.id.edtWidth);
        edtHeight = findViewById(R.id.edtHeight);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnDoneShipping).setOnClickListener(v -> done());
    }

    private void done() {
        if (!isPositive(edtWeight)) {
            showToast("Vui lòng nhập cân nặng hợp lệ");
            return;
        }

        Intent result = new Intent();
        result.putExtra("weight", textOf(edtWeight));
        result.putExtra("length", textOf(edtLength));
        result.putExtra("width", textOf(edtWidth));
        result.putExtra("height", textOf(edtHeight));
        setResult(RESULT_OK, result);
        finish();
    }

    private boolean isPositive(EditText editText) {
        try {
            return new BigDecimal(textOf(editText)).compareTo(BigDecimal.ZERO) > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String textOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
