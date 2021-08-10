package su.nightexpress.nexshop.shop.chest.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.api.AbstractShopView;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.api.type.ShopClickType;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopChestView extends AbstractShopView<IShopChest> {

    private static int[]        PRODUCT_SLOTS;
    private static List<String> PRODUCT_FORMAT_LORE;

    public ShopChestView(@NotNull IShopChest shop) {
        super(shop, ChestShopConfig.YML_SHOP_VIEW);
        JYML cfg = ChestShopConfig.YML_SHOP_VIEW;

        PRODUCT_SLOTS = cfg.getIntArray("Product_Slots");

        String path = "Product_Format.Lore.";
        PRODUCT_FORMAT_LORE = StringUT.color(cfg.getStringList(path + "Text"));

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType type2) {
                switch (type2) {
                    case EXIT -> p.closeInventory();
                    case NEXT -> this.open(p, this.getUserPage(p, 0) + 1);
                    case BACK -> this.open(p, this.getUserPage(p, 0) - 1);
                    default -> {}
                }
                return;
            }
        };

        for (String id : cfg.getSection("custom-items")) {
            GuiItem guiItem = cfg.getGuiItem("custom-items." + id, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }

        this.setTitle(shop.getName());
    }

    @Override
    public void displayProducts(@NotNull Player player, @NotNull Inventory inv, int page) {
        int len = PRODUCT_SLOTS.length;

        List<IShopChestProduct> list = this.getShop().getProducts().stream().toList();
        List<List<IShopChestProduct>> split = CollectionsUT.split(list, len);

        int pages = split.size();
        if (pages < 1) list = Collections.emptyList();
        else list = split.get(page - 1);

        int count = 0;
        for (IShopProduct product : list) {
            ItemStack preview = product.getPreview();
            ItemMeta meta = preview.getItemMeta();

            if (meta != null) {
                List<String> lore = new ArrayList<>();

                for (String lineFormat : PRODUCT_FORMAT_LORE) {
                    if (lineFormat.contains("%lore%")) {
                        List<String> list2 = meta.getLore();
                        if (list2 != null) lore.addAll(list2);
                        continue;
                    }
                    lore.add(lineFormat);
                }

                lore.replaceAll(product.replacePlaceholders(player));
                lore.replaceAll(product.getCurrency().replacePlaceholders());
                meta.setLore(lore);
                preview.setItemMeta(meta);
            }

            JIcon icon = new JIcon(preview);
            icon.setClick((p2, type, e) -> {
                ShopClickType clickType = ShopClickType.getByDefault(e.getClick());
                if (clickType == null) return;

                product.prepareTrade(p2, clickType);
            });
            this.addButton(player, icon, PRODUCT_SLOTS[count++]);
        }
        this.setUserPage(player, page, pages);
    }
}
