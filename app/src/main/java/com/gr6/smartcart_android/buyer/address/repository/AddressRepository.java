package com.gr6.smartcart_android.buyer.address.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.address.api.AddressApiService;
import com.gr6.smartcart_android.buyer.address.request.AddressRequest;
import com.gr6.smartcart_android.buyer.address.response.AddressResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressRepository {

    private final AddressApiService apiService;

    public AddressRepository(Context context) {
        apiService = ApiClient.createService(context, AddressApiService.class);
    }

    public void getMyAddresses(AddressListCallback callback) {
        apiService.getMyAddresses().enqueue(new Callback<BaseResponse<List<AddressResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<AddressResponse>>> call,
                    @NonNull Response<BaseResponse<List<AddressResponse>>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Không lấy được địa chỉ. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<List<AddressResponse>> body = response.body();

                if (body == null) {
                    callback.onError("Server không trả dữ liệu địa chỉ");
                    return;
                }

                if (!body.isSuccess()) {
                    callback.onError(body.getSafeMessage());
                    return;
                }

                callback.onSuccess(body.getData());
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<AddressResponse>>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void createAddress(AddressRequest request, AddressActionCallback callback) {
        apiService.createAddress(request).enqueue(new Callback<BaseResponse<AddressResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<AddressResponse>> call,
                    @NonNull Response<BaseResponse<AddressResponse>> response
            ) {
                handleAddressResponse(response, callback, "Thêm địa chỉ thất bại");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<AddressResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void updateAddress(Long addressId, AddressRequest request, AddressActionCallback callback) {
        apiService.updateAddress(addressId, request).enqueue(new Callback<BaseResponse<AddressResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<AddressResponse>> call,
                    @NonNull Response<BaseResponse<AddressResponse>> response
            ) {
                handleAddressResponse(response, callback, "Cập nhật địa chỉ thất bại");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<AddressResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void deleteAddress(Long addressId, SimpleCallback callback) {
        apiService.deleteAddress(addressId).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Response<BaseResponse<String>> response
            ) {
                handleSimpleResponse(response, callback, "Xóa địa chỉ thất bại");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void setDefaultAddress(Long addressId, SimpleCallback callback) {
        apiService.setDefaultAddress(addressId).enqueue(new Callback<BaseResponse<String>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Response<BaseResponse<String>> response
            ) {
                handleSimpleResponse(response, callback, "Thiết lập mặc định thất bại");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<String>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private void handleAddressResponse(
            Response<BaseResponse<AddressResponse>> response,
            AddressActionCallback callback,
            String defaultMessage
    ) {
        if (!response.isSuccessful()) {
            callback.onError(defaultMessage + ". Mã lỗi: " + response.code());
            return;
        }

        BaseResponse<AddressResponse> body = response.body();

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

    private void handleSimpleResponse(
            Response<BaseResponse<String>> response,
            SimpleCallback callback,
            String defaultMessage
    ) {
        if (!response.isSuccessful()) {
            callback.onError(defaultMessage + ". Mã lỗi: " + response.code());
            return;
        }

        BaseResponse<String> body = response.body();

        if (body == null) {
            callback.onError("Server không trả dữ liệu");
            return;
        }

        if (!body.isSuccess()) {
            callback.onError(body.getSafeMessage());
            return;
        }

        callback.onSuccess(body.getSafeMessage());
    }

    public interface AddressListCallback {
        void onSuccess(List<AddressResponse> addresses);

        void onError(String message);
    }

    public interface AddressActionCallback {
        void onSuccess(AddressResponse address, String message);

        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess(String message);

        void onError(String message);
    }
}