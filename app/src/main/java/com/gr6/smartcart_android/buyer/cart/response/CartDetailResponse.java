package com.gr6.smartcart_android.buyer.cart.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartDetailResponse {

    @SerializedName("shops")
    private List<ShopCart> shops;

    @SerializedName("totalItems")
    private Integer totalItems;

    @SerializedName("totalPrice")
    private Double totalPrice;

    @SerializedName("isEmpty")
    private Boolean isEmpty;

    public List<ShopCart> getShops() {
        if (shops == null) return new ArrayList<>();
        return shops;
    }

    public Integer getTotalItems() {
        return totalItems == null ? 0 : totalItems;
    }

    public Double getTotalPrice() {
        return totalPrice == null ? 0.0 : totalPrice;
    }

    public Boolean getEmpty() {
        return Boolean.TRUE.equals(isEmpty);
    }

    public static class ShopCart {

        @SerializedName("shopId")
        private Long shopId;

        @SerializedName("shopName")
        private String shopName;

        @SerializedName("items")
        private List<CartItem> items;

        @SerializedName("shopSubtotal")
        private Double shopSubtotal;

        private boolean selected = false;

        public Long getShopId() {
            return shopId;
        }

        public String getShopName() {
            if (shopName == null || shopName.trim().isEmpty()) return "SmartCart Shop";
            return shopName;
        }

        public List<CartItem> getItems() {
            if (items == null) return new ArrayList<>();
            return items;
        }

        public Double getShopSubtotal() {
            return shopSubtotal == null ? 0.0 : shopSubtotal;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;

            for (CartItem item : getItems()) {
                item.setSelected(selected);
            }
        }

        public boolean areAllItemsSelected() {
            if (getItems().isEmpty()) return false;

            for (CartItem item : getItems()) {
                if (!item.isSelected()) return false;
            }

            return true;
        }
    }

    public static class CartItem {

        @SerializedName("cartItemId")
        private Long cartItemId;

        @SerializedName("variantId")
        private Long variantId;

        @SerializedName("productId")
        private Long productId;

        @SerializedName("productName")
        private String productName;

        @SerializedName("name")
        private String name;

        @SerializedName("imageUrl")
        private String imageUrl;

        @SerializedName("productImageUrl")
        private String productImageUrl;

        @SerializedName("thumbnail")
        private String thumbnail;

        @SerializedName("thumbnailUrl")
        private String thumbnailUrl;

        @SerializedName("variantName")
        private String variantName;

        @SerializedName("variantSku")
        private String variantSku;

        @SerializedName("optionText")
        private String optionText;

        @SerializedName("variantAttributes")
        private String variantAttributes;

        @SerializedName("variantAttributeText")
        private String variantAttributeText;

        @SerializedName("sku")
        private String sku;

        @SerializedName("attributes")
        private Map<String, String> attributes;

        @SerializedName("price")
        private Double price;

        @SerializedName("quantity")
        private Integer quantity;

        @SerializedName("stock")
        private Integer stock;

        @SerializedName("stockQuantity")
        private Integer stockQuantity;

        @SerializedName("maxQuantity")
        private Integer maxQuantity;

        private boolean selected = false;

        public Long getCartItemId() {
            return cartItemId;
        }

        public Long getVariantId() {
            return variantId;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            if (productName != null && !productName.trim().isEmpty()) return productName.trim();
            if (name != null && !name.trim().isEmpty()) return name.trim();
            return "Sản phẩm SmartCart";
        }

        public String getImageUrl() {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) return imageUrl.trim();
            if (productImageUrl != null && !productImageUrl.trim().isEmpty()) return productImageUrl.trim();
            if (thumbnailUrl != null && !thumbnailUrl.trim().isEmpty()) return thumbnailUrl.trim();
            if (thumbnail != null && !thumbnail.trim().isEmpty()) return thumbnail.trim();
            return "";
        }

        public String getVariantText() {
            if (variantName != null && !variantName.trim().isEmpty()) return variantName.trim();
            if (optionText != null && !optionText.trim().isEmpty()) return optionText.trim();
            if (variantAttributes != null && !variantAttributes.trim().isEmpty()) return variantAttributes.trim();
            if (variantAttributeText != null && !variantAttributeText.trim().isEmpty()) return variantAttributeText.trim();

            if (attributes != null && !attributes.isEmpty()) {
                StringBuilder builder = new StringBuilder();

                for (String value : attributes.values()) {
                    if (value == null || value.trim().isEmpty()) continue;

                    if (builder.length() > 0) {
                        builder.append(", ");
                    }

                    builder.append(value.trim());
                }

                if (builder.length() > 0) {
                    return builder.toString();
                }
            }

            if (variantSku != null && !variantSku.trim().isEmpty()) return variantSku.trim();
            if (sku != null && !sku.trim().isEmpty()) return sku.trim();

            return "Phân loại";
        }

        public Double getPrice() {
            return price == null ? 0.0 : price;
        }

        public Integer getQuantity() {
            return quantity == null ? 1 : quantity;
        }

        public Integer getStock() {
            if (stock != null) return stock;
            if (stockQuantity != null) return stockQuantity;
            if (maxQuantity != null) return maxQuantity;
            return 9999;
        }

        public double getLineTotal() {
            return getPrice() * getQuantity();
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}