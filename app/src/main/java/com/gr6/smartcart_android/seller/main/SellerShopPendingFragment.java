package com.gr6.smartcart_android.seller.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gr6.smartcart_android.R;

public class SellerShopPendingFragment extends Fragment {

    private static final String ARG_STATUS = "status";
    private static final String ARG_SHOP_NAME = "shop_name";
    private static final String ARG_MESSAGE = "message";
    private static final String STATUS_LOADING = "LOADING";

    public static SellerShopPendingFragment loading() {
        return newInstance(STATUS_LOADING, "Đang kiểm tra cửa hàng", "Vui lòng chờ trong giây lát...");
    }

    public static SellerShopPendingFragment newInstance(String status, String shopName, String message) {
        SellerShopPendingFragment fragment = new SellerShopPendingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status == null ? "PENDING" : status);
        args.putString(ARG_SHOP_NAME, shopName == null ? "" : shopName);
        args.putString(ARG_MESSAGE, message == null ? "" : message);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_seller_shop_pending, container, false);

        String status = getArg(ARG_STATUS, "PENDING").trim().toUpperCase();
        String shopName = getArg(ARG_SHOP_NAME, "Cửa hàng của bạn");
        String message = getArg(ARG_MESSAGE, "");

        TextView txtShopName = view.findViewById(R.id.txtPendingShopName);
        TextView txtStatusBadge = view.findViewById(R.id.txtPendingStatusBadge);
        TextView txtTitle = view.findViewById(R.id.txtPendingTitle);
        TextView txtDescription = view.findViewById(R.id.txtPendingDescription);

        txtShopName.setText(shopName.trim().isEmpty() ? "Cửa hàng SmartCart" : shopName.trim());
        txtStatusBadge.setText(toVietnameseStatus(status));

        if (STATUS_LOADING.equals(status)) {
            txtTitle.setText("Đang kiểm tra trạng thái cửa hàng");
            txtDescription.setText(message);
            return view;
        }

        if ("REJECTED".equals(status)) {
            txtTitle.setText("Cửa hàng bị từ chối duyệt");
            txtDescription.setText(message.trim().isEmpty()
                    ? "Vui lòng kiểm tra lại thông tin cửa hàng hoặc liên hệ Admin để được hỗ trợ."
                    : message);
            return view;
        }

        if ("BANNED".equals(status)) {
            txtTitle.setText("Cửa hàng đang bị khóa");
            txtDescription.setText(message.trim().isEmpty()
                    ? "Bạn chưa thể sử dụng kênh người bán. Vui lòng liên hệ Admin để biết thêm chi tiết."
                    : message);
            return view;
        }

        txtTitle.setText("Cửa hàng đang chờ duyệt");
        txtDescription.setText("Shop của bạn đã được tạo thành công và đang chờ Admin duyệt. Khi trạng thái chuyển sang ĐANG HOẠT ĐỘNG, bạn mới có thể đăng sản phẩm, quản lý đơn hàng và sử dụng đầy đủ chức năng người bán.");
        return view;
    }

    private String getArg(String key, String defaultValue) {
        Bundle args = getArguments();
        if (args == null) return defaultValue;
        String value = args.getString(key);
        return value == null ? defaultValue : value;
    }

    private String toVietnameseStatus(String status) {
        if ("ACTIVE".equals(status)) return "ĐANG HOẠT ĐỘNG";
        if ("REJECTED".equals(status)) return "BỊ TỪ CHỐI";
        if ("BANNED".equals(status)) return "BỊ KHÓA";
        if (STATUS_LOADING.equals(status)) return "ĐANG KIỂM TRA";
        return "CHỜ DUYỆT";
    }
}


