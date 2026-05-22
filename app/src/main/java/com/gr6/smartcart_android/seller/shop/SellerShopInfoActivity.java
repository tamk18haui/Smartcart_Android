package com.gr6.smartcart_android.seller.shop;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.api.SellerCatalogApiService;
import com.gr6.smartcart_android.seller.model.SellerShopInfoResponse;
import com.gr6.smartcart_android.seller.model.SellerShopUpdateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerShopInfoActivity extends AppCompatActivity {

    private TextView txtShopName;
    private TextView txtStatus;
    private TextView txtDescription;
    private TextView txtPickupAddress;
    private TextView txtShopId;
    private TextView txtEditShop;
    private View progressBar;

    private SellerCatalogApiService sellerApiService;
    private SellerShopInfoResponse currentShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shop_info);

        sellerApiService = ApiClient.createService(this, SellerCatalogApiService.class);

        bindViews();
        bindEvents();
        loadShopInfo();
    }

    private void bindViews() {
        txtShopName = findViewById(R.id.txtShopInfoName);
        txtStatus = findViewById(R.id.txtShopInfoStatus);
        txtDescription = findViewById(R.id.txtShopInfoDescription);
        txtPickupAddress = findViewById(R.id.txtShopInfoPickupAddress);
        txtShopId = findViewById(R.id.txtShopInfoId);
        txtEditShop = findViewById(R.id.txtEditShop);
        progressBar = findViewById(R.id.progressShopInfo);
    }

    private void bindEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        txtEditShop.setOnClickListener(v -> openEditDialog());
    }

    private void loadShopInfo() {
        setLoading(true);
        sellerApiService.getMyShopInfo().enqueue(new Callback<BaseResponse<SellerShopInfoResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<SellerShopInfoResponse>> call,
                    @NonNull Response<BaseResponse<SellerShopInfoResponse>> response
            ) {
                setLoading(false);

                BaseResponse<SellerShopInfoResponse> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    String message = body != null ? body.getSafeMessage() : "Không lấy được thông tin cửa hàng";
                    Toast.makeText(SellerShopInfoActivity.this, message, Toast.LENGTH_SHORT).show();
                    return;
                }

                currentShop = body.getData();
                renderShop(currentShop);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<SellerShopInfoResponse>> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);
                Toast.makeText(SellerShopInfoActivity.this, "Không kết nối được server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderShop(SellerShopInfoResponse shop) {
        txtShopName.setText(emptyToDefault(shop.getShopName(), "Cửa hàng SmartCart"));
        txtStatus.setText(toVietnameseStatus(shop.getStatus()));
        txtDescription.setText(emptyToDefault(shop.getDescription(), "Chưa có mô tả cửa hàng."));
        txtPickupAddress.setText(emptyToDefault(shop.getPickupAddress(), "Chưa có địa chỉ lấy hàng."));
        txtShopId.setText(shop.getShopId() == null ? "#--" : "#" + shop.getShopId());

        String status = normalizeStatus(shop.getStatus());
        boolean canEdit = "ACTIVE".equals(status);
        txtEditShop.setEnabled(canEdit);
        txtEditShop.setAlpha(canEdit ? 1f : 0.45f);
    }

    private void openEditDialog() {
        if (currentShop == null) {
            Toast.makeText(this, "Chưa có dữ liệu shop để chỉnh sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"ACTIVE".equals(normalizeStatus(currentShop.getStatus()))) {
            Toast.makeText(this, "Shop chưa hoạt động nên chưa thể chỉnh sửa thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(18);
        wrapper.setPadding(padding, padding / 2, padding, 0);

        EditText edtName = createEditText("Tên cửa hàng", currentShop.getShopName(), false);
        EditText edtAddress = createEditText("Địa chỉ lấy hàng", currentShop.getPickupAddress(), true);
        EditText edtDescription = createEditText("Mô tả cửa hàng", currentShop.getDescription(), true);

        wrapper.addView(edtName);
        wrapper.addView(edtAddress);
        wrapper.addView(edtDescription);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sửa thông tin cửa hàng")
                .setView(wrapper)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String name = edtName.getText().toString().trim();
                String address = edtAddress.getText().toString().trim();
                String description = edtDescription.getText().toString().trim();

                if (name.isEmpty()) {
                    edtName.requestFocus();
                    edtName.setError("Tên cửa hàng không được để trống");
                    return;
                }

                if (address.isEmpty()) {
                    edtAddress.requestFocus();
                    edtAddress.setError("Địa chỉ lấy hàng không được để trống");
                    return;
                }

                updateShop(dialog, new SellerShopUpdateRequest(name, address, description));
            });
        });

        dialog.show();
    }

    private EditText createEditText(String hint, String value, boolean multiLine) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setText(value == null ? "" : value);
        editText.setSingleLine(!multiLine);
        if (multiLine) {
            editText.setMinLines(2);
            editText.setMaxLines(4);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(8));
        editText.setLayoutParams(params);
        return editText;
    }

    private void updateShop(AlertDialog dialog, SellerShopUpdateRequest request) {
        setLoading(true);
        sellerApiService.updateMyShop(request).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Response<BaseResponse<Object>> response
            ) {
                setLoading(false);

                BaseResponse<Object> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    String message = body != null ? body.getSafeMessage() : "Cập nhật cửa hàng thất bại";
                    Toast.makeText(SellerShopInfoActivity.this, message, Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(SellerShopInfoActivity.this, body.getSafeMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadShopInfo();
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<Object>> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);
                Toast.makeText(SellerShopInfoActivity.this, "Không kết nối được server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String emptyToDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        return value;
    }

    private String normalizeStatus(String status) {
        if (status == null) return "";
        return status.trim().toUpperCase();
    }

    private String toVietnameseStatus(String status) {
        String normalized = normalizeStatus(status);
        if ("ACTIVE".equals(normalized)) return "ĐANG HOẠT ĐỘNG";
        if ("PENDING".equals(normalized)) return "CHỜ DUYỆT";
        if ("REJECTED".equals(normalized)) return "BỊ TỪ CHỐI";
        if ("BANNED".equals(normalized)) return "BỊ KHÓA";
        return normalized.isEmpty() ? "CHƯA XÁC ĐỊNH" : normalized;
    }
}
