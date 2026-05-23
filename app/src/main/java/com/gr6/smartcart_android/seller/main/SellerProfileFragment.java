package com.gr6.smartcart_android.seller.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.storage.TokenManager;
import com.gr6.smartcart_android.common.storage.UserSession;
import com.gr6.smartcart_android.common.utils.ImageLoader;
import com.gr6.smartcart_android.navigation.AppNavigator;
import com.gr6.smartcart_android.seller.shop.SellerShopInfoActivity;
import com.gr6.smartcart_android.seller.shop.api.SellerShopApiService;
import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;
import com.gr6.smartcart_android.seller.voucher.CreateVoucherActivity;
import com.gr6.smartcart_android.seller.wallet.SellerWalletActivity;
import com.gr6.smartcart_android.seller.voucher.SellerVoucherActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerProfileFragment extends Fragment {

    private ImageView imgShopAvatar;
    private TextView txtSellerName;
    private TextView txtSellerEmail;
    private TextView txtShopStatus;
    private TextView txtShopName;

    private SellerShopApiService sellerApiService;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_seller_profile, container, false);

        sellerApiService = ApiClient.createService(requireContext(), SellerShopApiService.class);

        bindViews(view);
        bindUserSession();
        bindEvents(view);
        loadShopInfo();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sellerApiService != null) {
            loadShopInfo();
        }
    }

    private void bindViews(View view) {
        imgShopAvatar = view.findViewById(R.id.imgShopAvatar);
        txtSellerName = view.findViewById(R.id.txtSellerName);
        txtSellerEmail = view.findViewById(R.id.txtSellerEmail);
        txtShopStatus = view.findViewById(R.id.txtShopStatus);
        txtShopName = view.findViewById(R.id.txtShopName);
    }

    private void bindUserSession() {
        UserSession session = UserSession.getInstance(requireContext());
        String fullName = session.getFullName();
        String email = session.getEmail();

        txtSellerName.setText(fullName == null || fullName.trim().isEmpty() ? "Người bán SmartCart" : fullName);
        txtSellerEmail.setText(email == null || email.trim().isEmpty() ? "seller@smartcart.vn" : email);
    }

    private void bindEvents(View view) {
        View.OnClickListener openShopInfo = v ->
                startActivity(new Intent(requireContext(), SellerShopInfoActivity.class));

        imgShopAvatar.setOnClickListener(openShopInfo);
        view.findViewById(R.id.rowShopInfo).setOnClickListener(openShopInfo);

        view.findViewById(R.id.rowWallet).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SellerWalletActivity.class))
        );

        view.findViewById(R.id.rowVoucherManager).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SellerVoucherActivity.class))
        );

        view.findViewById(R.id.rowCreateVoucher).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CreateVoucherActivity.class))
        );

        view.findViewById(R.id.rowChangePassword).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Chức năng đổi mật khẩu đang chờ hoàn thiện backend", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.rowLogout).setOnClickListener(v -> logout());
    }

    private void loadShopInfo() {
        sellerApiService.getMyShopInfo().enqueue(new Callback<BaseResponse<SellerShopInfoResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<SellerShopInfoResponse>> call,
                    @NonNull Response<BaseResponse<SellerShopInfoResponse>> response
            ) {
                if (!isAdded()) return;

                BaseResponse<SellerShopInfoResponse> body = response.body();
                if (!response.isSuccessful() || body == null || !body.isSuccess() || body.getData() == null) {
                    txtShopName.setText("Chưa lấy được thông tin shop");
                    txtShopStatus.setText("CHƯA XÁC ĐỊNH");
                    ImageLoader.loadCircle(requireContext(), "", imgShopAvatar);
                    return;
                }

                SellerShopInfoResponse shop = body.getData();
                txtShopName.setText(shop.getShopName().trim().isEmpty() ? "Cửa hàng SmartCart" : shop.getShopName());
                txtShopStatus.setText(toVietnameseStatus(shop.getStatus()));
                ImageLoader.loadCircle(requireContext(), shop.getLogoUrl(), imgShopAvatar);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<SellerShopInfoResponse>> call,
                    @NonNull Throwable t
            ) {
                if (!isAdded()) return;
                txtShopName.setText("Không kết nối được server");
                txtShopStatus.setText("LỖI KẾT NỐI");
            }
        });
    }

    private void logout() {
        TokenManager.getInstance(requireContext()).clearAll();
        UserSession.getInstance(requireContext()).clear();
        AppNavigator.openLogin(requireContext());
    }

    private String toVietnameseStatus(String status) {
        if (status == null) return "CHƯA XÁC ĐỊNH";
        String normalized = status.trim().toUpperCase();
        if ("ACTIVE".equals(normalized)) return "ĐANG HOẠT ĐỘNG";
        if ("PENDING".equals(normalized)) return "CHỜ DUYỆT";
        if ("REJECTED".equals(normalized)) return "BỊ TỪ CHỐI";
        if ("BANNED".equals(normalized)) return "BỊ KHÓA";
        return normalized;
    }
}


