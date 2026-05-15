package com.gr6.smartcart_android.buyer.checkout;

import java.util.ArrayList;
import java.util.List;

public class CheckoutSelectedShop {

    private Long shopId;
    private String shopName;
    private String voucherCode;
    private List<CheckoutSelectedItem> items;

    public CheckoutSelectedShop() {
    }

    public CheckoutSelectedShop(
            Long shopId,
            String shopName,
            String voucherCode,
            List<CheckoutSelectedItem> items
    ) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.voucherCode = voucherCode;
        this.items = items;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        if (shopName == null || shopName.trim().isEmpty()) return "SmartCart Shop";
        return shopName;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public List<CheckoutSelectedItem> getItems() {
        if (items == null) return new ArrayList<>();
        return items;
    }

    public static class CheckoutSelectedItem {

        private Long variantId;
        private Long productId;
        private String productName;
        private String variantText;
        private String imageUrl;
        private Double price;
        private Integer quantity;

        public CheckoutSelectedItem() {
        }

        public CheckoutSelectedItem(
                Long variantId,
                Long productId,
                String productName,
                String variantText,
                String imageUrl,
                Double price,
                Integer quantity
        ) {
            this.variantId = variantId;
            this.productId = productId;
            this.productName = productName;
            this.variantText = variantText;
            this.imageUrl = imageUrl;
            this.price = price;
            this.quantity = quantity;
        }

        public Long getVariantId() {
            return variantId;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            if (productName == null || productName.trim().isEmpty()) return "Sản phẩm SmartCart";
            return productName;
        }

        public String getVariantText() {
            if (variantText == null || variantText.trim().isEmpty()) return "Phân loại";
            return variantText;
        }

        public String getImageUrl() {
            if (imageUrl == null) return "";
            return imageUrl;
        }

        public Double getPrice() {
            return price == null ? 0.0 : price;
        }

        public Integer getQuantity() {
            return quantity == null ? 1 : quantity;
        }
    }
}