package com.gr6.smartcart_android.buyer.main.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.buyer.main.api.BuyerHomeApiService;
import com.gr6.smartcart_android.buyer.main.request.SearchProductRequest;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.main.response.ProductPageResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyerHomeRepository {

    private final BuyerHomeApiService apiService;

    public BuyerHomeRepository(Context context) {
        apiService = ApiClient.createService(context, BuyerHomeApiService.class);
    }

    public void getCategories(HomeCallback<List<HomeCategoryResponse>> callback) {
        apiService.getCategories().enqueue(new Callback<BaseResponse<List<HomeCategoryResponse>>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<List<HomeCategoryResponse>>> call,
                    @NonNull Response<BaseResponse<List<HomeCategoryResponse>>> response
            ) {
                handleResponse(response, callback, "Không lấy được danh mục");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<List<HomeCategoryResponse>>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    public void searchProducts(
            SearchProductRequest request,
            int page,
            int size,
            HomeCallback<ProductPageResponse> callback
    ) {
        apiService.searchProducts(request, page, size).enqueue(new Callback<BaseResponse<ProductPageResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<ProductPageResponse>> call,
                    @NonNull Response<BaseResponse<ProductPageResponse>> response
            ) {
                handleResponse(response, callback, "Không lấy được sản phẩm");
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<ProductPageResponse>> call,
                    @NonNull Throwable t
            ) {
                callback.onError("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private <T> void handleResponse(
            Response<BaseResponse<T>> response,
            HomeCallback<T> callback,
            String defaultMessage
    ) {
        if (!response.isSuccessful()) {
            callback.onError(defaultMessage + ". Mã lỗi: " + response.code());
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

        callback.onSuccess(body.getData());
    }

    public interface HomeCallback<T> {
        void onSuccess(T data);

        void onError(String message);
    }
}