package com.gr6.smartcart_android.seller.order;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.common.utils.ThemeColor;
import com.gr6.smartcart_android.seller.order.api.SellerOrderApiService;
import com.gr6.smartcart_android.seller.order.response.OrderDetailResponse;
import com.gr6.smartcart_android.seller.order.response.OrderItemResponse;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShippingLabelActivity extends BaseActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private SellerOrderApiService apiService;
    private Long orderId = -1L;
    private OrderDetailResponse currentOrder;
    private Bitmap qrBitmap;

    private TextView txtLabelCode;
    private TextView txtOrderCode;
    private TextView txtReceiverName;
    private TextView txtReceiverPhone;
    private TextView txtAddress;
    private TextView txtItemCount;
    private TextView txtProductNames;
    private TextView txtTotal;
    private TextView txtCodNote;
    private TextView btnPrint;
    private ImageView imgQrCode;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_label);

        ThemeColor.applyLightStatusBar(this);
        ThemeColor.applyWhiteNavigationBar(this);

        apiService = ApiClient.createService(this, SellerOrderApiService.class);
        orderId = readOrderId();

        bindViews();
        setupEvents();

        if (orderId == null || orderId <= 0) {
            showToast("Không tìm thấy mã đơn hàng để in vận đơn");
            finish();
            return;
        }

        loadOrderDetail();
    }

    private void bindViews() {
        txtLabelCode = findViewById(R.id.txtLabelCode);
        txtOrderCode = findViewById(R.id.txtOrderCode);
        txtReceiverName = findViewById(R.id.txtReceiverName);
        txtReceiverPhone = findViewById(R.id.txtReceiverPhone);
        txtAddress = findViewById(R.id.txtAddress);
        txtItemCount = findViewById(R.id.txtItemCount);
        txtProductNames = findViewById(R.id.txtProductNames);
        txtTotal = findViewById(R.id.txtTotal);
        txtCodNote = findViewById(R.id.txtCodNote);
        btnPrint = findViewById(R.id.btnPrint);
        imgQrCode = findViewById(R.id.imgQrCode);
    }

    private void setupEvents() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPrint.setOnClickListener(v -> printShippingLabel());
    }

    private Long readOrderId() {
        try {
            if (getIntent() == null || getIntent().getExtras() == null) {
                return -1L;
            }

            Object raw = getIntent().getExtras().get(EXTRA_ORDER_ID);

            if (raw instanceof Long) return (Long) raw;
            if (raw instanceof Integer) return ((Integer) raw).longValue();
            if (raw instanceof Number) return ((Number) raw).longValue();
            if (raw instanceof String) {
                String value = ((String) raw).trim();
                return value.isEmpty() ? -1L : Long.parseLong(value);
            }
        } catch (Exception ignored) {
        }

        return -1L;
    }

    private void loadOrderDetail() {
        showLoading();
        btnPrint.setEnabled(false);
        btnPrint.setAlpha(0.55f);

        apiService.getOrderDetail(orderId).enqueue(new Callback<BaseResponse<OrderDetailResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<OrderDetailResponse>> call,
                    @NonNull Response<BaseResponse<OrderDetailResponse>> response
            ) {
                hideLoading();

                if (!response.isSuccessful()) {
                    showToast("Không tải được đơn hàng. Mã lỗi: " + response.code());
                    return;
                }

                BaseResponse<OrderDetailResponse> body = response.body();
                if (body == null || !body.isSuccess() || body.getData() == null) {
                    showToast(body == null ? "Server không trả dữ liệu" : body.getSafeMessage());
                    return;
                }

                currentOrder = body.getData();
                renderLabel();
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<OrderDetailResponse>> call,
                    @NonNull Throwable t
            ) {
                hideLoading();
                showToast("Không kết nối được server: " + t.getMessage());
            }
        });
    }

    private void renderLabel() {
        if (currentOrder == null) return;

        String status = OrderStatusHelper.normalize(currentOrder.getStatus());
        if (!canPrintShippingLabel(status)) {
            showToast("Chỉ in mã vận đơn khi đơn đã xác nhận và chưa chuyển sang đang giao");
            btnPrint.setEnabled(false);
            btnPrint.setAlpha(0.55f);
            return;
        }

        String labelCode = buildLabelCode();
        String orderCode = safeText(currentOrder.getOrderCode(), "#ORD-" + orderId);
        String qrContent = "SMARTCART|LABEL=" + labelCode + "|ORDER=" + orderCode + "|ID=" + orderId;

        qrBitmap = createQrBitmap(qrContent, 560);

        txtLabelCode.setText(labelCode);
        txtOrderCode.setText("Mã đơn: " + orderCode);
        txtReceiverName.setText("Người nhận: " + safeText(currentOrder.getReceiverName(), "Chưa có tên người nhận"));
        txtReceiverPhone.setText("SĐT: " + safeText(currentOrder.getReceiverPhone(), "Chưa có số điện thoại"));
        txtAddress.setText("Địa chỉ: " + safeText(currentOrder.getShippingAddress(), "Chưa có địa chỉ giao hàng"));
        txtItemCount.setText("Số sản phẩm: " + countItems(currentOrder.getItems()));
        txtProductNames.setText(buildProductText(currentOrder.getItems()));
        txtTotal.setText("Tổng tiền: " + formatMoney(currentOrder.getTotalAmount()));
        txtCodNote.setText("Thu hộ/COD: " + formatMoney(currentOrder.getTotalAmount()));

        if (qrBitmap != null) {
            imgQrCode.setImageBitmap(qrBitmap);
        }

        btnPrint.setEnabled(true);
        btnPrint.setAlpha(1f);
    }

    private boolean canPrintShippingLabel(String status) {
        String normalized = OrderStatusHelper.normalize(status);

        // Chỉ cho in khi đơn đã xác nhận và chưa chuyển sang đang giao.
        return "CONFIRMED".equals(normalized);
    }

    private void printShippingLabel() {
        if (currentOrder == null) {
            showToast("Đang tải đơn hàng, vui lòng chờ");
            return;
        }

        String html = buildPrintHtml();
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
                if (printManager == null) {
                    showToast("Thiết bị không hỗ trợ in");
                    return;
                }

                String jobName = "SmartCart_" + buildLabelCode();
                PrintAttributes attributes = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A6)
                        .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build();

                printManager.print(jobName, view.createPrintDocumentAdapter(jobName), attributes);
            }
        });

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private String buildPrintHtml() {
        String labelCode = buildLabelCode();
        String orderCode = safeText(currentOrder.getOrderCode(), "#ORD-" + orderId);
        String qrBase64 = bitmapToBase64(qrBitmap);

        return "<!doctype html>"
                + "<html><head><meta charset='utf-8'/>"
                + "<style>"
                + "body{font-family:Arial,sans-serif;margin:0;padding:10px;color:#111;}"
                + ".label{border:2px solid #111;padding:12px;min-height:540px;}"
                + ".top{display:flex;justify-content:space-between;align-items:flex-start;border-bottom:1px dashed #333;padding-bottom:10px;}"
                + ".brand{font-size:22px;font-weight:700;}"
                + ".code{font-size:20px;font-weight:700;margin-top:4px;}"
                + ".qr{width:120px;height:120px;}"
                + ".section{border-bottom:1px dashed #999;padding:10px 0;}"
                + ".title{font-weight:700;margin-bottom:6px;}"
                + ".big{font-size:18px;font-weight:700;}"
                + "p{margin:4px 0;font-size:13px;line-height:1.35;}"
                + "</style></head><body>"
                + "<div class='label'>"
                + "<div class='top'><div><div class='brand'>SMARTCART EXPRESS</div><div class='code'>" + escapeHtml(labelCode) + "</div><p>" + escapeHtml(orderCode) + "</p></div>"
                + "<img class='qr' src='data:image/png;base64," + qrBase64 + "'/></div>"
                + "<div class='section'><div class='title'>Người nhận</div>"
                + "<p class='big'>" + escapeHtml(safeText(currentOrder.getReceiverName(), "Chưa có tên người nhận")) + "</p>"
                + "<p>SĐT: " + escapeHtml(safeText(currentOrder.getReceiverPhone(), "Chưa có số điện thoại")) + "</p>"
                + "<p>Địa chỉ: " + escapeHtml(safeText(currentOrder.getShippingAddress(), "Chưa có địa chỉ giao hàng")) + "</p></div>"
                + "<div class='section'><div class='title'>Thông tin hàng</div>"
                + "<p>Số sản phẩm: " + countItems(currentOrder.getItems()) + "</p>"
                + "<p>Tổng tiền: " + escapeHtml(formatMoney(currentOrder.getTotalAmount())) + "</p>"
                + "<p>Thu hộ/COD: " + escapeHtml(formatMoney(currentOrder.getTotalAmount())) + "</p></div>"
                + "<div class='section'><div class='title'>Danh sách sản phẩm</div>"
                + buildItemHtml(currentOrder.getItems())
                + "</div>"
                + "<p style='text-align:center;margin-top:12px;'>Quét mã QR để kiểm tra mã vận đơn SmartCart</p>"
                + "</div></body></html>";
    }

    private String buildProductText(List<OrderItemResponse> items) {
        if (items == null || items.isEmpty()) {
            return "Không có dữ liệu sản phẩm";
        }

        StringBuilder builder = new StringBuilder();

        for (OrderItemResponse item : items) {
            if (item == null) continue;

            if (builder.length() > 0) {
                builder.append("\n");
            }

            builder.append("• ")
                    .append(safeText(item.getProductName(), "Sản phẩm"));

            String variantName = item.getVariantName();
            if (variantName != null && !variantName.trim().isEmpty()) {
                builder.append(" - ").append(variantName.trim());
            }

            builder.append(" x").append(Math.max(item.getQuantity(), 0));
        }

        return builder.length() == 0 ? "Không có dữ liệu sản phẩm" : builder.toString();
    }

    private String buildItemHtml(List<OrderItemResponse> items) {
        if (items == null || items.isEmpty()) {
            return "<p>Không có dữ liệu sản phẩm</p>";
        }

        StringBuilder builder = new StringBuilder();
        for (OrderItemResponse item : items) {
            if (item == null) continue;
            builder.append("<p>")
                    .append(escapeHtml(item.getProductName()))
                    .append(" x")
                    .append(item.getQuantity())
                    .append(" - ")
                    .append(escapeHtml(formatMoney(item.getPrice())))
                    .append("</p>");
        }

        return builder.toString();
    }

    private Bitmap createQrBitmap(String content, int size) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    size,
                    size
            );

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            showToast("Không tạo được mã QR: " + e.getMessage());
            return null;
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
    }

    private String buildLabelCode() {
        long id = orderId == null || orderId <= 0 ? 0L : orderId;
        return String.format("SCGH-%06d", id);
    }

    private int countItems(List<OrderItemResponse> items) {
        if (items == null || items.isEmpty()) return 0;

        int total = 0;
        for (OrderItemResponse item : items) {
            if (item == null) continue;
            total += Math.max(item.getQuantity(), 0);
        }
        return total;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0đ";
        return moneyFormat.format(value) + "đ";
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}


