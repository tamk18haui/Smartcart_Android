package com.gr6.smartcart_android.buyer.product.response;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductDetailResponse {

    @SerializedName("productId")
    private Long productId;

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("productName")
    private String productName;

    @SerializedName("description")
    private String description;

    @SerializedName("productDescription")
    private String productDescription;

    @SerializedName("brand")
    private String brand;

    @SerializedName("basePrice")
    private BigDecimal basePrice;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("oldPrice")
    private BigDecimal oldPrice;

    @SerializedName("originalPrice")
    private BigDecimal originalPrice;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    @SerializedName("images")
    private List<String> images;

    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("shopId")
    private Long shopId;

    @SerializedName("shopName")
    private String shopName;

    @SerializedName("shopOwnerId")
    private Long shopOwnerId;

    @SerializedName("shopImageUrl")
    private String shopImageUrl;

    @SerializedName("shopAvatarUrl")
    private String shopAvatarUrl;

    @SerializedName("shopLogoUrl")
    private String shopLogoUrl;

    @SerializedName("totalStock")
    private Integer totalStock;

    @SerializedName("stockQuantity")
    private Integer stockQuantity;

    @SerializedName("status")
    private String status;

    @SerializedName("soldQuantity")
    private Integer soldQuantity;

    @SerializedName("sold")
    private Integer sold;

    @SerializedName("totalSold")
    private Integer totalSold;

    @SerializedName("averageRating")
    private Double averageRating;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("reviewCount")
    private Integer reviewCount;

    @SerializedName("optionGroups")
    private List<OptionGroupDTO> optionGroups;

    @SerializedName("variants")
    private List<VariantDTO> variants;

    @SerializedName("reviews")
    private List<ReviewDTO> reviews;

    @SerializedName("vouchers")
    private List<ShopVoucherDTO> vouchers;

    @SerializedName(value = "shopStatus", alternate = {"statusShop", "shop_status"})
    private String shopStatus;

    public String getShopStatus() {
        return shopStatus == null ? "" : shopStatus.trim().toUpperCase();
    }

    public boolean isShopOnline() {
        String s = getShopStatus();
        return "ACTIVE".equals(s) || "ONLINE".equals(s);
    }

    public String getShopOnlineText() {
        return isShopOnline() ? "Đang hoạt động" : "Tạm ngừng";
    }

    public Long getProductId() {
        if (productId != null) return productId;
        return id;
    }

    public Long getShopOwnerId() {
        return shopOwnerId;
    }

    public String getName() {
        if (name != null && !name.trim().isEmpty()) return name;
        if (productName != null && !productName.trim().isEmpty()) return productName;
        return "Sản phẩm SmartCart";
    }

    public String getDescription() {
        if (description != null && !description.trim().isEmpty()) return description;
        if (productDescription != null && !productDescription.trim().isEmpty()) return productDescription;
        return "Chưa có mô tả sản phẩm.";
    }

    public String getBrand() {
        if (brand == null || brand.trim().isEmpty()) return "SmartCart";
        return brand;
    }

    public BigDecimal getBasePrice() {
        if (basePrice != null) return basePrice;
        if (price != null) return price;
        VariantDTO variant = getDefaultVariant();
        if (variant != null) return variant.getPrice();
        return BigDecimal.ZERO;
    }

    public BigDecimal getOldPrice() {
        if (oldPrice != null) return oldPrice;
        if (originalPrice != null) return originalPrice;
        return null;
    }

    public List<String> getImageUrls() {
        List<String> result = new ArrayList<>();

        if (imageUrls != null) {
            for (String url : imageUrls) {
                if (url != null && !url.trim().isEmpty()) result.add(url.trim());
            }
        }

        if (images != null) {
            for (String url : images) {
                if (url != null && !url.trim().isEmpty() && !result.contains(url.trim())) {
                    result.add(url.trim());
                }
            }
        }

        if (result.isEmpty() && thumbnailUrl != null && !thumbnailUrl.trim().isEmpty()) {
            result.add(thumbnailUrl.trim());
        }

        if (result.isEmpty() && imageUrl != null && !imageUrl.trim().isEmpty()) {
            result.add(imageUrl.trim());
        }

        return result;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        if (shopName == null || shopName.trim().isEmpty()) return "SmartCart Shop";
        return shopName;
    }

    public String getShopImageUrl() {
        if (shopImageUrl != null && !shopImageUrl.trim().isEmpty()) return shopImageUrl;
        if (shopAvatarUrl != null && !shopAvatarUrl.trim().isEmpty()) return shopAvatarUrl;
        if (shopLogoUrl != null && !shopLogoUrl.trim().isEmpty()) return shopLogoUrl;
        return null;
    }

    public Integer getTotalStock() {
        if (totalStock != null) return totalStock;
        if (stockQuantity != null) return stockQuantity;

        int sum = 0;
        for (VariantDTO variant : getVariants()) {
            sum += variant.getStockQuantity();
        }

        return sum;
    }

    public String getStatus() {
        return status;
    }

    public int getSoldQuantity() {
        if (soldQuantity != null) return soldQuantity;
        if (sold != null) return sold;
        if (totalSold != null) return totalSold;
        return 0;
    }

    public double getDisplayRating() {
        if (averageRating != null) return averageRating;
        if (rating != null) return rating;
        return calculateAverageRatingFromReviews();
    }

    public double getAverageRating() {
        return getDisplayRating();
    }

    public int getReviewCount() {
        if (reviewCount != null) return reviewCount;
        return getReviews().size();
    }

    private double calculateAverageRatingFromReviews() {
        List<ReviewDTO> safeReviews = getReviews();

        if (safeReviews.isEmpty()) {
            return 0.0;
        }

        double total = 0;
        int count = 0;

        for (ReviewDTO review : safeReviews) {
            if (review.getRating() > 0) {
                total += review.getRating();
                count++;
            }
        }

        if (count == 0) return 0.0;
        return total / count;
    }

    public List<OptionGroupDTO> getOptionGroups() {
        if (optionGroups == null) return new ArrayList<>();
        return optionGroups;
    }

    public List<VariantDTO> getVariants() {
        if (variants == null) return new ArrayList<>();
        return variants;
    }

    public VariantDTO getDefaultVariant() {
        List<VariantDTO> safeVariants = getVariants();
        if (safeVariants.isEmpty()) return null;
        return safeVariants.get(0);
    }

    public List<ReviewDTO> getReviews() {
        if (reviews == null) return new ArrayList<>();
        return reviews;
    }

    public List<ShopVoucherDTO> getVouchers() {
        if (vouchers == null) return new ArrayList<>();
        return vouchers;
    }

    public static class OptionGroupDTO {

        @SerializedName("name")
        private String name;

        @SerializedName("optionName")
        private String optionName;

        @SerializedName("values")
        private List<String> values;

        @SerializedName("optionValues")
        private List<String> optionValues;

        public String getName() {
            if (name != null && !name.trim().isEmpty()) return name;
            if (optionName != null && !optionName.trim().isEmpty()) return optionName;
            return "Phân loại";
        }

        public List<String> getValues() {
            if (values != null) return values;
            if (optionValues != null) return optionValues;
            return new ArrayList<>();
        }
    }

    public static class VariantDTO {

        @SerializedName("variantId")
        private Long variantId;

        @SerializedName("id")
        private Long id;

        @SerializedName("sku")
        private String sku;

        @SerializedName("price")
        private BigDecimal price;

        @SerializedName("stockQuantity")
        private Integer stockQuantity;

        @SerializedName("stock")
        private Integer stock;

        @SerializedName("imageUrl")
        private String imageUrl;

        @SerializedName("thumbnailUrl")
        private String thumbnailUrl;

        @SerializedName("attributes")
        private Map<String, String> attributes;

        public Long getVariantId() {
            if (variantId != null) return variantId;
            return id;
        }

        public String getSku() {
            return sku;
        }

        public BigDecimal getPrice() {
            if (price != null) return price;
            return BigDecimal.ZERO;
        }

        public Integer getStockQuantity() {
            if (stockQuantity != null) return stockQuantity;
            if (stock != null) return stock;
            return 0;
        }

        public String getImageUrl() {
            if (imageUrl != null && !imageUrl.trim().isEmpty()) return imageUrl;
            return thumbnailUrl;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public String getAttributeText() {
            if (attributes != null && !attributes.isEmpty()) {
                StringBuilder builder = new StringBuilder();

                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    String value = entry.getValue();

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

            if (sku != null && !sku.trim().isEmpty()) {
                return sku;
            }

            return "Phân loại";
        }    }

    public static class ReviewDTO {

        @SerializedName("reviewId")
        private Long reviewId;

        @SerializedName("rating")
        private Integer rating;

        @SerializedName("comment")
        private String comment;

        @SerializedName("imageUrls")
        private List<String> imageUrls;

        @SerializedName("videoUrl")
        private String videoUrl;

        @SerializedName("userName")
        private String userName;

        @SerializedName("buyerName")
        private String buyerName;

        @SerializedName("buyerAvatarUrl")
        private String buyerAvatarUrl;

        @SerializedName("sellerReply")
        private String sellerReply;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("repliedAt")
        private String repliedAt;

        public Long getReviewId() {
            return reviewId;
        }

        public int getRating() {
            if (rating == null) return 0;
            if (rating < 0) return 0;
            if (rating > 5) return 5;
            return rating;
        }

        public String getComment() {
            if (comment == null || comment.trim().isEmpty()) {
                return "Người mua chưa để lại nhận xét.";
            }
            return comment.trim();
        }

        public List<String> getImageUrls() {
            List<String> result = new ArrayList<>();

            if (imageUrls == null) return result;

            for (String url : imageUrls) {
                if (url == null || url.trim().isEmpty()) continue;
                result.add(url.trim());

                if (result.size() == 4) break;
            }

            return result;
        }

        public String getVideoUrl() {
            return videoUrl == null ? "" : videoUrl.trim();
        }

        public String getUserName() {
            if (userName != null && !userName.trim().isEmpty()) {
                return userName.trim();
            }

            if (buyerName != null && !buyerName.trim().isEmpty()) {
                return buyerName.trim();
            }

            return "Người dùng SmartCart";
        }

        public String getBuyerAvatarUrl() {
            return buyerAvatarUrl == null ? "" : buyerAvatarUrl.trim();
        }

        public String getSellerReply() {
            return sellerReply == null ? "" : sellerReply.trim();
        }

        public String getCreatedAt() {
            return createdAt == null ? "" : createdAt.trim();
        }

        public String getRepliedAt() {
            return repliedAt == null ? "" : repliedAt.trim();
        }

        public boolean hasSellerReply() {
            return !getSellerReply().isEmpty();
        }

        public boolean hasVideo() {
            return !getVideoUrl().isEmpty();
        }
    }
    public static class ShopVoucherDTO {

        private Long voucherId;
        private Long shopId;
        private String code;
        private String discountType;
        private Long discountValue;
        private Long minOrderValue;
        private Long maxDiscountAmount;
        private Integer usageLimit;
        private Integer usedCount;
        private String startDate;
        private String endDate;
        private String status;

        private Boolean usable;
        private Boolean usedByCurrentUser;
        private String unavailableReason;
        private String displayTitle;
        private String displaySubtitle;

        public Long getVoucherId() {
            return voucherId;
        }

        public Long getShopId() {
            return shopId;
        }

        public String getCode() {
            return code == null ? "" : code.trim();
        }

        public String getDiscountType() {
            return discountType == null ? "" : discountType;
        }

        public Long getDiscountValue() {
            return discountValue == null ? 0L : discountValue;
        }

        public Long getMinOrderValue() {
            return minOrderValue == null ? 0L : minOrderValue;
        }

        public Long getMaxDiscountAmount() {
            return maxDiscountAmount;
        }

        public Integer getUsageLimit() {
            return usageLimit == null ? 0 : usageLimit;
        }

        public Integer getUsedCount() {
            return usedCount == null ? 0 : usedCount;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public String getStatus() {
            return status == null ? "" : status;
        }

        public boolean isUsable() {
            return usable == null || usable;
        }

        public boolean isUsedByCurrentUser() {
            return usedByCurrentUser != null && usedByCurrentUser;
        }

        public String getUnavailableReason() {
            return unavailableReason == null ? "" : unavailableReason;
        }

        public String getDisplayTitle() {
            if (displayTitle != null && !displayTitle.trim().isEmpty()) {
                return displayTitle;
            }

            if ("PERCENT".equalsIgnoreCase(discountType)) {
                return "Giảm " + getDiscountValue() + "%";
            }

            return "Giảm " + formatVnd(getDiscountValue());
        }

        public String getDisplaySubtitle() {
            if (displaySubtitle != null && !displaySubtitle.trim().isEmpty()) {
                return displaySubtitle;
            }

            if (getMinOrderValue() > 0) {
                return "Đơn tối thiểu " + formatVnd(getMinOrderValue());
            }

            return "Không yêu cầu đơn tối thiểu";
        }

        private String formatVnd(Long value) {
            long amount = value == null ? 0L : value;
            return String.format("%,d", amount).replace(",", ".") + "đ";
        }
    }
}