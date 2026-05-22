package com.gr6.smartcart_android.seller.voucher;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.common.base.BaseActivity;
import com.gr6.smartcart_android.common.base.BaseResponse;
import com.gr6.smartcart_android.common.network.ApiClient;
import com.gr6.smartcart_android.seller.utils.ApiErrorUtils;
import com.gr6.smartcart_android.seller.voucher.api.SellerVoucherApiService;
import com.gr6.smartcart_android.seller.voucher.model.VoucherRequest;
import com.gr6.smartcart_android.seller.voucher.model.VoucherResponse;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateVoucherActivity extends BaseActivity {

    public static final String EXTRA_VOUCHER_ID = "extra_voucher_id";
    public static final String EXTRA_CODE = "extra_code";
    public static final String EXTRA_DISCOUNT_TYPE = "extra_discount_type";
    public static final String EXTRA_DISCOUNT_VALUE = "extra_discount_value";
    public static final String EXTRA_MIN_ORDER_VALUE = "extra_min_order_value";
    public static final String EXTRA_MAX_DISCOUNT_AMOUNT = "extra_max_discount_amount";
    public static final String EXTRA_USAGE_LIMIT = "extra_usage_limit";
    public static final String EXTRA_START_DATE = "extra_start_date";
    public static final String EXTRA_END_DATE = "extra_end_date";

    private EditText edtProgramName;
    private EditText edtCode;
    private EditText edtDiscountValue;
    private EditText edtMaxDiscount;
    private EditText edtMinOrder;
    private EditText edtUsageLimit;
    private TextView txtStartDate;
    private TextView txtEndDate;
    private TextView txtPreviewTitle;
    private TextView txtPreviewSubtitle;
    private Button btnFixed;
    private Button btnPercent;
    private Button btnSave;

    private SellerVoucherApiService apiService;
    private String discountType = "FIXED";
    private String startDateValue;
    private String endDateValue;
    private Long editingVoucherId;

    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_create_voucher);

        apiService = ApiClient.createService(this, SellerVoucherApiService.class);

        bindViews();
        setupListeners();
        readEditDataIfAny();
        refreshDiscountTypeUi();
        refreshPreview();
    }

    private void bindViews() {
        edtProgramName = findViewById(R.id.edtProgramName);
        edtCode = findViewById(R.id.edtCode);
        edtDiscountValue = findViewById(R.id.edtDiscountValue);
        edtMaxDiscount = findViewById(R.id.edtMaxDiscount);
        edtMinOrder = findViewById(R.id.edtMinOrder);
        edtUsageLimit = findViewById(R.id.edtUsageLimit);
        txtStartDate = findViewById(R.id.txtStartDate);
        txtEndDate = findViewById(R.id.txtEndDate);
        txtPreviewTitle = findViewById(R.id.txtPreviewTitle);
        txtPreviewSubtitle = findViewById(R.id.txtPreviewSubtitle);
        btnFixed = findViewById(R.id.btnFixed);
        btnPercent = findViewById(R.id.btnPercent);
        btnSave = findViewById(R.id.btnSave);

        TextView btnBack = findViewById(R.id.btnBack);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnFixed.setOnClickListener(v -> {
            discountType = "FIXED";
            refreshDiscountTypeUi();
            refreshPreview();
        });

        btnPercent.setOnClickListener(v -> {
            discountType = "PERCENT";
            refreshDiscountTypeUi();
            refreshPreview();
        });

        txtStartDate.setOnClickListener(v -> showDatePicker(true));
        txtEndDate.setOnClickListener(v -> showDatePicker(false));

        btnSave.setOnClickListener(v -> submitVoucher());

        TextWatcher watcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                refreshPreview();
            }
        };

        edtProgramName.addTextChangedListener(watcher);
        edtCode.addTextChangedListener(watcher);
        edtDiscountValue.addTextChangedListener(watcher);
        edtMaxDiscount.addTextChangedListener(watcher);
        edtMinOrder.addTextChangedListener(watcher);
    }

    private void readEditDataIfAny() {
        editingVoucherId = null;
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_VOUCHER_ID)) {
            return;
        }

        editingVoucherId = getIntent().getLongExtra(EXTRA_VOUCHER_ID, -1L);
        if (editingVoucherId <= 0) {
            editingVoucherId = null;
            return;
        }

        TextView txtTitle = findViewById(R.id.txtScreenTitle);
        txtTitle.setText("Sửa Voucher");

        edtCode.setText(getIntent().getStringExtra(EXTRA_CODE));
        edtCode.setEnabled(false);

        String extraType = getIntent().getStringExtra(EXTRA_DISCOUNT_TYPE);
        if ("PERCENT".equalsIgnoreCase(extraType)) {
            discountType = "PERCENT";
        } else {
            discountType = "FIXED";
        }

        long discountValue = getIntent().getLongExtra(EXTRA_DISCOUNT_VALUE, 0L);
        long minOrderValue = getIntent().getLongExtra(EXTRA_MIN_ORDER_VALUE, 0L);
        long maxDiscountAmount = getIntent().getLongExtra(EXTRA_MAX_DISCOUNT_AMOUNT, 0L);
        int usageLimit = getIntent().getIntExtra(EXTRA_USAGE_LIMIT, 1);

        if (discountValue > 0) edtDiscountValue.setText(String.valueOf(discountValue));
        if (minOrderValue > 0) edtMinOrder.setText(String.valueOf(minOrderValue));
        if (maxDiscountAmount > 0) edtMaxDiscount.setText(String.valueOf(maxDiscountAmount));
        edtUsageLimit.setText(String.valueOf(Math.max(usageLimit, 1)));

        String extraStartDate = getIntent().getStringExtra(EXTRA_START_DATE);
        String extraEndDate = getIntent().getStringExtra(EXTRA_END_DATE);

        Date start = parseAnyDate(extraStartDate);
        Date end = parseAnyDate(extraEndDate);

        if (start != null) {
            startDateValue = apiDateFormat.format(start);
            txtStartDate.setText(displayDateFormat.format(start));
        }

        if (end != null) {
            endDateValue = apiDateFormat.format(end);
            txtEndDate.setText(displayDateFormat.format(end));
        }
    }

    private void refreshDiscountTypeUi() {
        boolean isFixed = "FIXED".equals(discountType);

        btnFixed.setSelected(isFixed);
        btnPercent.setSelected(!isFixed);

        btnFixed.setTextColor(isFixed ? getColorCompat(android.R.color.white) : getColorFromHex("#8B2F13"));
        btnPercent.setTextColor(!isFixed ? getColorCompat(android.R.color.white) : getColorFromHex("#8B2F13"));

        btnFixed.setBackgroundResource(isFixed ? R.drawable.bg_button_orange_round : R.drawable.bg_input_border);
        btnPercent.setBackgroundResource(!isFixed ? R.drawable.bg_button_orange_round : R.drawable.bg_input_border);

        edtMaxDiscount.setEnabled(!isFixed);
        edtMaxDiscount.setAlpha(isFixed ? 0.45f : 1f);

        if (isFixed) {
            edtMaxDiscount.setText(edtDiscountValue.getText().toString().trim());
        }
    }

    private void showDatePicker(boolean isStart) {
        Calendar calendar = Calendar.getInstance();

        Date current = parseAnyDate(isStart ? startDateValue : endDateValue);
        if (current != null) {
            calendar.setTime(current);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (isStart) {
                        selected.set(Calendar.HOUR_OF_DAY, 0);
                        selected.set(Calendar.MINUTE, 0);
                        selected.set(Calendar.SECOND, 0);
                        startDateValue = apiDateFormat.format(selected.getTime());
                        txtStartDate.setText(displayDateFormat.format(selected.getTime()));
                    } else {
                        selected.set(Calendar.HOUR_OF_DAY, 23);
                        selected.set(Calendar.MINUTE, 59);
                        selected.set(Calendar.SECOND, 59);
                        endDateValue = apiDateFormat.format(selected.getTime());
                        txtEndDate.setText(displayDateFormat.format(selected.getTime()));
                    }

                    refreshPreview();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void submitVoucher() {
        String code = getText(edtCode).toUpperCase(Locale.US);
        long discountValue = parseLong(getText(edtDiscountValue));
        long minOrderValue = parseLong(getText(edtMinOrder));
        long maxDiscountAmount = parseLong(getText(edtMaxDiscount));
        int usageLimit = (int) parseLong(getText(edtUsageLimit));

        if (code.isEmpty()) {
            showToast("Vui lòng nhập mã voucher");
            edtCode.requestFocus();
            return;
        }

        if (!code.matches("^[A-Z0-9]{5,20}$")) {
            showToast("Mã voucher phải viết hoa, không dấu, không khoảng trắng, 5-20 ký tự");
            edtCode.requestFocus();
            return;
        }

        if (discountValue <= 0) {
            showToast("Vui lòng nhập mức giảm lớn hơn 0");
            edtDiscountValue.requestFocus();
            return;
        }

        if ("PERCENT".equals(discountType) && discountValue > 100) {
            showToast("Giảm theo phần trăm phải từ 1 đến 100");
            edtDiscountValue.requestFocus();
            return;
        }

        if ("FIXED".equals(discountType)) {
            maxDiscountAmount = discountValue;
        }

        if (usageLimit <= 0) {
            showToast("Lượt sử dụng tối đa phải lớn hơn 0");
            edtUsageLimit.requestFocus();
            return;
        }

        if (startDateValue == null || startDateValue.trim().isEmpty()) {
            showToast("Vui lòng chọn ngày bắt đầu");
            return;
        }

        if (endDateValue == null || endDateValue.trim().isEmpty()) {
            showToast("Vui lòng chọn ngày kết thúc");
            return;
        }

        Date startDate = parseAnyDate(startDateValue);
        Date endDate = parseAnyDate(endDateValue);
        if (startDate != null && endDate != null && endDate.before(startDate)) {
            showToast("Ngày kết thúc phải sau ngày bắt đầu");
            return;
        }

        VoucherRequest request = new VoucherRequest(
                code,
                discountType,
                discountValue,
                minOrderValue,
                maxDiscountAmount <= 0 ? null : maxDiscountAmount,
                usageLimit,
                startDateValue,
                endDateValue
        );

        showLoading();

        Call<BaseResponse<VoucherResponse>> call;
        if (editingVoucherId == null) {
            call = apiService.createVoucher(request);
        } else {
            call = apiService.updateVoucher(editingVoucherId, request);
        }

        call.enqueue(new Callback<BaseResponse<VoucherResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<BaseResponse<VoucherResponse>> call,
                    @NonNull Response<BaseResponse<VoucherResponse>> response
            ) {
                hideLoading();

                if (!response.isSuccessful()) {
                    showToast("Lưu voucher thất bại: " + ApiErrorUtils.extractErrorMessage(response));
                    return;
                }

                BaseResponse<VoucherResponse> body = response.body();
                if (body == null) {
                    showToast("Server không trả dữ liệu voucher");
                    return;
                }

                if (!body.isSuccess()) {
                    showToast(body.getSafeMessage());
                    return;
                }

                showToast(editingVoucherId == null ? "Tạo voucher thành công" : "Cập nhật voucher thành công");
                finish();
            }

            @Override
            public void onFailure(
                    @NonNull Call<BaseResponse<VoucherResponse>> call,
                    @NonNull Throwable t
            ) {
                hideLoading();
                showToast("Lỗi kết nối server khi lưu voucher: " + t.getMessage());
            }
        });
    }

    private void refreshPreview() {
        String code = getText(edtCode).toUpperCase(Locale.US);
        long discountValue = parseLong(getText(edtDiscountValue));
        long minOrder = parseLong(getText(edtMinOrder));
        long maxDiscount = parseLong(getText(edtMaxDiscount));

        if (code.isEmpty()) {
            code = "SMARTCART OFF";
        }

        String title;
        if ("PERCENT".equals(discountType)) {
            title = "Giảm " + (discountValue > 0 ? discountValue : 10) + "%";
            if (maxDiscount > 0) {
                title += " tối đa " + formatMoney(maxDiscount);
            }
        } else {
            title = "Giảm " + formatMoney(discountValue > 0 ? discountValue : 50000);
        }

        String subtitle = minOrder > 0
                ? "Đơn tối thiểu " + formatMoney(minOrder)
                : "Không yêu cầu đơn tối thiểu";

        txtPreviewTitle.setText(title);
        txtPreviewSubtitle.setText(subtitle);

        if ("FIXED".equals(discountType) && edtMaxDiscount.isEnabled()) {
            edtMaxDiscount.setText(edtDiscountValue.getText().toString().trim());
        }
    }

    private Date parseAnyDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;

        String normalized = value.trim();
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "MM/dd/yyyy",
                "yyyy-MM-dd"
        };

        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.US).parse(normalized);
            } catch (ParseException ignored) {
            }
        }

        return null;
    }

    private String getText(EditText editText) {
        if (editText == null || editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }

    private long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) return 0L;
        try {
            return Long.parseLong(value.trim().replace(".", "").replace(",", ""));
        } catch (Exception e) {
            return 0L;
        }
    }

    private String formatMoney(long amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return "đ" + formatter.format(amount);
    }

    private int getColorCompat(int colorRes) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return getColor(colorRes);
        }
        return getResources().getColor(colorRes);
    }

    private int getColorFromHex(String hex) {
        return android.graphics.Color.parseColor(hex);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}


