package com.gr6.smartcart_android.buyer.address.location.repository;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.address.location.LocationUnit;
import com.gr6.smartcart_android.buyer.address.location.api.VietnamAddressApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VietnamAddressRepository {

    private static final String BASE_URL = "https://provinces.open-api.vn/api/v2/";

    private final VietnamAddressApiService apiService;

    public VietnamAddressRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(VietnamAddressApiService.class);
    }

    public void getProvinces(LocationListCallback callback) {
        apiService.getProvinces().enqueue(new Callback<List<LocationUnit>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<LocationUnit>> call,
                    @NonNull Response<List<LocationUnit>> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Không lấy được danh sách tỉnh/thành phố. Mã lỗi: " + response.code());
                    return;
                }

                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<LocationUnit>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được API tỉnh thành v2: " + t.getMessage());
            }
        });
    }

    public void getWardsByProvince(int provinceCode, LocationListCallback callback) {
        apiService.getProvinceDetail(provinceCode, 2).enqueue(new Callback<LocationUnit>() {
            @Override
            public void onResponse(
                    @NonNull Call<LocationUnit> call,
                    @NonNull Response<LocationUnit> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Không lấy được danh sách phường/xã. Mã lỗi: " + response.code());
                    return;
                }

                LocationUnit province = response.body();

                if (province == null) {
                    callback.onError("Dữ liệu phường/xã không hợp lệ");
                    return;
                }

                callback.onSuccess(province.getWards());
            }

            @Override
            public void onFailure(
                    @NonNull Call<LocationUnit> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được API phường/xã v2: " + t.getMessage());
            }
        });
    }

    public interface LocationListCallback {
        void onSuccess(List<LocationUnit> data);

        void onError(String message);
    }
}