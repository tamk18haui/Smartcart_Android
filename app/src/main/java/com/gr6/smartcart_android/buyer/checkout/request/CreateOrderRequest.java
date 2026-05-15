package com.gr6.smartcart_android.buyer.checkout.request;

import java.util.List;

public class CreateOrderRequest {

    private Long addressId;
    private String paymentMethod;
    private String paymentProvider;
    private String checkoutSource;
    private String checkoutToken;
    private List<ShopOrderRequest> shopOrders;

    public CreateOrderRequest(
            Long addressId,
            String paymentMethod,
            String paymentProvider,
            String checkoutSource,
            String checkoutToken,
            List<ShopOrderRequest> shopOrders
    ) {
        this.addressId = addressId;
        this.paymentMethod = paymentMethod;
        this.paymentProvider = paymentProvider;
        this.checkoutSource = checkoutSource;
        this.checkoutToken = checkoutToken;
        this.shopOrders = shopOrders;
    }

    public Long getAddressId() {
        return addressId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getCheckoutSource() {
        return checkoutSource;
    }

    public String getCheckoutToken() {
        return checkoutToken;
    }

    public List<ShopOrderRequest> getShopOrders() {
        return shopOrders;
    }

    public static class ShopOrderRequest {

        private Long shopId;
        private String voucherCode;
        private List<ItemRequest> items;

        public ShopOrderRequest(
                Long shopId,
                String voucherCode,
                List<ItemRequest> items
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

        public List<ItemRequest> getItems() {
            return items;
        }
    }

    public static class ItemRequest {

        private Long variantId;
        private Integer quantity;

        public ItemRequest(Long variantId, Integer quantity) {
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