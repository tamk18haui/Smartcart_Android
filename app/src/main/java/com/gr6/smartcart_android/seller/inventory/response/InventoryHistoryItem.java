package com.gr6.smartcart_android.seller.inventory.response;

public class InventoryHistoryItem {
    public enum Type {
        IMPORT,
        EXPORT,
        ADJUSTMENT
    }

    private final Type type;
    private final String productName;
    private final String timeText;
    private final String sourceText;
    private final int quantityDelta;

    public InventoryHistoryItem(Type type, String productName, String timeText, String sourceText, int quantityDelta) {
        this.type = type;
        this.productName = productName;
        this.timeText = timeText;
        this.sourceText = sourceText;
        this.quantityDelta = quantityDelta;
    }

    public Type getType() {
        return type;
    }

    public String getProductName() {
        return productName == null ? "" : productName;
    }

    public String getTimeText() {
        return timeText == null ? "" : timeText;
    }

    public String getSourceText() {
        return sourceText == null ? "" : sourceText;
    }

    public int getQuantityDelta() {
        return quantityDelta;
    }
}


