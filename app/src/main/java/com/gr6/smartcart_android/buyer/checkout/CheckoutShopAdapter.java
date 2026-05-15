package com.gr6.smartcart_android.buyer.checkout;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gr6.smartcart_android.R;
import com.gr6.smartcart_android.buyer.checkout.response.CheckoutPreviewResponse;
import com.gr6.smartcart_android.common.utils.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutShopAdapter extends RecyclerView.Adapter<CheckoutShopAdapter.ShopViewHolder> {

    private final Context context;
    private final List<CheckoutPreviewResponse.ShopPreview> shops = new ArrayList<>();
    private final Map<Long, String> voucherCodes = new LinkedHashMap<>();

    private OnShopVoucherClickListener voucherClickListener;

    public CheckoutShopAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<CheckoutPreviewResponse.ShopPreview> data) {
        shops.clear();

        if (data != null) {
            shops.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void setOnShopVoucherClickListener(OnShopVoucherClickListener listener) {
        this.voucherClickListener = listener;
    }

    public void setVoucherCode(Long shopId, String code) {
        if (shopId == null) return;

        if (code == null || code.trim().isEmpty()) {
            voucherCodes.remove(shopId);
        } else {
            voucherCodes.put(shopId, code.trim().toUpperCase(Locale.ROOT));
        }

        notifyDataSetChanged();
    }

    public String getVoucherCode(Long shopId) {
        if (shopId == null) return null;
        return voucherCodes.get(shopId);
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        CheckoutPreviewResponse.ShopPreview shop = shops.get(position);

        Long shopId = shop.getShopId();
        String currentVoucher = getVoucherCode(shopId);

        holder.txtShopName.setText(shop.getShopName());

        holder.layoutProducts.removeAllViews();

        int quantityCount = 0;

        for (CheckoutPreviewResponse.ItemPreview item : shop.getItems()) {
            quantityCount += item.getQuantity();
            holder.layoutProducts.addView(createProductRow(item));
        }

        holder.txtShopTotalLabel.setText("Tổng số tiền (" + quantityCount + " sản phẩm):");
        holder.txtShopItemTotal.setText(formatVnd(shop.getShopItemTotal()));
        holder.txtShippingFee.setText(formatVnd(shop.getShopShippingFee()));

        if (shop.getShopDiscount() > 0) {
            holder.txtShopDiscount.setText("-" + formatVnd(shop.getShopDiscount()));
            holder.txtShopDiscount.setTextColor(ContextCompat.getColor(context, R.color.success));
        } else {
            holder.txtShopDiscount.setText("-0đ");
            holder.txtShopDiscount.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }

        holder.txtShopTotal.setText(formatVnd(shop.getSubtotal()));

        if (currentVoucher == null || currentVoucher.trim().isEmpty()) {
            holder.txtShopVoucher.setText("Chọn mã giảm giá  ›");
            holder.txtShopVoucher.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        } else {
            holder.txtShopVoucher.setText(currentVoucher + "  ✕");
            holder.txtShopVoucher.setTextColor(ContextCompat.getColor(context, R.color.price_red));
        }

        holder.layoutShopVoucher.setOnClickListener(v -> {
            if (voucherClickListener != null) {
                voucherClickListener.onClick(shopId, shop.getShopName(), currentVoucher);
            }
        });
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    private View createProductRow(CheckoutPreviewResponse.ItemPreview item) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, 0);

        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundResource(R.drawable.bg_cart_image);
        image.setPadding(dp(5), dp(5), dp(5), dp(5));

        if (item.getVariantImageUrl() != null && !item.getVariantImageUrl().trim().isEmpty()) {
            ImageLoader.load(context, item.getVariantImageUrl(), image);
        } else {
            image.setImageResource(R.drawable.ic_cart);
        }

        row.addView(image, new LinearLayout.LayoutParams(dp(76), dp(76)));

        LinearLayout info = new LinearLayout(context);
        info.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        infoParams.leftMargin = dp(12);
        row.addView(info, infoParams);

        TextView name = createText(item.getProductName(), 14, R.color.text_primary, Typeface.BOLD);
        name.setMaxLines(2);
        name.setEllipsize(TextUtils.TruncateAt.END);
        info.addView(name);

        TextView variant = createText(item.getOptionValues(), 12, R.color.text_secondary, Typeface.NORMAL);

        LinearLayout.LayoutParams variantParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        variantParams.topMargin = dp(4);
        info.addView(variant, variantParams);

        TextView price = createText(
                formatVnd(item.getPrice()) + "  x" + item.getQuantity(),
                14,
                R.color.price_red,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        priceParams.topMargin = dp(6);
        info.addView(price, priceParams);

        return row;
    }

    private TextView createText(String text, int sp, int colorRes, int style) {
        TextView textView = new TextView(context);
        textView.setText(text == null ? "" : text);
        textView.setTextSize(sp);
        textView.setTextColor(ContextCompat.getColor(context, colorRes));
        textView.setTypeface(null, style);
        return textView;
    }

    private String formatVnd(Long value) {
        long amount = value == null ? 0L : value;
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + "đ";
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {

        TextView txtShopName;
        TextView txtShopVoucher;
        TextView txtShopItemTotal;
        TextView txtShippingFee;
        TextView txtShopDiscount;
        TextView txtShopTotalLabel;
        TextView txtShopTotal;
        LinearLayout layoutProducts;
        LinearLayout layoutShopVoucher;

        ShopViewHolder(@NonNull View itemView) {
            super(itemView);

            txtShopName = itemView.findViewById(R.id.txtShopName);
            txtShopVoucher = itemView.findViewById(R.id.txtShopVoucher);
            txtShopItemTotal = itemView.findViewById(R.id.txtShopItemTotal);
            txtShippingFee = itemView.findViewById(R.id.txtShippingFee);
            txtShopDiscount = itemView.findViewById(R.id.txtShopDiscount);
            txtShopTotalLabel = itemView.findViewById(R.id.txtShopTotalLabel);
            txtShopTotal = itemView.findViewById(R.id.txtShopTotal);
            layoutProducts = itemView.findViewById(R.id.layoutProducts);
            layoutShopVoucher = itemView.findViewById(R.id.layoutShopVoucher);
        }
    }

    public interface OnShopVoucherClickListener {
        void onClick(Long shopId, String shopName, String currentVoucherCode);
    }
}