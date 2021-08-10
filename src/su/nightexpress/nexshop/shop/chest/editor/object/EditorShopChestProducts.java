package su.nightexpress.nexshop.shop.chest.editor.object;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.*;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorHandler;

import java.util.List;

public class EditorShopChestProducts extends NGUI<ExcellentShop> {

    private static ItemStack    productSlot;
    private static String       productName;
    private static List<String> productLore;

    private IShopChest shop;

    public EditorShopChestProducts(@NotNull ExcellentShop plugin, @NotNull IShopChest shop) {
        super(plugin, ChestEditorHandler.CONFIG_SHOP_PRODUCTS, "");
        this.shop = shop;

        JYML cfg = ChestEditorHandler.CONFIG_SHOP_PRODUCTS;

        productSlot = cfg.getItem("free-slot");
        if (ItemUT.isAir(productSlot)) {
            JIcon icon = new JIcon(Material.LIME_STAINED_GLASS_PANE);
            icon.setName("&a&lFree Slot!");
            productSlot = icon.build();
        }

        productName = StringUT.color(cfg.getString("product.name", "%name%"));
        productLore = StringUT.color(cfg.getStringList("product.lore"));

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType) {
                ContentType type2 = (ContentType) type;
                if (type2 == ContentType.RETURN) {
                    shop.getEditor().open(p, 1);
                }
                return;
            }
        };

        for (String id : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + id, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
        int count = 0;
        int maxProducts = ChestShopConfig.getMaxShopProducts(player);
        if (maxProducts < 0) maxProducts = inv.getSize();

        for (IShopProduct shopProduct : this.shop.getProducts()) {
            if (count >= maxProducts || count >= inv.getSize()) break;

            JIcon item = new JIcon(shopProduct.getPreview());
            item.setName(productName.replace("%name%", ItemUT.getItemName(shopProduct.getPreview())));
            item.clearLore();
            for (String line : productLore) {
                item.addLore(line
                        .replace("%sell%", NumberUT.format(shopProduct.getPricer().getPriceSell()))
                        .replace("%buy%", NumberUT.format(shopProduct.getPricer().getPriceBuy(false))));
            }

            item.setClick((p, type, e) -> {
                if (e.isLeftClick()) {
                    shopProduct.getEditor().open(p, 1);
                    return;
                }
                if (e.isShiftClick()) {
                    if (e.isRightClick()) {
                        if (this.shop.getProductAmount(shopProduct) > 0) {
                            this.plugin.lang().Chest_Shop_Editor_Error_ProductLeft.send(p);
                            return;
                        }
                        this.shop.deleteProduct(shopProduct);
                        this.shop.save();
                        this.open(p, page);
                    }
                    return;
                }
            });
            this.addButton(player, item, count++);
        }


        JIcon free = new JIcon(productSlot);
        free.setClick((p, type, e) -> {
            ItemStack cursor = e.getCursor();
            if (cursor == null || !this.shop.createProduct(p, cursor)) return;

            e.getView().setCursor(null);
            ItemUT.addItem(p, cursor);
            this.shop.save();
            this.open(p, page);
        });

        for (int slot = count; slot < maxProducts; slot++) {
            this.addButton(player, free, slot);
        }
    }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        plugin.runTask((c) -> {
            InventoryView view = player.getOpenInventory();
            if (view.getTopInventory().getHolder() instanceof NGUI<?>) return;

            shop.getEditor().open(player, 1);
        }, false);

        super.onClose(player, e);
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
}
