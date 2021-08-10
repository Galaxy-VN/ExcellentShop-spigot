package su.nightexpress.nexshop.api.type;

import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ShopClickType {

    BUY_SELECTION(ClickType.LEFT, TradeType.BUY),
    SELL_SELECTION(ClickType.RIGHT, TradeType.SELL),
    SELL_ALL(ClickType.MIDDLE, TradeType.SELL),
    BUY_SINGLE(ClickType.SHIFT_LEFT, TradeType.BUY),
    SELL_SINGLE(ClickType.SHIFT_RIGHT, TradeType.SELL),
    ;

    private       ClickType defType;
    private final TradeType buyType;

    ShopClickType(@NotNull ClickType defType, @NotNull TradeType buyType) {
        this.setClickType(defType);
        this.buyType = buyType;
    }

    public void setClickType(@NotNull ClickType type) {
        this.defType = type;
    }

    @NotNull
    public ClickType getDefaultType() {
        return this.defType;
    }

    @NotNull
    public TradeType getBuyType() {
        return this.buyType;
    }

    @Nullable
    public static ShopClickType getByDefault(@NotNull ClickType defType) {
        for (ShopClickType type : values()) {
            if (type.getDefaultType() == defType) {
                return type;
            }
        }
        return null;
    }
}
