package com.gr6.smartcart_android.seller.order;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.order.model.OrderListResponse;
import com.gr6.smartcart_android.seller.order.repository.SellerOrderRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerOrdersFragment extends Fragment {

    private SellerOrderRepository repository;
    private SellerOrderAdapter adapter;
    private EditText edtSearch;
    private TextView chipPending;
    private TextView chipConfirmed;
    private TextView chipShipping;
    private TextView chipCompleted;
    private TextView chipCancelled;
    private View layoutEmpty;

    private final List<OrderListResponse> allOrders = new ArrayList<>();
    private String currentTab = "PENDING";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_orders, container, false);

        repository = new SellerOrderRepository(requireContext());

        bindViews(view);
        setupRecyclerView(view);
        setupEvents();

        loadOrders("");

        return view;
    }

    private void bindViews(View view) {
        edtSearch = view.findViewById(R.id.edtSearchOrder);
        chipPending = view.findViewById(R.id.chipPending);
        chipConfirmed = view.findViewById(R.id.chipConfirmed);
        chipShipping = view.findViewById(R.id.chipShipping);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipCancelled = view.findViewById(R.id.chipCancelled);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rvOrders);
        adapter = new SellerOrderAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        adapter.setListener(new SellerOrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(OrderListResponse order) {
                openDetail(order);
            }

            @Override
            public void onPrimaryAction(OrderListResponse order) {
                String nextStatus = OrderStatusHelper.nextStatus(order.getStatus());
                if (nextStatus.isEmpty()) {
                    openDetail(order);
                    return;
                }
                openDetail(order);
            }
        });
    }

    private void setupEvents() {
        chipPending.setOnClickListener(v -> selectTab("PENDING"));
        chipConfirmed.setOnClickListener(v -> selectTab("CONFIRMED"));
        chipShipping.setOnClickListener(v -> selectTab("SHIPPING"));
        chipCompleted.setOnClickListener(v -> selectTab("COMPLETED"));
        chipCancelled.setOnClickListener(v -> selectTab("CANCELLED"));

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadOrders(s == null ? "" : s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        selectTab("PENDING");
    }

    private void loadOrders(String keyword) {
        repository.getOrders(keyword).enqueue(new Callback<BaseResponse<List<OrderListResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<List<OrderListResponse>>> call,
                                   @NonNull Response<BaseResponse<List<OrderListResponse>>> response) {
                BaseResponse<List<OrderListResponse>> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess()) {
                    showToast(body == null ? "Không lấy được danh sách đơn hàng" : body.getSafeMessage());
                    allOrders.clear();
                    applyFilter();
                    return;
                }

                allOrders.clear();
                if (body.getData() != null) {
                    allOrders.addAll(body.getData());
                }

                applyFilter();
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<OrderListResponse>>> call, @NonNull Throwable t) {
                showToast("Không kết nối được server: " + t.getMessage());
                allOrders.clear();
                applyFilter();
            }
        });
    }

    private void selectTab(String tab) {
        currentTab = tab;
        setChipActive(chipPending, "PENDING".equals(tab));
        setChipActive(chipConfirmed, "CONFIRMED".equals(tab));
        setChipActive(chipShipping, "SHIPPING".equals(tab));
        setChipActive(chipCompleted, "COMPLETED".equals(tab));
        setChipActive(chipCancelled, "CANCELLED".equals(tab));
        applyFilter();
    }

    private void setChipActive(TextView chip, boolean active) {
        chip.setSelected(active);
        chip.setTextColor(active ? getResources().getColor(R.color.seller_text_white) : getResources().getColor(R.color.seller_text_primary));
        chip.setBackgroundResource(active ? R.drawable.bg_seller_button_primary : R.drawable.bg_seller_chip_soft);
    }

    private void applyFilter() {
        List<OrderListResponse> filtered = new ArrayList<>();
        for (OrderListResponse order : allOrders) {
            if (OrderStatusHelper.belongsToTab(order.getStatus(), currentTab)) {
                filtered.add(order);
            }
        }
        adapter.submitList(filtered);
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void openDetail(OrderListResponse order) {
        if (order == null || order.getId() == null) return;
        Intent intent = new Intent(requireContext(), SellerOrderDetailActivity.class);
        intent.putExtra(SellerOrderDetailActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message == null ? "Có lỗi xảy ra" : message, Toast.LENGTH_SHORT).show();
        }
    }
}
