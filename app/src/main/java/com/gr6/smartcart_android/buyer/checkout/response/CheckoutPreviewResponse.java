package com.gr6.smartcart_android.buyer.checkout.response;

import java.util.ArrayList;
import java.util.List;

public class CheckoutPreviewResponse {

    private AddressPreviewDto defaultAddress;
    private List<ShopPreview> shops;
    private Long totalItemPrice;
    private Long totalShippingFee;
    private Long totalDiscount;
    private Long totalAmount;

    public AddressPreviewDto getDefaultAddress() {
        return defaultAddress;
    }

    public List<ShopPreview> getShops() {
        if (shops == null) return new ArrayList<>();
        return shops;
    }

    public Long getTotalItemPrice() {
        return totalItemPrice == null ? 0L : totalItemPrice;
    }

    public Long getTotalShippingFee() {
        return totalShippingFee == null ? 0L : totalShippingFee;
    }

    public Long getTotalDiscount() {
        return totalDiscount == null ? 0L : totalDiscount;
    }

    public Long getTotalAmount() {
        return totalAmount == null ? 0L : totalAmount;
    }

    public static class AddressPreviewDto {

        private Long addressId;
        private String receiverName;
        private String receiverPhone;
        private String fullAddress;

        public Long getAddressId() {
            return addressId;
        }

        public String getReceiverName() {
            return receiverName;
        }

        public String getReceiverPhone() {
            return receiverPhone;
        }

        public String getFullAddress() {
            return fullAddress;
        }
    }

    public static class ShopPreview {

        private Long shopId;
        private String shopName;
        private List<ItemPreview> items;
        private Long shopItemTotal;
        private Long shopShippingFee;
        private Long shopDiscount;
        private Long subtotal;

        public Long getShopId() {
            return shopId;
        }

        public String getShopName() {
            if (shopName == null || shopName.trim().isEmpty()) return "SmartCart Shop";
            return shopName;
        }

        public List<ItemPreview> getItems() {
            if (items == null) return new ArrayList<>();
            return items;
        }

        public Long getShopItemTotal() {
            return shopItemTotal == null ? 0L : shopItemTotal;
        }

        public Long getShopShippingFee() {
            return shopShippingFee == null ? 0L : shopShippingFee;
        }

        public Long getShopDiscount() {
            return shopDiscount == null ? 0L : shopDiscount;
        }

        public Long getSubtotal() {
            return subtotal == null ? 0L : subtotal;
        }
    }

    public static class ItemPreview {

        private String productName;
        private String variantImageUrl;
        private Long price;
        private Integer quantity;
        private String optionValues;

        public String getProductName() {
            if (productName == null || productName.trim().isEmpty()) return "Sản phẩm SmartCart";
            return productName;
        }

        public String getVariantImageUrl() {
            return variantImageUrl;
        }

        public Long getPrice() {
            return price == null ? 0L : price;
        }

        public Integer getQuantity() {
            return quantity == null ? 1 : quantity;
        }

        public String getOptionValues() {
            if (optionValues == null || optionValues.trim().isEmpty()) return "Phân loại";
            return optionValues;
        }
    }
}