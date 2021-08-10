package su.nightexpress.nexshop.api.virtual.event;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent;
import su.nightexpress.nexshop.shop.virtual.object.VirtualProductPrepared;

public class VirtualShopPurchaseEvent extends AbstractShopPurchaseEvent {

    public VirtualShopPurchaseEvent(@NotNull Player player, @NotNull VirtualProductPrepared prepared) {
        super(player, prepared);
    }
}
