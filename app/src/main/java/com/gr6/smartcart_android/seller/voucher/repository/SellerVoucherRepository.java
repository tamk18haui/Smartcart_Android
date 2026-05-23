package com.gr6.smartcart_android.seller.voucher.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.voucher.api.SellerVoucherApiService;
import com.gr6.smartcart_android.seller.voucher.request.VoucherRequest;
import com.gr6.smartcart_android.seller.voucher.response.VoucherResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerVoucherRepository {

    private final SellerVoucherApiService apiService;

    public SellerVoucherRepository(Context context) {
        apiService = ApiClient.createService(context, SellerVoucherApiService.class);
    }

    public void loadMyVouchers(VoucherCallback<List<VoucherResponse>> callback) {
        apiService.getMyVouchers().enqueue(wrap(callback, "Không lấy được danh sách voucher"));
    }

    public void createVoucher(VoucherRequest request, VoucherCallback<VoucherResponse> callback) {
        apiService.createVoucher(request).enqueue(wrap(callback, "Tạo voucher thất bại"));
    }

    public void updateVoucher(Long voucherId, VoucherRequest request, VoucherCallback<VoucherResponse> callback) {
        if (voucherId == null || voucherId <= 0) {
            callback.onError("Mã voucher không hợp lệ");
            return;
        }
        apiService.updateVoucher(voucherId, request).enqueue(wrap(callback, "Cập nhật voucher thất bại"));
    }

    public void deactivateVoucher(Long voucherId, SimpleCallback callback) {
        if (voucherId == null || voucherId <= 0) {
            callback.onError("Mã voucher không hợp lệ");
            return;
        }
        apiService.deactivateVoucher(voucherId).enqueue(new Callback<BaseResponse<Object>>() {
            @Override public void onResponse(@NonNull Call<BaseResponse<Object>> call,
                                             @NonNull Response<BaseResponse<Object>> response) {
                if (!response.isSuccessful()) {
                    callback.onError("Tắt voucher thất bại. Mã lỗi: " + response.code());
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
                callback.onSuccess(body.getSafeMessage());
            }

            @Override public void onFailure(@NonNull Call<BaseResponse<Object>> call, @NonNull Throwable t) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private <T> Callback<BaseResponse<T>> wrap(VoucherCallback<T> callback, String defaultError) {
        return new Callback<BaseResponse<T>>() {
            @Override public void onResponse(@NonNull Call<BaseResponse<T>> call,
                                             @NonNull Response<BaseResponse<T>> response) {
                if (!response.isSuccessful()) {
                    callback.onError(defaultError + ". Mã lỗi: " + response.code());
                    return;
                }
                BaseResponse<T> body = response.body();
                if (body == null) {
                    callback.onError("Server không trả dữ liệu");
                    return;
                }
                if (!body.isSuccess()) {
                    callback.onError(body.getSafeMessage());
                    return;
                }
                callback.onSuccess(body.getData(), body.getSafeMessage());
            }

            @Override public void onFailure(@NonNull Call<BaseResponse<T>> call, @NonNull Throwable t) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        };
    }

    public interface VoucherCallback<T> {
        void onSuccess(T data, String message);
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess(String message);
        void onError(String message);
    }
}
