package su.nightexpress.nexshop.shop.virtual.editor.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

public class EditorShopList extends NGUI<ExcellentShop> {

    private VirtualShop virtualShop;

    private String       objName;
    private List<String> objLore;
    private int[]        objSlots;

    public EditorShopList(@NotNull VirtualShop virtualShop) {
        super(virtualShop.plugin, VirtualEditorHandler.SHOP_LIST_YML, "");
        this.virtualShop = virtualShop;

        JYML cfg = VirtualEditorHandler.SHOP_LIST_YML;
        this.objName = StringUT.color(cfg.getString("objects.name", "%title%"));
        this.objLore = StringUT.color(cfg.getStringList("objects.lore"));
        this.objSlots = cfg.getIntArray("objects.slots");

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case EXIT -> p.closeInventory();
                    case NEXT -> this.open(p, this.getUserPage(p, 0) + 1);
                    case BACK -> this.open(p, this.getUserPage(p, 0) - 1);
                    default -> {}
                }
                return;
            }

            if (type instanceof VirtualEditorType) {
                VirtualEditorType type2 = (VirtualEditorType) type;
                switch (type2) {
                    case CREATE_SHOP: {
                        p.closeInventory();
                        EditorManager.startEdit(p, null, type2);
                        EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_Id.getMsg());
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
        List<IShopVirtual> list = new ArrayList<>(this.virtualShop.getShops());
        List<List<IShopVirtual>> split = CollectionsUT.split(list, len);

        int pages = split.size();
        if (pages < 1) list = Collections.emptyList();
        else list = split.get(page - 1);

        int count = 0;
        for (IShopVirtual shop : list) {
            ItemStack item = new ItemStack(shop.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(this.objName.replace("%title%", shop.getView().getTitle()));
                List<String> lore = new ArrayList<>(this.objLore);
                lore.replaceAll(line -> line.replace("%file%", shop.getId() + ".yml"));
                meta.addItemFlags(ItemFlag.values());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            JIcon icon = new JIcon(item);
            icon.setClick((p2, type, e) -> {
                if (e.isShiftClick()) {
                    if (e.isRightClick()) {
                        this.virtualShop.delete(shop);
                        this.open(p2, 1);
                        return;
                    }
                }

                shop.getEditor().open(p2, 1);
            });

            this.addButton(player, icon, this.objSlots[count++]);
        }

        this.setUserPage(player, page, pages);
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
        return true;
    }
}
