package su.nightexpress.nexshop.shop.chest.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;

import java.util.List;

public class ShopListGlobalGUI extends AbstractShopListGUI {

    public ShopListGlobalGUI(@NotNull ChestShop chestShop) {
        super(chestShop, ChestShopConfig.YML_LIST_GLOBAL);
    }

    @Override
    @NotNull
    protected List<IShopChest> getShops(@NotNull Player player) {
        return this.chestShop.getShops().stream().toList();
    }

    @Override
    @NotNull
    protected GuiClick getClick(@NotNull IShopChest shop) {
        return (p, type, e) -> {
            if (e.isRightClick()) {
                if (shop.isOwner(p) || p.hasPermission(Perms.ADMIN)) {
                    shop.getEditor().open(p, 1);
                }
                else plugin.lang().Error_NoPerm.send(p);
                return;
            }

            if ((shop.isOwner(p) && !p.hasPermission(Perms.CHEST_TELEPORT))
                    || (!shop.isOwner(p) && !p.hasPermission(Perms.CHEST_TELEPORT_OTHERS))) {
                plugin.lang().Error_NoPerm.send(p);
                return;
            }

            shop.teleport(p);
        };
    }
}
