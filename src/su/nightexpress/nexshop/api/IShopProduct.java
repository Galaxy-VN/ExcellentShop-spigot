package su.nightexpress.nexshop.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.manager.api.Editable;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.ShopClickType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.data.object.ShopUser;
import su.nightexpress.nexshop.data.object.UserProductLimit;

import java.util.List;
import java.util.function.UnaryOperator;

public interface IShopProduct extends Cleanable, Editable {

    String PLACEHOLDER_PRICE_BUY                = "%product_price_buy%";
    String PLACEHOLDER_PRICE_BUY_FORMATTED      = "%product_price_buy_formatted%";
    String PLACEHOLDER_PRICE_SELL               = "%product_price_sell%";
    String PLACEHOLDER_PRICE_SELL_FORMATTED     = "%product_price_sell_formatted%";
    String PLACEHOLDER_PRICE_SELL_ALL           = "%product_price_sell_all%";
    String PLACEHOLDER_PRICE_SELL_ALL_FORMATTED = "%product_price_sell_all_formatted%";
    String PLACEHOLDER_LIMIT_BUY_AVAILABLE      = "%product_limit_buy_available%";
    String PLACEHOLDER_LIMIT_SELL_AVAILABLE     = "%product_limit_sell_available%";

    @NotNull
    IShop getShop();

    @NotNull
    String getId();

    @NotNull
    default UnaryOperator<String> replacePlaceholders(@NotNull Player player) {
        double priceBuy = this.getPricer().getPriceBuy(true);
        double priceSell = this.getPricer().getPriceSell();
        double priceSellAll = this.getPricer().getPriceSellAll(player);

        IShopCurrency currency = this.getCurrency();
        // TODO
        int limBuy = this.getStockAmountLeft(player, TradeType.BUY);
        String limitBuy = limBuy >= 0 ? String.valueOf(limBuy) : getShop().plugin().lang().Other_Infinity.getMsg();

        int limSell = this.getStockAmountLeft(player, TradeType.SELL);
        String limitSell = limSell >= 0 ? String.valueOf(limSell) : getShop().plugin().lang().Other_Infinity.getMsg();


        return str -> str
                .replace(PLACEHOLDER_PRICE_BUY, NumberUT.format(priceBuy))
                .replace(PLACEHOLDER_PRICE_BUY_FORMATTED, priceBuy >= 0 ? currency.format(priceBuy) : "-")
                .replace(PLACEHOLDER_PRICE_SELL, NumberUT.format(priceSell))
                .replace(PLACEHOLDER_PRICE_SELL_FORMATTED, priceSell >= 0 ? currency.format(priceSell) : "-")
                .replace(PLACEHOLDER_PRICE_SELL_ALL, NumberUT.format(priceSellAll))
                .replace(PLACEHOLDER_PRICE_SELL_ALL_FORMATTED, priceSell >= 0 ? currency.format(priceSellAll) : "-")
                .replace(PLACEHOLDER_LIMIT_BUY_AVAILABLE, limitBuy)
                .replace(PLACEHOLDER_LIMIT_SELL_AVAILABLE, limitSell)
                ;
    }

    @Override
    @NotNull
    NGUI<?> getEditor();

    @NotNull
    IProductPricer getPricer();

    @NotNull
    IShopCurrency getCurrency();

    void setCurrency(@NotNull IShopCurrency currency);

    @NotNull
    IProductPrepared getPrepared(@NotNull TradeType tradeType);

    default int getStockAmountLeft(@NotNull Player player, @NotNull TradeType tradeType) {
        ShopUser user = this.getShop().plugin().getUserManager().getOrLoadUser(player);
        UserProductLimit userLimit = user != null ? user.getVirtualProductLimit(tradeType, this.getId()) : null;
        return userLimit != null ? userLimit.getItemsLeft(tradeType) : this.getBuyLimitAmount(tradeType);
    }

    boolean isDiscountAllowed();

    void setDiscountAllowed(boolean isAllowed);

