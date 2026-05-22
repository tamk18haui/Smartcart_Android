package com.gr6.smartcart_android.seller.order.repository;

import android.content.Context;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.order.api.SellerOrderApiService;
import com.gr6.smartcart_android.seller.order.model.OrderDetailResponse;
import com.gr6.smartcart_android.seller.order.model.OrderListResponse;
import com.gr6.smartcart_android.seller.order.model.UpdateShopOrderStatusRequest;

import java.util.List;

import retrofit2.Call;

public class SellerOrderRepository {
    private final SellerOrderApiService apiService;

    public SellerOrderRepository(Context context) {
        apiService = ApiClient.createService(context, SellerOrderApiService.class);
    }

    public Call<BaseResponse<List<OrderListResponse>>> getOrders(String keyword) {
        return apiService.getOrders(keyword);
    }

    public Call<BaseResponse<OrderDetailResponse>> getOrderDetail(Long id) {
        return apiService.getOrderDetail(id);
    }

    public Call<BaseResponse<String>> updateStatus(Long id, String status, String reason) {
        return apiService.updateStatus(id, new UpdateShopOrderStatusRequest(status, reason));
    }

    public Call<BaseResponse<String>> confirmOrder(Long id) {
        return apiService.confirmOrder(id);
    }
}
