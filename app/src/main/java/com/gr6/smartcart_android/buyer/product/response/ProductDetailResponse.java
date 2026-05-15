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

    public Long getProductId() {
        if (productId != null) return productId;
        return id;
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

        @SerializedName("id")
        private Long id;

        @SerializedName("rating")
        private Double rating;

        @SerializedName("comment")
        private String comment;

        @SerializedName("userName")
        private String userName;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("imageUrl")
        private String imageUrl;

        @SerializedName("createdAt")
        private String createdAt;

        public Long getReviewId() {
            if (reviewId != null) return reviewId;
            return id;
        }

        public double getRating() {
            if (rating == null) return 0.0;
            return rating;
        }

        public String getComment() {
            if (comment == null || comment.trim().isEmpty()) {
                return "Người dùng chưa để lại bình luận.";
            }
            return comment;
        }

        public String getUserName() {
            if (userName != null && !userName.trim().isEmpty()) return userName;
            if (fullName != null && !fullName.trim().isEmpty()) return fullName;
            return "Người mua SmartCart";
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public static class ShopVoucherDTO {

        @SerializedName("voucherId")
        private Long voucherId;

        @SerializedName("id")
        private Long id;

        @SerializedName("code")
        private String code;

        @SerializedName("voucherCode")
        private String voucherCode;

        @SerializedName("name")
        private String name;

        @SerializedName("voucherName")
        private String voucherName;

        @SerializedName("discountType")
        private String discountType;

        @SerializedName("type")
        private String type;

        @SerializedName("discountValue")
        private BigDecimal discountValue;

        @SerializedName("value")
        private BigDecimal value;

        @SerializedName("minOrderAmount")
        private BigDecimal minOrderAmount;

        @SerializedName("minimumOrderValue")
        private BigDecimal minimumOrderValue;

        @SerializedName("maxDiscountAmount")
        private BigDecimal maxDiscountAmount;

        @SerializedName("status")
        private String status;

        public Long getVoucherId() {
            if (voucherId != null) return voucherId;
            return id;
        }

        public String getCode() {
            if (code != null && !code.trim().isEmpty()) return code;
            if (voucherCode != null && !voucherCode.trim().isEmpty()) return voucherCode;
            return "VOUCHER";
        }

        public String getName() {
            if (name != null && !name.trim().isEmpty()) return name;
            if (voucherName != null && !voucherName.trim().isEmpty()) return voucherName;
            return "Mã giảm giá của shop";
        }

        public String getDiscountType() {
            if (discountType != null && !discountType.trim().isEmpty()) return discountType;
            return type;
        }

        public BigDecimal getDiscountValue() {
            if (discountValue != null) return discountValue;
            if (value != null) return value;
            return BigDecimal.ZERO;
        }

        public BigDecimal getMinOrderAmount() {
            if (minOrderAmount != null) return minOrderAmount;
            if (minimumOrderValue != null) return minimumOrderValue;
            return BigDecimal.ZERO;
        }

        public BigDecimal getMaxDiscountAmount() {
            return maxDiscountAmount;
        }

        public String getStatus() {
            return status;
        }

        public String getDisplayTitle() {
            return getCode();
        }

        public String getDisplaySubtitle() {
            String safeType = getDiscountType() == null ? "" : getDiscountType().trim().toUpperCase();

            if ("PERCENT".equals(safeType) || "PERCENTAGE".equals(safeType)) {
                return "Giảm " + getDiscountValue().stripTrailingZeros().toPlainString() + "%";
            }

            if ("FIXED".equals(safeType) || "AMOUNT".equals(safeType) || "MONEY".equals(safeType)) {
                return "Giảm " + getDiscountValue().stripTrailingZeros().toPlainString() + "đ";
            }

            return getName();
        }
    }
}