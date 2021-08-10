package su.nightexpress.nexshop.shop.chest.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.*;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestShop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractShopListGUI extends NGUI<ExcellentShop> {

    protected ChestShop chestShop;

    protected int[]        shopSlots;
    protected String       shopName;
    protected List<String> shopLore;

    public AbstractShopListGUI(@NotNull ChestShop chestShop, @NotNull JYML cfg) {
        super(chestShop.plugin, cfg, "");
        this.chestShop = chestShop;

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType type2) {
                switch (type2) {
                    case NEXT -> this.open(p, getUserPage(p, 0) + 1);
                    case BACK -> this.open(p, getUserPage(p, 0) - 1);
                    case EXIT -> p.closeInventory();
                    default -> {}
                }
                return;
            }

            if (type instanceof ItemType type2) {
                if (type2 == ItemType.LIST_GLOBAL_TOGGLE) {
                    boolean isGlobal = false;
                    if (this instanceof ShopListOwnGUI) {
                        this.chestShop.getListGlobalGUI().open(p, 1);
                        isGlobal = true;
                    }
                    else if (this instanceof ShopListGlobalGUI) {
                        this.chestShop.getListOwnGUI().open(p, 1);
                    }
                    else return;

                    plugin.lang().Chest_Shop_ShopList_Info_Switch
                            .replace("%state%", plugin.lang().getBool(isGlobal))
                            .send(p);
                }
                return;
            }
        };

        for (String id : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + id, ItemType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }

            this.addButton(guiItem);
        }

        this.shopSlots = cfg.getIntArray("shop-icon.slots");
        this.shopName = StringUT.color(cfg.getString("shop-icon.name", ""));
        this.shopLore = StringUT.color(cfg.getStringList("shop-icon.lore"));
    }

    private enum ItemType {
        LIST_GLOBAL_TOGGLE,
    }

    @NotNull
    protected abstract List<IShopChest> getShops(@NotNull Player player);

    @NotNull
    protected abstract GuiClick getClick(@NotNull IShopChest shop);

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
        int len = this.shopSlots.length;

        // If player used the search function, then display only that shops.
        List<IShopChest> list = this.getShops(player);
        List<List<IShopChest>> split = CollectionsUT.split(list, len);

        int pages = split.size();
        if (pages < 1) list = Collections.emptyList();
        else list = split.get(page - 1);

        int count = 0;
        for (IShopChest shop : list) {
            ItemStack icon = new ItemStack(shop.getLocation().getBlock().getType());
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                String name = shop.replacePlaceholders().apply(this.shopName);
                meta.setDisplayName(name);

                List<String> lore = new ArrayList<>(this.shopLore);
                lore.replaceAll(shop.replacePlaceholders());
                meta.setLore(lore);

                meta.addItemFlags(ItemFlag.values());
                icon.setItemMeta(meta);
            }

            JIcon icon2 = new JIcon(icon);
            icon2.setClick(this.getClick(shop));

            this.addButton(player, icon2, this.shopSlots[count++]);
        }

        this.setUserPage(player, page, pages);
    }

    @Override
    protected boolean ignoreNullClick() {
        return true;
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return true;
    }
}
