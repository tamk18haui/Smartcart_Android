package com.gr6.smartcart_android.buyer.main.api;

import com.gr6.smartcart_android.buyer.main.request.SearchProductRequest;
import com.gr6.smartcart_android.buyer.main.response.HomeCategoryResponse;
import com.gr6.smartcart_android.buyer.main.response.HomeProductResponse;
import com.gr6.smartcart_android.buyer.main.response.ProductPageResponse;
import com.gr6.smartcart_android.buyer.main.response.RecommendationPageResponse;
import com.gr6.smartcart_android.common.base.BaseResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface BuyerHomeApiService {

    @GET("api/v1/categories")
    Call<BaseResponse<List<HomeCategoryResponse>>> getCategories();

    @GET("api/v1/storefront/discovery/home-products")
    Call<BaseResponse<List<HomeProductResponse>>> getHomeProducts();

    @POST("api/v1/storefront/discovery/search")
    Call<BaseResponse<ProductPageResponse>> searchProducts(
            @Body SearchProductRequest request,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v3/recommendations/ai/trending")
    Call<RecommendationPageResponse> getAiTrending(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v3/recommendations/ai/personal")
    Call<RecommendationPageResponse> getAiPersonal(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v3/recommendations/ai/search")
    Call<RecommendationPageResponse> getAiSearch(
            @Query("keyword") String keyword,
            @Query("page") int page,
            @Query("size") int size
    );

    @Multipart
    @POST("api/v3/recommendations/ai/image-search")
    Call<RecommendationPageResponse> searchByImage(
            @Part MultipartBody.Part file,
            @Query("page") int page,
            @Query("size") int size
    );
}