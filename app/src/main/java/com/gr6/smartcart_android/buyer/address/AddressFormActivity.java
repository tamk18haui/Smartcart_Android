package com.gr6.smartcart_android.buyer.address;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.address.location.LocationUnit;
import com.gr6.smartcart_android.buyer.address.location.repository.VietnamAddressRepository;
import com.gr6.smartcart_android.buyer.address.request.AddressRequest;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.common.utils.Validator;

import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.text.Normalizer;
import java.util.ArrayList;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.gr6.smartcart_android.buyer.address.location.LocationPickerAdapter;

public class AddressFormActivity extends BaseActivity {

    public static final String EXTRA_MODE = "mode";
    public static final String MODE_CREATE = "create";
    public static final String MODE_EDIT = "edit";

    public static final String EXTRA_ADDRESS_ID = "address_id";
    public static final String EXTRA_RECEIVER_NAME = "receiver_name";
    public static final String EXTRA_RECEIVER_PHONE = "receiver_phone";
    public static final String EXTRA_FULL_ADDRESS = "full_address";
    public static final String EXTRA_IS_DEFAULT = "is_default";

    private TextView txtTitle;
    private TextView btnSave;

    private EditText edtReceiverName;
    private EditText edtReceiverPhone;
    private EditText edtProvince;
    private EditText edtDistrict;
    private EditText edtWard;
    private EditText edtDetailAddress;

    private SwitchCompat switchDefault;

    private AddressViewModel viewModel;
    private VietnamAddressRepository locationRepository;

    private String mode = MODE_CREATE;
    private Long addressId = null;

