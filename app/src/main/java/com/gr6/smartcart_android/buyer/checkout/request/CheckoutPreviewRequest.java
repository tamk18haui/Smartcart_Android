package com.gr6.smartcart_android.buyer.checkout.request;

import java.util.List;

public class CheckoutPreviewRequest {

    private Long addressId;
    private String checkoutSource;
    private List<ShopOrderPreviewDto> shopOrders;

    public CheckoutPreviewRequest(
            Long addressId,
            String checkoutSource,
            List<ShopOrderPreviewDto> shopOrders
    ) {
        this.addressId = addressId;
        this.checkoutSource = checkoutSource;
        this.shopOrders = shopOrders;
    }

    public Long getAddressId() {
        return addressId;
    }

    public String getCheckoutSource() {
        return checkoutSource;
    }

    public List<ShopOrderPreviewDto> getShopOrders() {
        return shopOrders;
    }

    public static class ShopOrderPreviewDto {

        private Long shopId;
        private String voucherCode;
        private List<ItemPreviewDto> items;

        public ShopOrderPreviewDto(
                Long shopId,
                String voucherCode,
                List<ItemPreviewDto> items
        ) {
            this.shopId = shopId;
            this.voucherCode = voucherCode;
            this.items = items;
        }

        public Long getShopId() {
            return shopId;
        }

        public String getVoucherCode() {
            return voucherCode;
        }

        public List<ItemPreviewDto> getItems() {
            return items;
        }
    }

    public static class ItemPreviewDto {

        private Long variantId;
        private Integer quantity;

        public ItemPreviewDto(Long variantId, Integer quantity) {
            this.variantId = variantId;
            this.quantity = quantity;
        }

        public Long getVariantId() {
            return variantId;
        }

        public Integer getQuantity() {
            return quantity;
        }
    }
}