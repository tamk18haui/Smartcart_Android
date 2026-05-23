package com.gr6.smartcart_android.seller.voucher;

import com.gr6.smartcart_android.seller.voucher.response.VoucherResponse;

import java.util.ArrayList;
import java.util.List;

public class VoucherListState {
    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final List<VoucherResponse> vouchers;
    private final String message;

    private VoucherListState(Status status, List<VoucherResponse> vouchers, String message) {
        this.status = status;
        this.vouchers = vouchers == null ? new ArrayList<>() : vouchers;
        this.message = message;
    }

    public static VoucherListState loading() { return new VoucherListState(Status.LOADING, new ArrayList<>(), null); }
    public static VoucherListState success(List<VoucherResponse> vouchers, String message) { return new VoucherListState(Status.SUCCESS, vouchers, message); }
    public static VoucherListState error(String message) { return new VoucherListState(Status.ERROR, new ArrayList<>(), message); }

    public Status getStatus() { return status; }
    public List<VoucherResponse> getVouchers() { return vouchers; }
    public String getMessage() { return message; }
    public boolean isLoading() { return status == Status.LOADING; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isError() { return status == Status.ERROR; }
}
