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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.seller.order.response.OrderListResponse;

import java.util.ArrayList;
import java.util.List;

public class SellerOrdersFragment extends Fragment {

    private SellerOrderViewModel viewModel;
    private SellerOrderAdapter adapter;

    private EditText edtSearch;
    private TextView chipAll;
    private TextView chipPending;
    private TextView chipConfirmed;
    private TextView chipShipping;
    private TextView chipCompleted;
    private TextView chipCancelled;
    private View layoutEmpty;

    private final List<OrderListResponse> allOrders = new ArrayList<>();

    private String currentTab = "ALL";
    private boolean actionRunning = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_seller_orders, container, false);

        viewModel = new ViewModelProvider(this).get(SellerOrderViewModel.class);

        bindViews(view);
        setupRecyclerView(view);
        setupEvents();
        observeViewModel();

        selectTab("ALL");
        viewModel.loadOrders("");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (viewModel != null) {
            viewModel.loadOrders(getSearchKeyword());
        }
    }

    private void bindViews(@NonNull View view) {
        edtSearch = view.findViewById(R.id.edtSearchOrder);
        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipConfirmed = view.findViewById(R.id.chipConfirmed);
        chipShipping = view.findViewById(R.id.chipShipping);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipCancelled = view.findViewById(R.id.chipCancelled);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rvOrders);

        adapter = new SellerOrderAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(false);

        adapter.setListener(new SellerOrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(OrderListResponse order) {
                openDetail(order);
            }

            @Override
            public void onPrimaryAction(OrderListResponse order) {
                handlePrimaryAction(order);
            }

            @Override
            public void onPrintShippingLabel(OrderListResponse order) {
                openShippingLabel(order);
            }
        });
    }

    private void setupEvents() {
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> selectTab("ALL"));
        }

        if (chipPending != null) {
            chipPending.setOnClickListener(v -> selectTab("PENDING"));
        }

        if (chipConfirmed != null) {
            chipConfirmed.setOnClickListener(v -> selectTab("CONFIRMED"));
        }

        if (chipShipping != null) {
            chipShipping.setOnClickListener(v -> selectTab("SHIPPING"));
        }

        if (chipCompleted != null) {
            chipCompleted.setOnClickListener(v -> selectTab("COMPLETED"));
        }

        if (chipCancelled != null) {
            chipCancelled.setOnClickListener(v -> selectTab("CANCELLED"));
        }

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (viewModel != null) {
                        viewModel.loadOrders(s == null ? "" : s.toString().trim());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void observeViewModel() {
        viewModel.getOrderListState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.isLoading()) {
                if (layoutEmpty != null) {
                    layoutEmpty.setVisibility(View.GONE);
                }
                return;
            }

            if (state.isError()) {
                showToast(state.getMessage());

                allOrders.clear();
                applyFilter();
                return;
            }

            if (state.isSuccess()) {
                allOrders.clear();

                if (state.getOrders() != null) {
                    allOrders.addAll(state.getOrders());
                }

                /*
                 * Yêu cầu mới:
                 * - Quản lý đơn hàng luôn hiển thị đơn mới nhất lên đầu.
                 * - Sắp xếp ở local để không ảnh hưởng API/backend cũ.
                 */
                sortNewestOrdersFirst(allOrders);

                applyFilter();
            }
        });

        viewModel.getActionState().observe(getViewLifecycleOwner(), state -> {
            if (state == null || state.isIdle()) return;

            if (state.isLoading()) {
                actionRunning = true;
                return;
            }

            actionRunning = false;

            if (state.isError()) {
                showToast(state.getMessage());
                viewModel.resetActionState();
                return;
            }

            if (state.isSuccess()) {
                showToast(state.getMessage());
                viewModel.resetActionState();
                viewModel.loadOrders(getSearchKeyword());
            }
        });
    }

    private void selectTab(String tab) {
        currentTab = tab == null || tab.trim().isEmpty()
                ? "ALL"
                : tab.trim().toUpperCase();

        setChipActive(chipAll, "ALL".equals(currentTab));
        setChipActive(chipPending, "PENDING".equals(currentTab));
        setChipActive(chipConfirmed, "CONFIRMED".equals(currentTab));
        setChipActive(chipShipping, "SHIPPING".equals(currentTab));
        setChipActive(chipCompleted, "COMPLETED".equals(currentTab));
        setChipActive(chipCancelled, "CANCELLED".equals(currentTab));

        applyFilter();
    }

    private void setChipActive(TextView chip, boolean active) {
        if (chip == null || getContext() == null) return;

        chip.setSelected(active);

        int textColor = ContextCompat.getColor(
                chip.getContext(),
                active ? R.color.text_white : R.color.text_primary
        );

        chip.setTextColor(textColor);

        chip.setBackgroundResource(
                active
                        ? R.drawable.bg_seller_button_primary
                        : R.drawable.bg_seller_chip_soft
        );
    }

    private void applyFilter() {
        List<OrderListResponse> filtered = new ArrayList<>();

        for (OrderListResponse order : allOrders) {
            if (order == null) continue;

            if (OrderStatusHelper.belongsToTab(order.getStatus(), currentTab)) {
                filtered.add(order);
            }
        }

        sortNewestOrdersFirst(filtered);

        if (adapter != null) {
            adapter.submitList(filtered);
        }

        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void sortNewestOrdersFirst(List<OrderListResponse> orders) {
        if (orders == null || orders.size() <= 1) {
            return;
        }

        orders.sort((o1, o2) -> {
            String t1 = o1 == null || o1.getCreatedAt() == null ? "" : String.valueOf(o1.getCreatedAt());
            String t2 = o2 == null || o2.getCreatedAt() == null ? "" : String.valueOf(o2.getCreatedAt());

            /*
             * createdAt dạng ISO/string thì so sánh chuỗi yyyy-MM-dd... vẫn cho đúng thứ tự mới-cũ.
             * Nếu null thì đẩy xuống dưới.
             */
            return t2.compareTo(t1);
        });
    }

    private void handlePrimaryAction(OrderListResponse order) {
        if (!isAdded() || getContext() == null) return;

        if (actionRunning) {
            showToast("Đang xử lý đơn hàng, vui lòng chờ");
            return;
        }

        if (order == null || order.getId() == null || order.getId() <= 0) {
            showToast("Không tìm thấy mã đơn hàng");
            return;
        }

        String currentStatus = OrderStatusHelper.normalize(order.getStatus());
        String nextStatus = OrderStatusHelper.nextStatus(currentStatus);

        if (nextStatus == null || nextStatus.trim().isEmpty()) {
            openDetail(order);
            return;
        }

        viewModel.updateStatus(order.getId(), nextStatus, "");
    }

    private void openDetail(OrderListResponse order) {
        if (!isAdded() || getContext() == null) {
            return;
        }

        if (order == null) {
            showToast("Không tìm thấy đơn hàng");
            return;
        }

        Long orderId = order.getId();

        if (orderId == null || orderId <= 0) {
            showToast("Không tìm thấy mã đơn hàng");
            return;
        }

        try {
            Intent intent = new Intent(requireContext(), SellerOrderDetailActivity.class);
            intent.putExtra(SellerOrderDetailActivity.EXTRA_ORDER_ID, orderId.longValue());
            startActivity(intent);
        } catch (Exception e) {
            showToast("Không mở được chi tiết đơn hàng: " + e.getMessage());
        }
    }

    private void openShippingLabel(OrderListResponse order) {
        if (!isAdded() || getContext() == null) return;

        if (order == null || order.getId() == null || order.getId() <= 0) {
            showToast("Không tìm thấy mã đơn hàng để in phiếu");
            return;
        }

        try {
            Intent intent = new Intent(requireContext(), ShippingLabelActivity.class);
            intent.putExtra(ShippingLabelActivity.EXTRA_ORDER_ID, order.getId().longValue());
            startActivity(intent);
        } catch (Exception e) {
            showToast("Không mở được phiếu giao hàng: " + e.getMessage());
        }
    }

    private String getSearchKeyword() {
        if (edtSearch == null || edtSearch.getText() == null) {
            return "";
        }

        return edtSearch.getText().toString().trim();
    }

    private void showToast(String message) {
        if (getContext() == null) return;

        Toast.makeText(
                getContext(),
                message == null || message.trim().isEmpty()
                        ? "Có lỗi xảy ra"
                        : message,
                Toast.LENGTH_SHORT
        ).show();
    }
}
