package com.gr6.smartcart_android.seller.repository;

import android.content.Context;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.base.PageResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.api.SellerCatalogApiService;
import com.gr6.smartcart_android.seller.model.CategoryResponse;
import com.gr6.smartcart_android.seller.model.ProductRequest;
import com.gr6.smartcart_android.seller.model.ProductResponse;
import com.gr6.smartcart_android.seller.model.SellerShopInfoResponse;

import java.util.List;

import retrofit2.Call;

public class SellerProductRepository {
    private final SellerCatalogApiService apiService;

    public SellerProductRepository(Context context) {
        apiService = ApiClient.createService(context, SellerCatalogApiService.class);
    }

    public Call<BaseResponse<List<CategoryResponse>>> getCategories() {
        return apiService.getCategories();
    }

    public Call<BaseResponse<List<String>>> getBrandSuggestions(String keyword) {
        return apiService.getBrandSuggestions(keyword);
    }

    public Call<BaseResponse<ProductResponse>> createProduct(ProductRequest request) {
        return apiService.createProduct(request);
    }

    public Call<BaseResponse<SellerShopInfoResponse>> getMyShopInfo() {
        return apiService.getMyShopInfo();
    }

    public Call<BaseResponse<PageResponse<ProductResponse>>> getProductsByShop(Long shopId, int page, int size) {
        return apiService.getProductsByShop(shopId, page, size);
    }
}