    default void prepareTrade(@NotNull Player player, @NotNull ShopClickType click) {
        if (!this.hasItem() && !this.hasCommands()) {
            return;
        }

        IShop shop = this.getShop();
        TradeType tradeType = click.getBuyType();
        if (!shop.isPurchaseAllowed(tradeType)) {
            return;
        }

        if (tradeType == TradeType.BUY) {
            if (!this.isBuyable()) {
                shop.plugin().lang().Shop_Product_Error_Unbuyable.send(player);
                return;
            }
        }
        else if (tradeType == TradeType.SELL) {
            if (!this.isSellable()) {
                shop.plugin().lang().Shop_Product_Error_Unsellable.send(player);
                return;
            }
        }

        int left = this.getStockAmountLeft(player, tradeType);
        if (left == 0) {
            shop.plugin().lang().Shop_Product_Error_OutOfStock.send(player);
            return;
        }
        // TODO PrepareTradeEvent

        boolean isSellAll = (click == ShopClickType.SELL_ALL);
        IProductPrepared prepared = this.getPrepared(tradeType);
        if (click == ShopClickType.BUY_SINGLE || click == ShopClickType.SELL_SINGLE || isSellAll) {
            prepared.trade(player, isSellAll);
            shop.open(player, shop.getView().getUserPage(player, 0)); // Update current shop page
            return;
        }

        if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY && player.getInventory().firstEmpty() < 0) {
            this.getShop().plugin().lang().Shop_Product_Error_FullInventory.send(player);
            return;
        }
        this.openTrade(player, prepared);
    }

    default void openTrade(@NotNull Player player, @NotNull IProductPrepared prepared) {
        Config.getCartGUI(prepared.getTradeType()).open(player, prepared);
    }

    boolean isItemMetaEnabled();

    void setItemMetaEnabled(boolean isEnabled);

    @NotNull
    ItemStack getItem();

    void setItem(@NotNull ItemStack item);

    default boolean hasItem() {
        return !ItemUT.isAir(this.getItem());
    }

    default int getItemAmount(@NotNull Player player) {
        ItemStack item = this.getItem();
        if (ItemUT.isAir(item)) return 0;

        return this.isItemMetaEnabled() ? PlayerUT.countItem(player, item) : PlayerUT.countItem(player, item.getType());
    }

    default void takeItemAmount(@NotNull Player player, int amount) {
        ItemStack item = this.getItem();
        if (ItemUT.isAir(item)) return;

        if (this.isItemMetaEnabled()) {
            PlayerUT.takeItem(player, item, amount);
        }
        else PlayerUT.takeItem(player, item.getType(), amount);
    }

    @NotNull ItemStack getPreview();

    void setPreview(@NotNull ItemStack preview);

    @NotNull List<String> getCommands();

    void setCommands(@NotNull List<String> commands);

    default boolean hasCommands() {
        return !this.getCommands().isEmpty();
    }

    default boolean isBuyable() {
        if (this.getBuyLimitAmount(TradeType.BUY) == 0) {
            return false;
        }

        IProductPricer pricer = this.getPricer();
        if (pricer.getPriceBuy(false) < 0D || pricer.getPriceBuy(true) < 0D) {
            return false;
        }

        return true;
    }

    default boolean isSellable() {
        if (!this.hasItem() || this.getBuyLimitAmount(TradeType.SELL) == 0) {
            return false;
        }

        IProductPricer pricer = this.getPricer();
        double price = pricer.getPriceSell();
        if (price < 0D) {
            return false;
        }

        // Check if this product is buyable, so we can check if sell price is over the buy price
        // to prevent money duplication.
        // If this product can not be purchased, these checks are useless.
        if (this.getShop().isPurchaseAllowed(TradeType.BUY) && this.isBuyable()) {
            if (price > pricer.getPriceBuy(false) || price > pricer.getPriceBuy(true)) {
                return false;
            }
        }

        return true;
    }

    default boolean isBuyLimited(@NotNull TradeType tradeType) {
        return this.getBuyLimitAmount(tradeType) >= 0;
    }

    default boolean isBuyLimitExpirable(@NotNull TradeType tradeType) {
        return this.getBuyLimitCooldown(tradeType) >= 0;
    }

    int getBuyLimitAmount(@NotNull TradeType tradeType);

    void setBuyLimitAmount(@NotNull TradeType tradeType, int amount);

    long getBuyLimitCooldown(@NotNull TradeType tradeType);

    void setBuyLimitCooldown(@NotNull TradeType tradeType, long cooldown);
}
