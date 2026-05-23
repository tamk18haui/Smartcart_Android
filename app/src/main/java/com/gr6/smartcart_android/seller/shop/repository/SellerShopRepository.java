package com.gr6.smartcart_android.seller.shop.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.shop.api.SellerShopApiService;
import com.gr6.smartcart_android.seller.shop.request.SellerShopUpdateRequest;
import com.gr6.smartcart_android.seller.shop.response.SellerShopInfoResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerShopRepository {

    private final SellerShopApiService apiService;

    public SellerShopRepository(Context context) {
        apiService = ApiClient.createService(context, SellerShopApiService.class);
    }

    public Call<BaseResponse<SellerShopInfoResponse>> getMyShopInfoCall() {
        return apiService.getMyShopInfo();
    }

    public Call<BaseResponse<Object>> updateMyShopCall(SellerShopUpdateRequest request) {
        return apiService.updateMyShop(request);
    }

    public void loadMyShopInfo(ShopCallback<SellerShopInfoResponse> callback) {
        apiService.getMyShopInfo().enqueue(new Callback<BaseResponse<SellerShopInfoResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<SellerShopInfoResponse>> call,
                                   @NonNull Response<BaseResponse<SellerShopInfoResponse>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Không lấy được thông tin cửa hàng. Mã lỗi: " + response.code());
                    return;
                }
                BaseResponse<SellerShopInfoResponse> body = response.body();
                if (body == null) {
                    callback.onError("Server không trả dữ liệu cửa hàng");
                    return;
                }
                if (!body.isSuccess()) {
                    callback.onError(body.getSafeMessage());
                    return;
                }
                if (body.getData() == null) {
                    callback.onError("Không có dữ liệu cửa hàng");
                    return;
                }
                callback.onSuccess(body.getData(), body.getSafeMessage());
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<SellerShopInfoResponse>> call, @NonNull Throwable t) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void updateMyShop(SellerShopUpdateRequest request, ShopCallback<Object> callback) {
        apiService.updateMyShop(request).enqueue(new Callback<BaseResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<Object>> call,
                                   @NonNull Response<BaseResponse<Object>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Cập nhật cửa hàng thất bại. Mã lỗi: " + response.code());
                    return;
                }
                BaseResponse<Object> body = response.body();
                if (body == null) {
                    callback.onError("Server không trả phản hồi");
                    return;
                }
                if (!body.isSuccess()) {
                    callback.onError(body.getSafeMessage());
                    return;
                }
                callback.onSuccess(body.getData(), body.getSafeMessage());
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<Object>> call, @NonNull Throwable t) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public interface ShopCallback<T> {
        void onSuccess(T data, String message);
        void onError(String message);
    }
}
