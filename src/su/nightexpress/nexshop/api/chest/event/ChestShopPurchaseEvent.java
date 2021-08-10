package su.nightexpress.nexshop.api.chest.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.chest.IShopChestProductPrepared;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent;

public class ChestShopPurchaseEvent extends AbstractShopPurchaseEvent {

    public ChestShopPurchaseEvent(@NotNull Player player, @NotNull IShopChestProductPrepared prepared) {
        super(player, prepared);
    }

    @Override
    @NotNull
    public IShopChestProductPrepared getPrepared() {
        return (IShopChestProductPrepared) this.prepared;
    }
}
