package su.nightexpress.nexshop.shop.chest.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;

import java.util.List;

public class ShopListSearchGUI extends AbstractShopListGUI {

    public ShopListSearchGUI(@NotNull ChestShop chestShop) {
        super(chestShop, ChestShopConfig.YML_LIST_SEARCH);
    }

    @Override
    @NotNull
    protected List<IShopChest> getShops(@NotNull Player player) {
        return this.chestShop.getShopsSearched(player);
    }

    @Override
    @NotNull
    protected GuiClick getClick(@NotNull IShopChest shop) {
        return (p, type, e) -> {
            shop.open(p, 1);
        };
    }

}
