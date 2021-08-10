package su.nightexpress.nexshop.data.object;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.IProductPrepared;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

import java.util.HashMap;
import java.util.Map;

public class UserProductLimit {

    private final String                  shopId;
    private final String                  productId;
    private final Map<TradeType, Integer> count;
    private final Map<TradeType, Long>    expireDate;

    public UserProductLimit(@NotNull IProductPrepared product) {
        this.shopId = product.getShop().getId();
        this.productId = product.getShopProduct().getId();
        this.count = new HashMap<>();
        this.expireDate = new HashMap<>();
    }

    public UserProductLimit(
            @NotNull String shopId,
            @NotNull String productId,
            @NotNull Map<TradeType, Integer> count,
            @NotNull Map<TradeType, Long> expireDate
    ) {
        this.shopId = shopId;
        this.productId = productId;
        this.count = count;
        this.expireDate = expireDate;
    }

    @NotNull
    public String getShopId() {
        return this.shopId;
    }

    @NotNull
    public String getProductId() {
        return this.productId;
    }

    public void addCount(@NotNull TradeType tradeType, int amount) {
        int has = this.count.computeIfAbsent(tradeType, t -> 0);
        this.count.put(tradeType, has + amount);
    }

    public int getCount(@NotNull TradeType tradeType) {
        return this.count.getOrDefault(tradeType, 0);
    }

    public long getExpireDate(@NotNull TradeType tradeType) {
        return this.expireDate.getOrDefault(tradeType, 0L);
    }

    public void updateLastOperationTime(@NotNull IProductPrepared product, @NotNull TradeType tradeType) {
        IShopProduct shopProduct = product.getShopProduct();
        if (!shopProduct.isBuyLimitExpirable(tradeType)) {
            this.expireDate.put(tradeType, -1L);
            return;
        }
        this.expireDate.put(tradeType, System.currentTimeMillis() + shopProduct.getBuyLimitCooldown(tradeType) * 1000L);
    }

    public boolean isExpired(@NotNull TradeType tradeType) {
        VirtualShop virtualShop = ShopAPI.getVirtualShop();
        if (virtualShop == null) return false;

        IShopVirtual shop = virtualShop.getShopById(this.getShopId());
        if (shop == null) return false;

        IShopProduct product = shop.getProductById(this.getProductId());
        if (product == null || !product.isBuyLimitExpirable(tradeType)) return false;

        if (this.getExpireDate(tradeType) < 0 || System.currentTimeMillis() <= this.getExpireDate(tradeType)) {
            return false;
        }

        return true;
    }

    public int getItemsLeft(@NotNull TradeType tradeType) {
        VirtualShop virtualShop = ShopAPI.getVirtualShop();
        if (virtualShop == null) return -1;

        IShopVirtual shop = virtualShop.getShopById(this.getShopId());
        if (shop == null) return -1;

        IShopProduct product = shop.getProductById(this.getProductId());
        if (product == null) return -1;

        if (!product.isBuyLimited(tradeType) || (this.getExpireDate(tradeType) > 0 && System.currentTimeMillis() > this.getExpireDate(tradeType))) {
            return -1;
        }

        return Math.max(0, product.getBuyLimitAmount(tradeType) - this.getCount(tradeType));
    }
}
