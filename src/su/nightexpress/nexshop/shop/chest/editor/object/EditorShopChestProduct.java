package su.nightexpress.nexshop.shop.chest.editor.object;

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
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.IProductPricer;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorHandler;
import su.nightexpress.nexshop.shop.chest.editor.ChestEditorType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditorShopChestProduct extends NGUI<ExcellentShop> {

    private IShopChestProduct product;

    public EditorShopChestProduct(@NotNull ExcellentShop plugin, @NotNull IShopChestProduct product) {
        super(plugin, ChestEditorHandler.CONFIG_SHOP_PRODUCT, "");
        this.product = product;

        IShopChest shop = product.getShop();

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case RETURN -> shop.getEditor().getEditorProducts().open(p, 1);
                    case EXIT -> p.closeInventory();
                    default -> {}
                }
                return;
            }

            if (type instanceof ChestEditorType) {
                ChestEditorType type2 = (ChestEditorType) type;
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
                    case PRODUCT_CHANGE_CURRENCY: {
                        EditorManager.tipCustom(p, plugin.lang().Editor_Enter_Currency.getMsg());
                        EditorManager.startEdit(p, product, type2);
                        EditorManager.sendClickableTips(p, ChestShopConfig.ALLOWED_CURRENCIES);
                        p.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_BUY: {
                        if (e.getClick() == ClickType.MIDDLE) {
                            if (!p.hasPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_NEGATIVE)) {
                                EditorManager.errorCustom(p, plugin.lang().Chest_Shop_Editor_Error_Negative.getMsg());
                                return;
                            }
                            product.getPricer().setPriceMin(TradeType.BUY, -1);
                            product.getPricer().setPriceMax(TradeType.BUY, -1);
                            break;
                        }

                        ChestEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN;
                        }
                        else type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX;

                        EditorManager.tipCustom(p, plugin.lang().Editor_Enter_Price.getMsg());
                        EditorManager.startEdit(p, product, type3);
                        p.closeInventory();
                        return;
                    }
                    case PRODUCT_CHANGE_PRICE_SELL: {
                        if (e.getClick() == ClickType.MIDDLE) {
                            if (!p.hasPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_NEGATIVE)) {
                                EditorManager.errorCustom(p, plugin.lang().Chest_Shop_Editor_Error_Negative.getMsg());
                                return;
                            }
                            product.getPricer().setPriceMin(TradeType.SELL, -1);
                            product.getPricer().setPriceMax(TradeType.SELL, -1);
                            break;
                        }

                        ChestEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_SELL_MIN;
                        }
                        else type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX;

                        EditorManager.tipCustom(p, plugin.lang().Editor_Enter_Price.getMsg());
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

                        ChestEditorType type3;
                        if (e.isLeftClick()) {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_RND_TIME_DAY;
                            EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_Day.getMsg());
                            EditorManager.sendClickableTips(p, CollectionsUT.getEnumsList(DayOfWeek.class));
                        }
                        else {
                            type3 = ChestEditorType.PRODUCT_CHANGE_PRICE_RND_TIME_TIME;
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

        JYML cfg = ChestEditorHandler.CONFIG_SHOP_PRODUCT;
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

            Enum<?> type = guiItem.getType();
            if (type != null) {
                guiItem.setClick(click);

                if (type == ChestEditorType.PRODUCT_CHANGE_PRICE_RND) {
                    guiItem.setPermission(Perms.CHEST_EDITOR_PRODUCT_PRICE_RND);
                }
                else if (type == ChestEditorType.PRODUCT_CHANGE_COMMANDS) {
                    guiItem.setPermission(Perms.CHEST_EDITOR_PRODUCT_COMMANDS);
                }
                else if (type == ChestEditorType.PRODUCT_CHANGE_CURRENCY) {
                    guiItem.setPermission(Perms.CHEST_EDITOR_PRODUCT_CURRENCY);
                }
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
    protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
        super.replaceMeta(player, item, guiItem);

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
                .replace("%currency-name%", cur.getName())
                .replace("%price-rnd-enabled%", plugin.lang().getBool(pricer.isRandomizerEnabled()))
                .replace("%item%", itemName)
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
