package com.gr6.smartcart_android.seller.wallet.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.base.PageResponse;
import com.gr6.smartcart_android.seller.wallet.request.AnalyticsDateFilterRequest;
import com.gr6.smartcart_android.seller.wallet.request.WithdrawCreateRequest;
import com.gr6.smartcart_android.seller.wallet.response.RevenueReportResponse;
import com.gr6.smartcart_android.seller.wallet.response.SellerSettlementResponse;
import com.gr6.smartcart_android.seller.wallet.response.WalletSummaryResponse;
import com.gr6.smartcart_android.seller.wallet.response.WalletTransactionResponse;
import com.gr6.smartcart_android.seller.wallet.response.WithdrawResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SellerWalletApiService {

    @GET("api/v3/seller/withdraw/wallet")
    Call<BaseResponse<WalletSummaryResponse>> getMyWallet();

    @GET("api/v3/seller/withdraw/wallet/transactions")
    Call<BaseResponse<PageResponse<WalletTransactionResponse>>> getMyWalletTransactions(
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("api/v3/seller/withdraw/requests")
    Call<BaseResponse<WithdrawResponse>> createWithdrawRequest(
            @Body WithdrawCreateRequest request
    );

    @GET("api/v3/seller/withdraw/requests")
    Call<BaseResponse<PageResponse<WithdrawResponse>>> getMyWithdrawRequests(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v3/seller/withdraw/settlements")
    Call<BaseResponse<PageResponse<SellerSettlementResponse>>> getMySettlements(
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("api/v3/seller/analytics/revenue")
    Call<BaseResponse<RevenueReportResponse>> getRevenueReport(
            @Body AnalyticsDateFilterRequest request
    );
}