    private LocationUnit selectedProvince;
    private LocationUnit selectedWard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_form);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        viewModel = new ViewModelProvider(this).get(AddressViewModel.class);
        locationRepository = new VietnamAddressRepository();

        readIntent();
        initViews();
        setupLocationPickers();
        bindDataIfEdit();
        initEvents();
        observeData();
    }

    private void readIntent() {
        mode = getIntent().getStringExtra(EXTRA_MODE);

        if (mode == null || mode.trim().isEmpty()) {
            mode = MODE_CREATE;
        }

        addressId = getIntent().getLongExtra(EXTRA_ADDRESS_ID, -1L);

        if (addressId == -1L) {
            addressId = null;
        }
    }

    private void initViews() {
        txtTitle = findViewById(R.id.txtTitle);
        btnSave = findViewById(R.id.btnSave);

        edtReceiverName = findViewById(R.id.edtReceiverName);
        edtReceiverPhone = findViewById(R.id.edtReceiverPhone);
        edtProvince = findViewById(R.id.edtProvince);
        edtDistrict = findViewById(R.id.edtDistrict);
        edtWard = findViewById(R.id.edtWard);
        edtDetailAddress = findViewById(R.id.edtDetailAddress);

        switchDefault = findViewById(R.id.switchDefault);

        txtTitle.setText(MODE_EDIT.equals(mode) ? "Sửa địa chỉ" : "Thêm địa chỉ");
        btnSave.setText(MODE_EDIT.equals(mode) ? "Cập nhật địa chỉ" : "Lưu địa chỉ");

        // Bản v2 chỉ còn 2 cấp: Tỉnh/Thành phố -> Phường/Xã.
        if (edtDistrict != null) {
            edtDistrict.setVisibility(View.GONE);
        }
    }

    private void setupLocationPickers() {
        makePickerField(edtProvince, "Chọn tỉnh / thành phố");
        makePickerField(edtWard, "Chọn phường / xã");

        edtProvince.setOnClickListener(v -> showProvinceDialog());

        edtWard.setOnClickListener(v -> {
            if (selectedProvince == null) {
                showToast("Vui lòng chọn tỉnh / thành phố trước");
                return;
            }

            showWardDialog();
        });
    }

    private void makePickerField(EditText editText, String hint) {
        if (editText == null) return;

        editText.setHint(hint);
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setCursorVisible(false);
        editText.setInputType(0);
    }

    private void bindDataIfEdit() {
        if (!MODE_EDIT.equals(mode)) return;

        String receiverName = getIntent().getStringExtra(EXTRA_RECEIVER_NAME);
        String receiverPhone = getIntent().getStringExtra(EXTRA_RECEIVER_PHONE);
        String fullAddress = getIntent().getStringExtra(EXTRA_FULL_ADDRESS);
        boolean isDefault = getIntent().getBooleanExtra(EXTRA_IS_DEFAULT, false);

        edtReceiverName.setText(receiverName);
        edtReceiverPhone.setText(receiverPhone);

        /*
         * Với địa chỉ cũ, app chưa tách được tỉnh/xã nên giữ fullAddress vào ô địa chỉ cụ thể.
         * Nếu người dùng chọn lại tỉnh/xã, app sẽ build lại fullAddress theo chuẩn v2.
         */
        edtDetailAddress.setText(fullAddress);

        switchDefault.setChecked(isDefault);
    }

    private void initEvents() {
        findViewById(R.id.imgBack).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void observeData() {
        viewModel.getActionState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                showToast(state.getMessage());
                finish();
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void showProvinceDialog() {
        showLoading();

        locationRepository.getProvinces(new VietnamAddressRepository.LocationListCallback() {
            @Override
            public void onSuccess(List<LocationUnit> data) {
                hideLoading();

                if (data == null || data.isEmpty()) {
                    showToast("Không có dữ liệu tỉnh / thành phố");
                    return;
                }

                showLocationDialog(
                        "Chọn tỉnh / thành phố",
                        data,
                        unit -> {
                            selectedProvince = unit;
                            selectedWard = null;

                            edtProvince.setText(unit.getName());
                            edtWard.setText("");
                        }
                );
            }

            @Override
            public void onError(String message) {
                hideLoading();
                showLongToast(message);
            }
        });
    }

    private void showWardDialog() {
        if (selectedProvince == null || selectedProvince.getSafeCode() == -1) {
            showToast("Vui lòng chọn tỉnh / thành phố trước");
            return;
        }

        showLoading();

        locationRepository.getWardsByProvince(
                selectedProvince.getSafeCode(),
                new VietnamAddressRepository.LocationListCallback() {
                    @Override
                    public void onSuccess(List<LocationUnit> data) {
                        hideLoading();

                        if (data == null || data.isEmpty()) {
                            showToast("Không có dữ liệu phường / xã");
                            return;
                        }

                        showLocationDialog(
                                "Chọn phường / xã",
                                data,
                                unit -> {
                                    selectedWard = unit;
                                    edtWard.setText(unit.getName());
                                }
                        );
                    }

                    @Override
                    public void onError(String message) {
                        hideLoading();
                        showLongToast(message);
                    }
                }
        );
    }

    private void showLocationDialog(
            String title,
            List<LocationUnit> data,
            OnLocationSelectedListener listener
    ) {
        if (data == null || data.isEmpty()) {
            showToast("Không có dữ liệu để chọn");
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottom_sheet_location_picker, null, false);

        TextView txtPickerTitle = view.findViewById(R.id.txtPickerTitle);
        TextView txtPickerSubtitle = view.findViewById(R.id.txtPickerSubtitle);
        EditText edtSearchLocation = view.findViewById(R.id.edtSearchLocation);
        RecyclerView rcvLocations = view.findViewById(R.id.rcvLocations);
        LinearLayout layoutLocationEmpty = view.findViewById(R.id.layoutLocationEmpty);

        txtPickerTitle.setText(title);

        if (title.toLowerCase().contains("tỉnh")) {
            txtPickerSubtitle.setText("Chọn tỉnh / thành phố nơi bạn nhận hàng");
            edtSearchLocation.setHint("Tìm tỉnh / thành phố...");
        } else {
            txtPickerSubtitle.setText("Chọn phường / xã theo tỉnh đã chọn");
            edtSearchLocation.setHint("Tìm phường / xã...");
        }

        LocationPickerAdapter adapter = new LocationPickerAdapter();

        rcvLocations.setLayoutManager(new LinearLayoutManager(this));
        rcvLocations.setAdapter(adapter);
        adapter.setData(data);

        adapter.setListener(unit -> {
            listener.onSelected(unit);
            dialog.dismiss();
        });

        edtSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s, count1 -> {
                    if (adapter.isEmpty()) {
                        layoutLocationEmpty.setVisibility(View.VISIBLE);
                        rcvLocations.setVisibility(View.GONE);
                    } else {
                        layoutLocationEmpty.setVisibility(View.GONE);
                        rcvLocations.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        dialog.setContentView(view);

        dialog.setOnShowListener(d -> {
            View bottomSheet = dialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet
            );

            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);

                ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
                params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.86f);
                bottomSheet.setLayoutParams(params);
            }

            edtSearchLocation.requestFocus();
            edtSearchLocation.postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.showSoftInput(edtSearchLocation, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 180);
        });

        dialog.show();
    }

    private void validateAndSave() {
        String receiverName = edtReceiverName.getText().toString().trim();
        String receiverPhone = edtReceiverPhone.getText().toString().trim();
        String detailAddress = edtDetailAddress.getText().toString().trim();

        if (Validator.isEmpty(receiverName)) {
            edtReceiverName.requestFocus();
            showToast("Vui lòng nhập tên người nhận");
            return;
        }

        if (!Validator.isValidPhone(receiverPhone)) {
            edtReceiverPhone.requestFocus();
            showToast("Số điện thoại không hợp lệ");
            return;
        }

        if (Validator.isEmpty(detailAddress)) {
            edtDetailAddress.requestFocus();
            showToast("Vui lòng nhập địa chỉ cụ thể");
            return;
        }

        boolean userPickedLocation = selectedProvince != null || selectedWard != null;

        if (MODE_CREATE.equals(mode) || userPickedLocation) {
            if (selectedProvince == null) {
                showToast("Vui lòng chọn tỉnh / thành phố");
                return;
            }

            if (selectedWard == null) {
                showToast("Vui lòng chọn phường / xã");
                return;
            }
        }

        String fullAddress;

        if (selectedProvince != null && selectedWard != null) {
            fullAddress = buildFullAddress(
                    detailAddress,
                    selectedWard.getName(),
                    selectedProvince.getName()
            );
        } else {
            fullAddress = detailAddress;
        }

        AddressRequest request = new AddressRequest(
                receiverName,
                receiverPhone,
                fullAddress,
                switchDefault.isChecked()
        );

        if (MODE_EDIT.equals(mode)) {
            if (addressId == null) {
                showToast("Không tìm thấy địa chỉ cần sửa");
                return;
            }

            viewModel.updateAddress(addressId, request);
        } else {
            viewModel.createAddress(request);
        }
    }

    private String buildFullAddress(
            String detailAddress,
            String ward,
            String province
    ) {
        StringBuilder builder = new StringBuilder();

        appendPart(builder, detailAddress);
        appendPart(builder, ward);
        appendPart(builder, province);

        return builder.toString();
    }

    private void appendPart(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) return;

        if (builder.length() > 0) {
            builder.append(", ");
        }

        builder.append(value.trim());
    }

    private interface OnLocationSelectedListener {
        void onSelected(LocationUnit unit);
    }
}