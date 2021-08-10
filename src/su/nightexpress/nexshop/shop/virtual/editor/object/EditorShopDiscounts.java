package su.nightexpress.nexshop.shop.virtual.editor.object;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.*;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IShopDiscount;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.ShopDiscount;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class EditorShopDiscounts extends NGUI<ExcellentShop> {

    private String       objName;
    private List<String> objLore;
    private int[]        objSlots;

    private IShopVirtual shop;

    public EditorShopDiscounts(@NotNull ExcellentShop plugin, @NotNull IShopVirtual shop) {
        super(plugin, VirtualEditorHandler.SHOP_DISCOUNTS_YML, "");
        this.shop = shop;

        JYML cfg = VirtualEditorHandler.SHOP_DISCOUNTS_YML;
        this.objName = StringUT.color(cfg.getString("objects.name", "&eDiscount #%num%"));
        this.objLore = StringUT.color(cfg.getStringList("objects.lore"));
        this.objSlots = cfg.getIntArray("objects.slots");

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case EXIT -> p.closeInventory();
                    case RETURN -> shop.getEditor().open(p, 1);
                    case NEXT -> this.open(p, this.getUserPage(p, 0) + 1);
                    case BACK -> this.open(p, this.getUserPage(p, 0) - 1);
                    default -> {}
                }
                return;
            }

            if (type instanceof VirtualEditorType) {
                VirtualEditorType type2 = (VirtualEditorType) type;
                switch (type2) {
                    case CREATE_DISCOUNT: {
                        ShopDiscount discount = new ShopDiscount(new HashSet<>(), new HashSet<>(), 0D);
                        this.shop.getDiscounts().add(discount);
                        this.shop.save();
                        this.open(p, this.getUserPage(p, 0));
                        break;
                    }
                    default: {
                        break;
                    }
                }
                return;
            }
        };

        for (String sId : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + sId, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }

        for (String sId : cfg.getSection("editor")) {
            GuiItem guiItem = cfg.getGuiItem("editor." + sId, VirtualEditorType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
        int len = this.objSlots.length;
        List<IShopDiscount> list = new ArrayList<>(this.shop.getDiscounts());
        List<List<IShopDiscount>> split = CollectionsUT.split(list, len);

        int pages = split.size();
        if (pages < 1) list = Collections.emptyList();
        else list = split.get(page - 1);

        DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_TIME;

        int count = 0;
        for (IShopDiscount discount : list) {
            ItemStack item = new ItemStack(Material.GOLD_NUGGET);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(this.objName.replace("%num%", String.valueOf(count)));
                List<String> lore = new ArrayList<>(this.objLore);
                lore.replaceAll(line -> line
                        .replace("%discount%", NumberUT.format(discount.getDiscountRaw()))
                );
                lore = StringUT.replace(lore, "%days%", true, discount.getDays().stream()
                        .map(day -> day.name()).toList());
                lore = StringUT.replace(lore, "%times%", true, discount.getTimes().stream()
                        .map(arr -> f.format(arr[0]) + "-" + f.format(arr[1])).toList());

                meta.addItemFlags(ItemFlag.values());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            JIcon icon = new JIcon(item);
            icon.setClick((p2, type, e) -> {
                if (e.getClick() == ClickType.MIDDLE) {
                    this.shop.getDiscounts().remove(discount);
                    this.shop.save();
                    this.open(p2, this.getUserPage(p2, 0));
                    return;
                }

                if (e.isShiftClick()) {
                    if (e.isLeftClick()) {
                        EditorManager.tipCustom(p2, plugin.lang().Virtual_Shop_Editor_Enter_Day.getMsg());
                        EditorManager.startEdit(p2, discount, VirtualEditorType.DISCOUNT_CHANGE_DAY);
                        EditorManager.sendClickableTips(p2, CollectionsUT.getEnumsList(DayOfWeek.class));
                    }
                    if (e.isRightClick()) {
                        EditorManager.tipCustom(p2, plugin.lang().Virtual_Shop_Editor_Enter_Time_Full.getMsg());
                        EditorManager.startEdit(p2, discount, VirtualEditorType.DISCOUNT_CHANGE_TIME);
                    }
                    p2.closeInventory();
                    return;
                }
                EditorManager.tipCustom(p2, plugin.lang().Virtual_Shop_Editor_Enter_Amount.getMsg());
                EditorManager.startEdit(p2, discount, VirtualEditorType.DISCOUNT_CHANGE_DISCOUNT);
                p2.closeInventory();
            });

            this.addButton(player, icon, this.objSlots[count++]);
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
