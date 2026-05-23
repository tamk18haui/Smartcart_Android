package com.gr6.smartcart_android.seller.product.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.base.PageResponse;
import com.gr6.smartcart_android.seller.product.request.ProductRequest;
import com.gr6.smartcart_android.seller.product.response.CategoryResponse;
import com.gr6.smartcart_android.seller.product.response.ProductResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SellerProductApiService {

    @GET("api/v1/categories")
    Call<BaseResponse<List<CategoryResponse>>> getCategories();

    @GET("api/v1/products/brands")
    Call<BaseResponse<List<String>>> getBrandSuggestions(
            @Query("keyword") String keyword,
            @Query("categoryId") Long categoryId
    );

    @POST("api/v1/products")
    Call<BaseResponse<ProductResponse>> createProduct(@Body ProductRequest request);

    @GET("api/v1/products/shop/{shopId}")
    Call<BaseResponse<PageResponse<ProductResponse>>> getProductsByShop(
            @Path("shopId") Long shopId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/products/seller/{productId}")
    Call<BaseResponse<ProductResponse>> getProductForSeller(@Path("productId") Long productId);

    @PUT("api/v1/products/{productId}")
    Call<BaseResponse<ProductResponse>> updateProduct(
            @Path("productId") Long productId,
            @Body ProductRequest request
    );

    @PATCH("api/v1/products/{productId}/visibility")
    Call<BaseResponse<ProductResponse>> toggleProductVisibility(
            @Path("productId") Long productId,
            @Query("hidden") boolean hidden
    );
}