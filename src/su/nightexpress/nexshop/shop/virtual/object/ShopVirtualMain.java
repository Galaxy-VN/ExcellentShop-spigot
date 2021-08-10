package su.nightexpress.nexshop.shop.virtual.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class ShopVirtualMain extends NGUI<ExcellentShop> {

    public ShopVirtualMain(@NotNull VirtualShop guiShop, @NotNull JYML cfg, @NotNull String path) {
        super(guiShop.plugin, cfg, path);

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType type2) {

                if (type2 == ContentType.EXIT) {
                    p.closeInventory();
                }
            }
        };

        for (String id : cfg.getSection(path + "custom-items")) {
            GuiItem guiItem = cfg.getGuiItem(path + "custom-items." + id, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }

            this.addButton(guiItem);
        }

        for (String id : cfg.getSection(path + "shop-items")) {
            GuiItem guiItem = cfg.getGuiItem(path + "shop-items." + id);
            if (guiItem == null) continue;

            IShopVirtual shop = guiShop.getShopById(id);
            if (shop == null) {
                plugin.error("Invalid shop item in the main menu: '" + id + "' !");
                continue;
            }

            guiItem.setClick((p, type, e) -> {
                shop.open(p, 1);
            });

            this.addButton(guiItem);
        }
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int slot) {

    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean ignoreNullClick() {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return true;
    }

    @Override
    protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
        super.replaceMeta(player, item, guiItem);

        VirtualShop guiShop = plugin.getVirtualShop();
        if (guiShop == null) return;

        IShopVirtual shop = guiShop.getShopById(guiItem.getId());
        if (shop == null) return;

        ItemUT.replace(item, shop.replacePlaceholders());
    }
}
