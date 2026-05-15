package com.gr6.smartcart_android.buyer.address.location.api;

import com.gr6.smartcart_android.buyer.address.location.LocationUnit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VietnamAddressApiService {

    @GET("p/")
    Call<List<LocationUnit>> getProvinces();

    @GET("p/{provinceCode}")
    Call<LocationUnit> getProvinceDetail(
            @Path("provinceCode") int provinceCode,
            @Query("depth") int depth
    );
}