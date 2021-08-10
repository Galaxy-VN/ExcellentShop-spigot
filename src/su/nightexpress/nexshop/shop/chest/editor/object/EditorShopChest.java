package su.nightexpress.nexshop.shop.chest.editor.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorHandler;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;

import java.util.List;

public class EditorShopChest extends NGUI<ExcellentShop> {

    private final IShopChest shop;

    private EditorShopChestProducts products;

    public EditorShopChest(@NotNull ExcellentShop plugin, @NotNull IShopChest shop) {
        super(plugin, ChestEditorHandler.CONFIG_SHOP, "");
        this.shop = shop;

        ChestShop chestShop = plugin.getChestShop();
        if (chestShop == null) return;

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            Class<?> clazz = type.getClass();
            if (clazz.equals(ChestEditorType.class)) {
                ChestEditorType type2 = (ChestEditorType) type;
                switch (type2) {
                    case CHANGE_NAME: {
                        EditorManager.startEdit(p, shop, type2);
                        EditorManager.tipCustom(p, plugin.lang().Chest_Shop_Editor_Tip_Name.getMsg());
                        p.closeInventory();
                        return;
                    }
                    case CHANGE_ADMIN: {
                        if (!p.hasPermission(Perms.CHEST_EDITOR_ADMINSHOP)) {
                            plugin.lang().Error_NoPerm.send(p);
                            return;
                        }
                        shop.setAdminShop(!shop.isAdminShop());
                        break;
                    }
                    case CHANGE_TRANSACTIONS: {
                        if (e.isLeftClick()) {
                            shop.setPurchaseAllowed(TradeType.BUY, !shop.isPurchaseAllowed(TradeType.BUY));
                        }
                        else if (e.isRightClick()) {
                            shop.setPurchaseAllowed(TradeType.SELL, !shop.isPurchaseAllowed(TradeType.SELL));
                        }
                        break;
                    }
                    case CHANGE_PRODUCTS: {
                        this.getEditorProducts().open(p, 1);
                        return;
                    }
                    case DELETE: {
                        if (!p.hasPermission(Perms.CHEST_REMOVE)) {
                            plugin.lang().Error_NoPerm.send(p);
                            return;
                        }
                        chestShop.deleteShop(p, shop.getLocation().getBlock());
                        p.closeInventory();
                        return;
                    }
                    default: {
                        return;
                    }
                }
                this.save(p);
            }
            else if (clazz.equals(ContentType.class)) {
                ContentType type2 = (ContentType) type;
                if (type2 == ContentType.RETURN) {
                    chestShop.getListOwnGUI().open(p, 1);
                }
                else if (type2 == ContentType.EXIT) {
                    p.closeInventory();
                }
            }
        };

        JYML cfg = ChestEditorHandler.CONFIG_SHOP;

        for (String id : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + id, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }

        for (String id : cfg.getSection("editor")) {
            GuiItem guiItem = cfg.getGuiItem("editor." + id, ChestEditorType.class);
            if (guiItem == null) continue;

            Enum<?> gType = guiItem.getType();
            if (gType != null) {
                guiItem.setClick(click);
            }

            this.addButton(guiItem);
        }
    }

    @NotNull
    public EditorShopChestProducts getEditorProducts() {
        if (this.products == null) {
            this.products = new EditorShopChestProducts(this.plugin, this.shop);
        }
        return this.products;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {

    }

    private void save(@NotNull Player player) {
        this.shop.save();
        this.open(player, 1);
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return false;
    }

    @Override
    protected boolean ignoreNullClick() {
        return false;
    }

    @Override
    protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
        super.replaceMeta(player, item, guiItem);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return;

        lore.replaceAll(line -> line
                .replace("%name%", shop.getName())
                .replace("%admin%", plugin.lang().getBool(shop.isAdminShop()))
                .replace("%buying%", plugin.lang().getBool(shop.isPurchaseAllowed(TradeType.BUY)))
                .replace("%selling%", plugin.lang().getBool(shop.isPurchaseAllowed(TradeType.SELL)))
        );

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
