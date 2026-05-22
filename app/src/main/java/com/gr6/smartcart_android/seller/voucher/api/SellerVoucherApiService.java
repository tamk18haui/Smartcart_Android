package com.gr6.smartcart_android.seller.voucher.api;

import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.seller.voucher.model.VoucherRequest;
import com.gr6.smartcart_android.seller.voucher.model.VoucherResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * API quản lý voucher dành cho Seller.
 * Đồng bộ với backend hiện có:
 * /api/v2/seller/vouchers
 */
public interface SellerVoucherApiService {

    @GET("api/v2/seller/vouchers")
    Call<BaseResponse<List<VoucherResponse>>> getMyVouchers();

    @POST("api/v2/seller/vouchers")
    Call<BaseResponse<VoucherResponse>> createVoucher(@Body VoucherRequest request);

    @PUT("api/v2/seller/vouchers/{voucherId}")
    Call<BaseResponse<VoucherResponse>> updateVoucher(
            @Path("voucherId") Long voucherId,
            @Body VoucherRequest request
    );

    @DELETE("api/v2/seller/vouchers/{voucherId}")
    Call<BaseResponse<Object>> deactivateVoucher(@Path("voucherId") Long voucherId);
}


