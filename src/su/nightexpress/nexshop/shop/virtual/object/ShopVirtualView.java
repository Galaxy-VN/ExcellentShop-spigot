package su.nightexpress.nexshop.shop.virtual.object;

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
import su.nightexpress.nexshop.api.AbstractShopView;
import su.nightexpress.nexshop.api.type.ShopClickType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.VirtualConfig;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

import java.util.ArrayList;
import java.util.List;

public class ShopVirtualView extends AbstractShopView<IShopVirtual> {

    public ShopVirtualView(@NotNull IShopVirtual shop, @NotNull JYML cfg) {
        super(shop, cfg);

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType type2) {
                switch (type2) {
                    case EXIT -> p.closeInventory();
                    case NEXT -> this.open(p, this.getUserPage(p, 0) + 1);
                    case BACK -> this.open(p, this.getUserPage(p, 0) - 1);
                    case RETURN -> {
                        VirtualShop virtualShop = plugin.getVirtualShop();
                        if (virtualShop != null && virtualShop.hasMainMenu()) {
                            virtualShop.openMainMenu(p);
                        }
                        else p.closeInventory();

                    }
                    default -> {}
                }
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
    }

    @Override
    public void displayProducts(@NotNull Player player, @NotNull Inventory inv, int page) {
        for (IShopVirtualProduct product : shop.getProducts()) {
            if (product.getPage() != page) continue;

            ItemStack preview = product.getPreview();
            ItemMeta meta = preview.getItemMeta();

            List<String> loreFormat = VirtualConfig.PRODUCT_FORMAT_LORE_PRICE_ALL;
            if (!product.isBuyable() || !shop.isPurchaseAllowed(TradeType.BUY))
                loreFormat = VirtualConfig.PRODUCT_FORMAT_LORE_PRICE_SELL;
            if (!product.isSellable() || !shop.isPurchaseAllowed(TradeType.SELL))
                loreFormat = VirtualConfig.PRODUCT_FORMAT_LORE_PRICE_BUY;

            if (meta != null) {
                List<String> lore = new ArrayList<>();

                for (String lineFormat : loreFormat) {
                    if (lineFormat.contains("product_limit_buy") && !product.isBuyLimited(TradeType.BUY)) {
                        continue;
                    }
                    if (lineFormat.contains("product_limit_sell") && !product.isBuyLimited(TradeType.SELL)) {
                        continue;
                    }
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
            this.addButton(player, icon, product.getSlot());
        }

        this.setUserPage(player, page, shop.getPages());
    }
}
