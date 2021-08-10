package su.nightexpress.nexshop.shop.virtual;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.manager.IListener;
import su.nexmedia.engine.utils.MsgUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IProductPrepared;
import su.nightexpress.nexshop.api.event.AbstractShopPurchaseEvent.Result;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.event.VirtualShopPurchaseEvent;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.data.object.ShopUser;

public class VirtualShopListener extends IListener<ExcellentShop> {

    private final VirtualShop virtualShop;

    public VirtualShopListener(@NotNull VirtualShop virtualShop) {
        super(virtualShop.plugin);
        this.virtualShop = virtualShop;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShopPurchase(VirtualShopPurchaseEvent e) {
        Player player = e.getPlayer();
        IProductPrepared prepared = e.getPrepared();

        if (e.isCancelled()) {
            if (e.getResult() == Result.TOO_EXPENSIVE) {
                plugin.lang().Shop_Product_Error_TooExpensive
                        .replace(prepared.replacePlaceholders())
                        .send(player);
            }
            if (e.getResult() == Result.NOT_ENOUGH_ITEMS) {
                plugin.lang().Shop_Product_Error_NotEnoughItems
                        .replace(prepared.replacePlaceholders())
                        .send(player);
            }
            MsgUT.sound(player, Config.SOUND_PURCHASE_FAILURE);
            return;
        }

        this.virtualShop.getLogger().logTransaction(e);

        ShopUser user = plugin.getUserManager().getOrLoadUser(player);
        if (user != null) user.addVirtualProductLimit(prepared, e.getTradeType());

        MsgUT.sound(player, Config.SOUND_PURCHASE_SUCCESS);
        (e.getTradeType() == TradeType.BUY ? plugin.lang().Virtual_Shop_Product_Purchase_Buy : plugin.lang().Virtual_Shop_Product_Purchase_Sell)
                .replace(prepared.replacePlaceholders())
                .send(player);
    }
}
