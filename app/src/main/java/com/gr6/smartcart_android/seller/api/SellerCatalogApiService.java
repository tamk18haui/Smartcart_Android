package com.gr6.smartcart_android.seller.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.base.PageResponse;
import com.gr6.smartcart_android.seller.model.CategoryResponse;
import com.gr6.smartcart_android.seller.model.ProductRequest;
import com.gr6.smartcart_android.seller.model.ProductResponse;
import com.gr6.smartcart_android.seller.model.SellerShopInfoResponse;
import com.gr6.smartcart_android.seller.model.SellerShopUpdateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SellerCatalogApiService {

    @GET("api/v1/categories")
    Call<BaseResponse<List<CategoryResponse>>> getCategories();

    @GET("api/v1/products/brands")
    Call<BaseResponse<List<String>>> getBrandSuggestions(
            @Query("keyword") String keyword
    );

    @POST("api/v1/products")
    Call<BaseResponse<ProductResponse>> createProduct(
            @Body ProductRequest request
    );

    // Lấy shop của seller đang đăng nhập.
    // Backend hiện có: GET /api/v1/shops/info
    @GET("api/v1/shops/info")
    Call<BaseResponse<SellerShopInfoResponse>> getMyShopInfo();

    // Cập nhật thông tin cơ bản của shop.
    // Backend hiện có: PUT /api/v1/shops/update
    @PUT("api/v1/shops/update")
    Call<BaseResponse<Object>> updateMyShop(
            @Body SellerShopUpdateRequest request
    );

    // Lấy danh sách sản phẩm theo shopId.
    // Backend hiện có: GET /api/v1/products/shop/{shopId}
    @GET("api/v1/products/shop/{shopId}")
    Call<BaseResponse<PageResponse<ProductResponse>>> getProductsByShop(
            @Path("shopId") Long shopId,
            @Query("page") int page,
            @Query("size") int size
    );
}
