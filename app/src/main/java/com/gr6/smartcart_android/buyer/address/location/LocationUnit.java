package com.gr6.smartcart_android.buyer.address.location;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class LocationUnit {

    @SerializedName("code")
    private Integer code;

    @SerializedName("name")
    private String name;

    @SerializedName("division_type")
    private String divisionType;

    @SerializedName("codename")
    private String codename;

    @SerializedName("province_code")
    private Integer provinceCode;

    @SerializedName("wards")
    private List<LocationUnit> wards;

    public Integer getCode() {
        return code;
    }

    public int getSafeCode() {
        if (code == null) return -1;
        return code;
    }

    public String getName() {
        if (name == null || name.trim().isEmpty()) return "Không rõ";
        return name;
    }

    public String getDivisionType() {
        return divisionType;
    }

    public String getCodename() {
        return codename;
    }

    public Integer getProvinceCode() {
        return provinceCode;
    }

    public List<LocationUnit> getWards() {
        if (wards == null) return new ArrayList<>();
        return wards;
    }
}