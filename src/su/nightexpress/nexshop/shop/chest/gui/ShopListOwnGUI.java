package su.nightexpress.nexshop.shop.chest.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;

import java.util.List;

public class ShopListOwnGUI extends AbstractShopListGUI {

    public ShopListOwnGUI(@NotNull ChestShop chestShop) {
        super(chestShop, ChestShopConfig.YML_LIST_OWN);
    }

    @Override
    @NotNull
    protected List<IShopChest> getShops(@NotNull Player player) {
        return this.chestShop.getShops(player);
    }

    @Override
    @NotNull
    protected GuiClick getClick(@NotNull IShopChest shop) {
        return (p, type, e) -> {
            if (e.isRightClick()) {
                shop.getEditor().open(p, 1);
                return;
            }

            if (!p.hasPermission(Perms.CHEST_TELEPORT)) {
                plugin.lang().Error_NoPerm.send(p);
                return;
            }

            shop.teleport(p);
        };
    }
}
