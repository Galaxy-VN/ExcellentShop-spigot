package su.nightexpress.nexshop.api.virtual;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.data.object.ShopUser;
import su.nightexpress.nexshop.data.object.UserProductLimit;

import java.util.function.UnaryOperator;

public interface IShopVirtualProduct extends IShopProduct {

    String PLACEHOLDER_LIMIT_BUY_RESET  = "%product_limit_buy_reset%";
    String PLACEHOLDER_LIMIT_SELL_RESET = "%product_limit_sell_reset%";

    @Override
    @NotNull
    IShopVirtual getShop();

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders(@NotNull Player player) {
        long resBuy = this.getStockResetTime(player, TradeType.BUY);
        long resSell = this.getStockResetTime(player, TradeType.SELL);

        String never = this.getShop().plugin().lang().Other_Never.getMsg();
        String restockBuy = resBuy < 0 ? never : TimeUT.formatTime(resBuy);
        String restocSell = resSell < 0 ? never : TimeUT.formatTime(resSell);

        return str -> IShopProduct.super.replacePlaceholders(player).apply(str
                .replace(PLACEHOLDER_LIMIT_BUY_RESET, restockBuy)
                .replace(PLACEHOLDER_LIMIT_SELL_RESET, restocSell)
        );
    }

    int getSlot();

    void setSlot(int slot);

    int getPage();

    void setPage(int page);

    default long getStockResetTime(@NotNull Player player, @NotNull TradeType tradeType) {
        if (!this.isBuyLimitExpirable(tradeType)) return -1;

        ShopUser user = this.getShop().plugin().getUserManager().getOrLoadUser(player);
        UserProductLimit userLimit = user != null ? user.getVirtualProductLimit(tradeType, this.getId()) : null;

        if (userLimit != null) {
            return userLimit.getExpireDate(tradeType) - System.currentTimeMillis();
        }
        return 0;
    }
}
