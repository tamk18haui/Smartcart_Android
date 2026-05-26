package com.gr6.smartcart_android.seller.shop;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.address.location.LocationUnit;
import com.gr6.smartcart_android.buyer.address.location.repository.VietnamAddressRepository;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.cloudinary.CloudinaryRepository;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.seller.shop.api.SellerShopApiService;
import com.gr6.smartcart_android.seller.shop.request.SellerShopUpdateRequest;
import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerShopInfoActivity extends AppCompatActivity {

    private ImageView imgShopLogo;
    private TextView txtChangeLogo;
    private TextView txtShopName;
    private TextView txtStatus;
    private TextView txtDescription;
    private TextView txtPickupAddress;
    private TextView txtShopId;
    private TextView txtEditShop;
    private TextView txtShopRatingAverage;
    private TextView txtShopReviewCount;
    private TextView txtShopProductCount;
    private View progressBar;

    private SellerShopApiService sellerApiService;
    private CloudinaryRepository cloudinaryRepository;
    private VietnamAddressRepository vietnamAddressRepository;
    private ActivityResultLauncher<String> pickLogoLauncher;

    private SellerShopInfoResponse currentShop;
    private String currentLogoUrl = "";
    private String currentCoverUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shop_info);

        sellerApiService = ApiClient.createService(this, SellerShopApiService.class);
        cloudinaryRepository = new CloudinaryRepository();
        vietnamAddressRepository = new VietnamAddressRepository();

        setupImagePicker();
        bindViews();
        bindEvents();
        loadShopInfo();
    }

    private void setupImagePicker() {
        pickLogoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    uploadShopLogo(uri);
                }
        );
    }

    private void bindViews() {
        imgShopLogo = findViewById(R.id.imgShopLogo);
        txtChangeLogo = findViewById(R.id.txtChangeLogo);
        txtShopName = findViewById(R.id.txtShopInfoName);
        txtStatus = findViewById(R.id.txtShopInfoStatus);
        txtDescription = findViewById(R.id.txtShopInfoDescription);
        txtPickupAddress = findViewById(R.id.txtShopInfoPickupAddress);
        txtShopId = findViewById(R.id.txtShopInfoId);
        txtEditShop = findViewById(R.id.txtEditShop);
        txtShopRatingAverage = findViewById(R.id.txtShopRatingAverage);
        txtShopReviewCount = findViewById(R.id.txtShopReviewCount);
        txtShopProductCount = findViewById(R.id.txtShopProductCount);
        progressBar = findViewById(R.id.progressShopInfo);
    }

    private void bindEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        txtEditShop.setOnClickListener(v -> openEditDialog());
        txtChangeLogo.setOnClickListener(v -> chooseLogoImage());
        imgShopLogo.setOnClickListener(v -> chooseLogoImage());
    }

    private void chooseLogoImage() {
        if (currentShop == null) {
            Toast.makeText(this, "Đang tải dữ liệu shop, thử lại sau", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"ACTIVE".equals(normalizeStatus(currentShop.getStatus()))) {
            Toast.makeText(this, "Shop chưa hoạt động nên chưa thể đổi avatar", Toast.LENGTH_SHORT).show();
            return;
        }

        pickLogoLauncher.launch("image/*");
    }

    private void uploadShopLogo(Uri uri) {
        setLoading(true);
        imgShopLogo.setImageURI(uri);

        cloudinaryRepository.uploadImage(this, uri, new CloudinaryRepository.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    currentLogoUrl = imageUrl == null ? "" : imageUrl.trim();
                    ImageLoader.loadCircle(SellerShopInfoActivity.this, currentLogoUrl, imgShopLogo);
                    updateShopProfile(null, buildCurrentUpdateRequest(currentLogoUrl));
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    ImageLoader.loadCircle(SellerShopInfoActivity.this, currentLogoUrl, imgShopLogo);
                    Toast.makeText(SellerShopInfoActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
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
        currentLogoUrl = shop.getLogoUrl();
        currentCoverUrl = shop.getCoverUrl();

        ImageLoader.loadCircle(this, currentLogoUrl, imgShopLogo);

        txtShopName.setText(emptyToDefault(shop.getShopName(), "Cửa hàng SmartCart"));
        txtStatus.setText(toVietnameseStatus(shop.getStatus()));
        txtDescription.setText(emptyToDefault(shop.getDescription(), "Chưa có mô tả cửa hàng."));
        txtPickupAddress.setText(emptyToDefault(shop.getPickupAddress(), "Chưa có địa chỉ lấy hàng."));
        txtShopId.setText(shop.getShopId() == null ? "#--" : "#" + shop.getShopId());

        long productCount = shop.getProductCount();
        long reviewCount = shop.getReviewCount();
        double ratingAverage = shop.getRatingAverage();

        txtShopProductCount.setText(String.valueOf(productCount));

        if (reviewCount <= 0) {
            txtShopRatingAverage.setText("Chưa có");
            txtShopReviewCount.setText("0 đánh giá");
        } else {
            txtShopRatingAverage.setText(String.format(Locale.US, "%.1f/5", ratingAverage));
            txtShopReviewCount.setText(reviewCount + " đánh giá");
        }

        String status = normalizeStatus(shop.getStatus());
        boolean canEdit = "ACTIVE".equals(status);

        txtEditShop.setEnabled(canEdit);
        txtEditShop.setAlpha(canEdit ? 1f : 0.45f);

        txtChangeLogo.setEnabled(canEdit);
        txtChangeLogo.setAlpha(canEdit ? 1f : 0.45f);
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
        EditText edtDetailAddress = createEditText("Số nhà, tên đường/khu vực lấy hàng", currentShop.getPickupAddress(), true);
        TextView txtProvince = createPickerText("Chọn tỉnh/thành phố lấy hàng");
        TextView txtWard = createPickerText("Chọn phường/xã lấy hàng");
        EditText edtDescription = createEditText("Mô tả cửa hàng", currentShop.getDescription(), true);

        final LocationUnit[] selectedProvince = {null};
        final LocationUnit[] selectedWard = {null};

        txtProvince.setOnClickListener(v -> loadAndPickProvince(txtProvince, txtWard, selectedProvince, selectedWard));
        txtWard.setOnClickListener(v -> {
            if (selectedProvince[0] == null) {
                Toast.makeText(this, "Vui lòng chọn tỉnh/thành phố trước", Toast.LENGTH_SHORT).show();
                return;
            }
            loadAndPickWard(txtWard, selectedProvince[0], selectedWard);
        });

        wrapper.addView(createFieldLabel("Tên cửa hàng"));
        wrapper.addView(edtName);
        wrapper.addView(createFieldLabel("Số nhà, tên đường/khu vực lấy hàng"));
        wrapper.addView(edtDetailAddress);
        wrapper.addView(createFieldLabel("Tỉnh/Thành phố lấy hàng"));
        wrapper.addView(txtProvince);
        wrapper.addView(createFieldLabel("Phường/Xã lấy hàng"));
        wrapper.addView(txtWard);
        wrapper.addView(createFieldLabel("Mô tả cửa hàng"));
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
                String detailAddress = edtDetailAddress.getText().toString().trim();
                String description = edtDescription.getText().toString().trim();

                if (name.isEmpty()) {
                    edtName.requestFocus();
                    edtName.setError("Tên cửa hàng không được để trống");
                    return;
                }

                if (detailAddress.isEmpty()) {
                    edtDetailAddress.requestFocus();
                    edtDetailAddress.setError("Địa chỉ lấy hàng không được để trống");
                    return;
                }

                String address = buildPickupAddress(detailAddress, selectedWard[0], selectedProvince[0]);

                SellerShopUpdateRequest request = new SellerShopUpdateRequest(
                        name,
                        address,
                        description,
                        currentLogoUrl,
                        currentCoverUrl
                );

                updateShopProfile(dialog, request);
            });
        });

        dialog.show();
    }

    private TextView createFieldLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(13);
        label.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        label.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(10), 0, dp(4));
        label.setLayoutParams(params);
        return label;
    }

    private TextView createPickerText(String value) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(15);
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        textView.setGravity(android.view.Gravity.CENTER_VERTICAL);
        textView.setBackgroundResource(R.drawable.bg_address_input);
        textView.setPadding(dp(12), 0, dp(12), 0);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        params.setMargins(0, dp(8), 0, dp(8));
        textView.setLayoutParams(params);
        return textView;
    }

    private void loadAndPickProvince(
            TextView txtProvince,
            TextView txtWard,
            LocationUnit[] selectedProvince,
            LocationUnit[] selectedWard
    ) {
        setLoading(true);
        vietnamAddressRepository.getProvinces(new VietnamAddressRepository.LocationListCallback() {
            @Override
            public void onSuccess(List<LocationUnit> data) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showLocationDialog("Chọn tỉnh/thành phố", data, unit -> {
                        selectedProvince[0] = unit;
                        selectedWard[0] = null;
                        txtProvince.setText(unit.getName());
                        txtWard.setText("Chọn phường/xã lấy hàng");
                    });
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(SellerShopInfoActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadAndPickWard(
            TextView txtWard,
            LocationUnit province,
            LocationUnit[] selectedWard
    ) {
        setLoading(true);
        vietnamAddressRepository.getWardsByProvince(province.getSafeCode(), new VietnamAddressRepository.LocationListCallback() {
            @Override
            public void onSuccess(List<LocationUnit> data) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showLocationDialog("Chọn phường/xã", data, unit -> {
                        selectedWard[0] = unit;
                        txtWard.setText(unit.getName());
                    });
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(SellerShopInfoActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLocationDialog(String title, List<LocationUnit> data, LocationPickCallback callback) {
        List<LocationUnit> safeData = data == null ? new ArrayList<>() : data;
        if (safeData.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] names = new String[safeData.size()];
        for (int i = 0; i < safeData.size(); i++) {
            names[i] = safeData.get(i).getName();
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(names, (dialog, which) -> {
                    if (callback != null) {
                        callback.onPick(safeData.get(which));
                    }
                })
                .show();
    }

    private String buildPickupAddress(String detailAddress, LocationUnit ward, LocationUnit province) {
        StringBuilder builder = new StringBuilder(detailAddress == null ? "" : detailAddress.trim());

        if (ward != null) {
            appendAddressPart(builder, ward.getName());
        }

        if (province != null) {
            appendAddressPart(builder, province.getName());
        }

        return builder.toString();
    }

    private void appendAddressPart(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) return;
        if (builder.length() > 0) builder.append(", ");
        builder.append(value.trim());
    }

    private interface LocationPickCallback {
        void onPick(LocationUnit unit);
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

    private SellerShopUpdateRequest buildCurrentUpdateRequest(String logoUrl) {
        return new SellerShopUpdateRequest(
                currentShop == null ? "Cửa hàng SmartCart" : currentShop.getSafeShopName(),
                currentShop == null ? "Địa chỉ lấy hàng" : currentShop.getPickupAddress(),
                currentShop == null ? "" : currentShop.getDescription(),
                logoUrl,
                currentCoverUrl
        );
    }

    private void updateShopProfile(AlertDialog dialog, SellerShopUpdateRequest request) {
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
                if (dialog != null) {
                    dialog.dismiss();
                }
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


