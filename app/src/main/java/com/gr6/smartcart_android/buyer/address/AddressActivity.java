package com.gr6.smartcart_android.buyer.address;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.address.response.AddressResponse;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.utils.ThemeColor;

import java.util.ArrayList;
import java.util.List;

public class AddressActivity extends BaseActivity {

    public static final String EXTRA_SELECT_MODE = "select_mode";

    public static final String RESULT_ADDRESS_ID = "address_id";
    public static final String RESULT_RECEIVER_NAME = "receiver_name";
    public static final String RESULT_RECEIVER_PHONE = "receiver_phone";
    public static final String RESULT_FULL_ADDRESS = "full_address";

    private SwipeRefreshLayout swipeAddress;
    private TextView txtTitle;
    private TextView txtAddressCount;
    private TextView txtEmpty;
    private TextView btnAddAddress;
    private TextView btnAddAddressEmpty;

    private LinearLayout layoutEmpty;
    private RecyclerView rcvAddresses;

    private AddressAdapter adapter;
    private AddressViewModel viewModel;

    private final List<AddressResponse> addresses = new ArrayList<>();
    private boolean selectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        ThemeColor.applyBrandStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        selectMode = getIntent().getBooleanExtra(EXTRA_SELECT_MODE, false);

        viewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        initViews();
        setupRecyclerView();
        initEvents();
        observeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadAddresses();
    }

    private void initViews() {
        txtTitle = findViewById(R.id.txtTitle);
        txtAddressCount = findViewById(R.id.txtAddressCount);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnAddAddressEmpty = findViewById(R.id.btnAddAddressEmpty);
        swipeAddress = findViewById(R.id.swipeAddress);

        layoutEmpty = findViewById(R.id.layoutEmpty);
        rcvAddresses = findViewById(R.id.rcvAddresses);

        txtTitle.setText(selectMode ? "Chọn địa chỉ giao hàng" : "Địa chỉ giao hàng");
    }

    private void setupRecyclerView() {
        adapter = new AddressAdapter();

        rcvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rcvAddresses.setAdapter(adapter);

        adapter.setListener(new AddressAdapter.AddressListener() {
            @Override
            public void onSelect(AddressResponse address) {
                if (selectMode) {
                    returnSelectedAddress(address);
                }
            }

            @Override
            public void onEdit(AddressResponse address) {
                openForm(address);
            }

            @Override
            public void onDelete(AddressResponse address) {
                confirmDelete(address);
            }

            @Override
            public void onSetDefault(AddressResponse address) {
                if (address == null || address.getAddressId() == null) {
                    showToast("Không tìm thấy địa chỉ");
                    return;
                }

                if (address.isDefaultAddress()) {
                    showToast("Địa chỉ này đang là mặc định");
                    return;
                }

                viewModel.setDefaultAddress(address.getAddressId());
            }
        });
    }

    private void initEvents() {
        findViewById(R.id.imgBack).setOnClickListener(v -> finish());

        btnAddAddress.setOnClickListener(v -> openForm(null));

        btnAddAddressEmpty.setOnClickListener(v -> openForm(null));
        setupSwipeRefresh(swipeAddress, () -> viewModel.loadAddresses());
    }

    private void observeData() {
        viewModel.getAddressListState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                swipeAddress.setRefreshing(true);
                return;
            }

            stopSwipeRefresh(swipeAddress);
            if (state.isSuccess()) {
                addresses.clear();

                if (state.getData() != null) {
                    addresses.addAll(state.getData());
                }

                bindAddressList();
            } else {
                showLongToast(state.getMessage());
                showEmpty("Không tải được địa chỉ");
            }
        });

        viewModel.getActionState().observe(this, state -> {
            if (state == null) return;

            if (state.isLoading()) {
                showLoading();
                return;
            }

            hideLoading();

            if (state.isSuccess()) {
                showToast(state.getMessage());
                viewModel.loadAddresses();
            } else {
                showLongToast(state.getMessage());
            }
        });
    }

    private void bindAddressList() {
        txtAddressCount.setText(addresses.size() + " địa chỉ");

        if (addresses.isEmpty()) {
            showEmpty("Bạn chưa có địa chỉ nhận hàng");
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rcvAddresses.setVisibility(View.VISIBLE);
            adapter.setData(addresses);
        }
    }

    private void showEmpty(String message) {
        rcvAddresses.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
        txtEmpty.setText(message);
        txtAddressCount.setText("0 địa chỉ");
    }

    private void openForm(AddressResponse address) {
        Intent intent = new Intent(this, AddressFormActivity.class);

        if (address != null) {
            intent.putExtra(AddressFormActivity.EXTRA_MODE, AddressFormActivity.MODE_EDIT);
            intent.putExtra(AddressFormActivity.EXTRA_ADDRESS_ID, address.getAddressId());
            intent.putExtra(AddressFormActivity.EXTRA_RECEIVER_NAME, address.getReceiverName());
            intent.putExtra(AddressFormActivity.EXTRA_RECEIVER_PHONE, address.getReceiverPhone());
            intent.putExtra(AddressFormActivity.EXTRA_FULL_ADDRESS, address.getFullAddress());
            intent.putExtra(AddressFormActivity.EXTRA_IS_DEFAULT, address.isDefaultAddress());
        } else {
            intent.putExtra(AddressFormActivity.EXTRA_MODE, AddressFormActivity.MODE_CREATE);
        }

        startActivity(intent);
    }

    private void confirmDelete(AddressResponse address) {
        if (address == null || address.getAddressId() == null) {
            showToast("Không tìm thấy địa chỉ");
            return;
        }

        if (address.isDefaultAddress() && addresses.size() > 1) {
            showToast("Vui lòng đặt địa chỉ khác làm mặc định trước khi xóa");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ?")
                .setMessage("Bạn có chắc muốn xóa địa chỉ này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) ->
                        viewModel.deleteAddress(address.getAddressId())
                )
                .show();
    }

    private void returnSelectedAddress(AddressResponse address) {
        if (address == null) return;

        Intent data = new Intent();
        data.putExtra(RESULT_ADDRESS_ID, address.getAddressId());
        data.putExtra(RESULT_RECEIVER_NAME, address.getReceiverName());
        data.putExtra(RESULT_RECEIVER_PHONE, address.getReceiverPhone());
        data.putExtra(RESULT_FULL_ADDRESS, address.getFullAddress());

        setResult(RESULT_OK, data);
        finish();
    }
}