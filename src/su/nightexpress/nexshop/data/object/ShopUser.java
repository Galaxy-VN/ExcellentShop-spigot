package su.nightexpress.nexshop.data.object;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.data.users.IAbstractUser;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IProductPrepared;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopUser extends IAbstractUser<ExcellentShop> {

    private final Map<String, UserProductLimit> virtualLimits;
    private final UserSettings                  settings;

    public ShopUser(@NotNull ExcellentShop plugin, @NotNull Player player) {
        this(
                plugin, player.getUniqueId(), player.getName(), System.currentTimeMillis(),

                new UserSettings(true, true),
                new HashMap<>()
        );
    }

    public ShopUser(
            @NotNull ExcellentShop plugin, @NotNull UUID uuid, @NotNull String name, long lastOnline,
            @NotNull UserSettings settings,
            @NotNull Map<String, UserProductLimit> virtualLimits
    ) {
        super(plugin, uuid, name, lastOnline);

        this.settings = settings;
        this.virtualLimits = virtualLimits;
    }

    @NotNull
    public UserSettings getSettings() {
        return this.settings;
    }

    @NotNull
    public Map<String, UserProductLimit> getVirtualProductLimits() {
        this.virtualLimits.values().removeIf(l -> l.isExpired(TradeType.BUY) && l.isExpired(TradeType.SELL));
        return this.virtualLimits;
    }

    public void addVirtualProductLimit(@NotNull IProductPrepared product, @NotNull TradeType tradeType) {
        IShopProduct shopProduct = product.getShopProduct();
        if (!shopProduct.isBuyLimited(tradeType) || shopProduct.getBuyLimitCooldown(tradeType) == 0) return;

        UserProductLimit productLimit = this.getVirtualProductLimit(tradeType, shopProduct.getId());
        if (productLimit == null) {
            productLimit = new UserProductLimit(product);
            productLimit.updateLastOperationTime(product, tradeType);
        }
        productLimit.addCount(tradeType, product.getAmount());

        this.getVirtualProductLimits().put(shopProduct.getId(), productLimit);
    }

    @Nullable
    public UserProductLimit getVirtualProductLimit(@NotNull TradeType tradeType, @NotNull String id) {
        return this.getVirtualProductLimits().get(id);
    }
}
