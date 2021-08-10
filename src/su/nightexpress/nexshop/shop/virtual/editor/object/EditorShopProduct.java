package su.nightexpress.nexshop.shop.virtual.editor.object;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IProductPricer;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class EditorShopProduct extends NGUI<ExcellentShop> {

    private IShopVirtualProduct product;

    public EditorShopProduct(@NotNull ExcellentShop plugin, @NotNull IShopVirtualProduct product) {
        super(plugin, VirtualEditorHandler.SHOP_PRODUCT_MAIN_YML, "");
        this.product = product;

        IShopVirtual shop = (IShopVirtual) product.getShop();

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN -> shop.getEditor().getEditorProducts().open(p, this.product.getPage());
                    case EXIT -> p.closeInventory();
                    default -> {}
                }
                return;
            }

            if (type instanceof VirtualEditorType) {
                VirtualEditorType type2 = (VirtualEditorType) type;
                switch (type2) {
                    case PRODUCT_CHANGE_COMMANDS: {
                        if (e.isLeftClick()) {
                            EditorManager.tipCustom(p, plugin.lang().Editor_Enter_Command.getMsg());
                            EditorManager.startEdit(p, product, type2);
                            EditorManager.sendCommandTips(p);
                            p.closeInventory();
                            return;
                        }

                        if (e.isRightClick()) {
                            product.getCommands().clear();
                        }
                        break;
                    }
                    case PRODUCT_CHANGE_ITEM: {
                        if (e.getClick() == ClickType.MIDDLE) {
                            ItemStack buyItem = product.getItem();
                            ItemUT.addItem(p, buyItem);
                            return;
                        }

                        ItemStack cursor = e.getCursor();
                        if (cursor != null && !ItemUT.isAir(cursor)) {
                            product.setItem(cursor);
                            e.getView().setCursor(null);
                        }
                        else if (e.isRightClick()) {
                            product.setItem(new ItemStack(Material.AIR));
                        }
                        break;
                    }
                    case PRODUCT_CHANGE_PREVIEW: {
                        if (e.getClick() == ClickType.MIDDLE) {
                            ItemStack buyItem = product.getPreview();
                            ItemUT.addItem(p, buyItem);
                            return;
                        }

                        ItemStack item = e.getCursor();
                        if (item != null && !ItemUT.isAir(item)) {
                            product.setPreview(item);
                            e.getView().setCursor(null);
                        }
                        break;
                    }
                    case PRODUCT_CHANGE_CURRENCY: {
                        EditorManager.tipCustom(p, plugin.lang().Editor_Enter_Currency.getMsg());
                        EditorManager.startEdit(p, product, type2);
                        EditorManager.sendClickableTips(p, plugin.getCurrencyManager()
                                .getCurrencies().stream().map(IShopCurrency::getId)
                                .collect(Collectors.toList()));
                        p.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_DISCOUNT: {
                        product.setDiscountAllowed(!product.isDiscountAllowed());
                        break;
                    }
                    case PRODUCT_CHANGE_ITEM_META: {
                        product.setItemMetaEnabled(!product.isItemMetaEnabled());
                        break;
                    }
                    case PRODUCT_CHANGE_PRICE_BUY: {
                        if (e.getClick() == ClickType.MIDDLE) {
                            product.getPricer().setPriceMin(TradeType.BUY, -1);
                            product.getPricer().setPriceMax(TradeType.BUY, -1);
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN;
                        }
                        else type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX;

                        EditorManager.tipCustom(p, plugin.lang().Editor_Enter_Price.getMsg());
                        EditorManager.startEdit(p, product, type3);
                        p.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_SELL: {
                        if (e.getClick() == ClickType.MIDDLE) {
                            product.getPricer().setPriceMin(TradeType.SELL, -1);
                            product.getPricer().setPriceMax(TradeType.SELL, -1);
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MIN;
                        }
                        else type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX;

                        EditorManager.tipCustom(p, plugin.lang().Editor_Enter_Price.getMsg());
                        EditorManager.startEdit(p, product, type3);
                        p.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_LIMIT: {
                        if (e.getClick() == ClickType.MIDDLE) {
                            product.setBuyLimitAmount(TradeType.BUY, -1);
                            product.setBuyLimitCooldown(TradeType.BUY, 0);
                            product.setBuyLimitAmount(TradeType.SELL, -1);
                            product.setBuyLimitCooldown(TradeType.SELL, 0);
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_SELL_AMOUNT;
                                EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_Amount.getMsg());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_SELL_RESET;
                                EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_Time_Seconds.getMsg());
                            }
                        }
                        else {
                            if (e.isLeftClick()) {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_BUY_AMOUNT;
                                EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_Amount.getMsg());
                            }
                            else {
                                type3 = VirtualEditorType.PRODUCT_CHANGE_LIMIT_BUY_RESET;
                                EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_Time_Seconds.getMsg());
                            }
                        }

                        EditorManager.startEdit(p, product, type3);
                        p.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_RND: {
                        if (e.getClick() == ClickType.MIDDLE) {
                            product.getPricer().setRandomizerEnabled(!product.getPricer().isRandomizerEnabled());
                            break;
                        }

                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                product.getPricer().getDays().clear();
                            }
                            else product.getPricer().getTimes().clear();
                            break;
                        }

                        VirtualEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_RND_TIME_DAY;
                            EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_Day.getMsg());
                            EditorManager.sendClickableTips(p, CollectionsUT.getEnumsList(DayOfWeek.class));
                        }
                        else {
                            type3 = VirtualEditorType.PRODUCT_CHANGE_PRICE_RND_TIME_TIME;
                            EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_Time_Full.getMsg());
                        }

                        EditorManager.startEdit(p, product, type3);
                        p.closeInventory();
                        return;
                    }
                    default: {
                        return;
                    }
                }

                shop.save();
                this.open(p, 1);
                return;
            }
        };

        JYML cfg = VirtualEditorHandler.SHOP_PRODUCT_MAIN_YML;
        for (String id : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + id, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }

        for (String id : cfg.getSection("editor")) {
            GuiItem guiItem = cfg.getGuiItem("editor." + id, VirtualEditorType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {

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
    protected void replaceFrame(@NotNull Player player, @NotNull GuiItem guiItem) {
        Enum<?> e = guiItem.getType();
        if (e == VirtualEditorType.PRODUCT_CHANGE_ITEM_META) {
            guiItem.setAnimationStartFrame(product.isItemMetaEnabled() ? 1 : 0);
        }

        super.replaceFrame(player, guiItem);
    }

    @Override
    protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
        super.replaceMeta(player, item, guiItem);

        Enum<?> type = guiItem.getType();
        if (type != null) {
            if (type == VirtualEditorType.PRODUCT_CHANGE_PREVIEW) {
                item.setType(this.product.getPreview().getType());
            }
            else if (type == VirtualEditorType.PRODUCT_CHANGE_ITEM) {
                ItemStack buyItem = product.getItem();
                if (!ItemUT.isAir(buyItem)) {
                    item.setType(buyItem.getType());
                }
            }
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return;

        IProductPricer pricer = this.product.getPricer();
        ItemStack buyItem = product.getItem();
        String itemName = !ItemUT.isAir(buyItem) ? ItemUT.getItemName(buyItem) : "null";

        IShopCurrency cur = this.product.getCurrency();

        lore.replaceAll(line -> line
                .replace("%price-buy-min%", String.valueOf(pricer.getPriceMin(TradeType.BUY)))
                .replace("%price-buy-max%", String.valueOf(pricer.getPriceMax(TradeType.BUY)))
                .replace("%price-sell-min%", String.valueOf(pricer.getPriceMin(TradeType.SELL)))
                .replace("%price-sell-max%", String.valueOf(pricer.getPriceMax(TradeType.SELL)))
                .replace("%currency-id%", cur.getId())
                .replace("%limit-buy-amount%", String.valueOf(product.getBuyLimitAmount(TradeType.BUY)))
                .replace("%limit-buy-cooldown%", TimeUT.formatTime(product.getBuyLimitCooldown(TradeType.BUY) * 1000L))
                .replace("%limit-sell-amount%", String.valueOf(product.getBuyLimitAmount(TradeType.SELL)))
                .replace("%limit-sell-cooldown%", TimeUT.formatTime(product.getBuyLimitCooldown(TradeType.SELL) * 1000L))
                .replace("%discount-allowed%", plugin.lang().getBool(product.isDiscountAllowed()))
                .replace("%price-rnd-enabled%", plugin.lang().getBool(pricer.isRandomizerEnabled()))
                .replace("%item-meta-enabled%", plugin.lang().getBool(product.isItemMetaEnabled()))
                .replace("%item%", itemName)
                .replace("%preview%", ItemUT.getItemName(product.getPreview()))
        );

        List<String> lore2 = new ArrayList<>();
        for (String line : new ArrayList<>(lore)) {
            if (line.contains("%commands%")) {
                for (String cmd : product.getCommands()) {
                    lore2.add(line.replace("%commands%", cmd));
                }
            }
            else if (line.contains("%price-rnd-days%")) {
                for (DayOfWeek day : pricer.getDays()) {
                    lore2.add(line.replace("%price-rnd-days%", day.getDisplayName(TextStyle.FULL, Locale.ENGLISH)));
                }
            }
            else if (line.contains("%price-rnd-times%")) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
                for (LocalTime[] times : pricer.getTimes()) {
                    lore2.add(line.replace("%price-rnd-times%", formatter.format(times[0]) + "-" + formatter.format(times[1])));
                }
            }
            else {
                lore2.add(line);
            }
        }

        meta.setLore(lore2);
        item.setItemMeta(meta);
    }
}
