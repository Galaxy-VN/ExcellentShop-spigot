package su.nightexpress.nexshop.shop.virtual.editor.object;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.util.ArrayList;
import java.util.List;

public class EditorShop extends NGUI<ExcellentShop> {

    private IShopVirtual          shop;
    private EditorShopDiscounts   editorDiscounts;
    private EditorShopViewDesign  editorViewDesign;
    private EditorShopProductList editorProductList;

    public EditorShop(@NotNull ExcellentShop plugin, @NotNull IShopVirtual shop) {
        super(plugin, VirtualEditorHandler.SHOP_MAIN_YML, "");
        this.shop = shop;

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType) {
                ContentType type2 = (ContentType) type;
                switch (type2) {
                    case EXIT -> p.closeInventory();
                    case RETURN -> {
                        VirtualShop virtualShop = plugin.getVirtualShop();
                        if (virtualShop == null) return;
                        virtualShop.getEditorHandler().getEditorShopList().open(p, 1);
                    }
                    default -> {}
                }
                return;
            }

            if (type instanceof VirtualEditorType) {
                VirtualEditorType type2 = (VirtualEditorType) type;
                switch (type2) {
                    case CHANGE_ICON: {
                        ItemStack cursor = e.getCursor();
                        if (cursor == null || ItemUT.isAir(cursor)) return;

                        shop.setIcon(cursor);
                        e.getView().setCursor(null);
                        break;
                    }
                    case CHANGE_CITIZENS_ID: {
                        if (!Hooks.hasPlugin(Hooks.CITIZENS)) return;

                        if (e.isLeftClick()) {
                            EditorManager.startEdit(p, shop, type2);
                            EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_NpcId.getMsg());
                            p.closeInventory();
                            return;
                        }
                        else if (e.isRightClick()) {
                            shop.setCitizensIds(new int[]{});
                        }
                        break;
                    }
                    case CHANGE_PERMISSION: {
                        shop.setPermissionRequired(!shop.isPermissionRequired());
                        break;
                    }
                    case CHANGE_PRODUCTS: {
                        this.getEditorProducts().open(p, 1);
                        return;
                    }
                    case CHANGE_VIEW_DESIGN: {
                        if (e.isShiftClick()) {
                            if (e.isLeftClick()) {
                                EditorManager.tipCustom(p, plugin.lang().Virtual_Shop_Editor_Enter_Title.getMsg());
                                EditorManager.startEdit(p, shop, VirtualEditorType.CHANGE_TITLE);
                                p.closeInventory();
                                return;
                            }
                            int size = shop.getView().getSize();
                            if (size == 54) size = 0;
                            shop.getView().setSize(size + 9);
                            shop.save();
                            shop.setupView();
                            this.open(p, 1);
                            return;
                        }
                        this.getEditorViewDesign().open(p, 1);
                        return;
                    }
                    case CHANGE_DISCOUNTS: {
                        this.getEditorDiscounts().open(p, 1);
                        return;
                    }
                    case CHANGE_PAGES: {
                        if (e.isLeftClick()) {
                            shop.setPages(shop.getPages() + 1);
                        }
                        else if (e.isRightClick()) {
                            shop.setPages(Math.max(1, shop.getPages() - 1));
                        }
                        shop.save();
                        shop.setupView();
                        this.open(p, 1);
                        return;
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
                    default: {
                        return;
                    }
                }

                this.shop.save();
                this.open(p, 1);
                return;
            }
        };

        JYML cfg = VirtualEditorHandler.SHOP_MAIN_YML;
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
    public void shutdown() {
        if (this.editorDiscounts != null) {
            this.editorDiscounts.shutdown();
            this.editorDiscounts = null;
        }
        if (this.editorViewDesign != null) {
            this.editorViewDesign.shutdown();
            this.editorViewDesign = null;
        }
        if (this.editorProductList != null) {
            this.editorProductList.shutdown();
            this.editorProductList = null;
        }
        super.shutdown();
    }

    public void rebuild() {
        this.editorViewDesign = new EditorShopViewDesign(this.plugin, this.shop);
        this.editorProductList = new EditorShopProductList(this.plugin, this.shop);
    }

    @NotNull
    public EditorShopDiscounts getEditorDiscounts() {
        if (this.editorDiscounts == null) {
            this.editorDiscounts = new EditorShopDiscounts(this.plugin, this.shop);
        }
        return this.editorDiscounts;
    }

    @NotNull
    public EditorShopViewDesign getEditorViewDesign() {
        if (this.editorViewDesign == null) {
            this.rebuild();
        }
        return this.editorViewDesign;
    }

    @NotNull
    public EditorShopProductList getEditorProducts() {
        if (this.editorProductList == null) {
            this.rebuild();
        }
        return this.editorProductList;
    }

    @Override
    protected void replaceFrame(@NotNull Player player, @NotNull GuiItem guiItem) {
        Enum<?> ee = guiItem.getType();
        if (ee instanceof VirtualEditorType) {
            VirtualEditorType type = (VirtualEditorType) ee;
            if (type == VirtualEditorType.CHANGE_PERMISSION) {
                guiItem.setAnimationStartFrame(shop.isPermissionRequired() ? 1 : 0);
            }
        }
        super.replaceFrame(player, guiItem);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int slot) {

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

        if (guiItem.getType() == VirtualEditorType.CHANGE_ICON) {
            item.setType(this.shop.getIcon().getType());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return;

        lore.replaceAll(line -> line
                .replace("%buying%", plugin.lang().getBool(shop.isPurchaseAllowed(TradeType.BUY)))
                .replace("%selling%", plugin.lang().getBool(shop.isPurchaseAllowed(TradeType.SELL)))
                .replace("%icon%", ItemUT.getItemName(shop.getIcon()))

                .replace("%size%", String.valueOf(shop.getView().getSize()))
                .replace("%pages%", String.valueOf(shop.getPages()))
                .replace("%permission-node%", Perms.VIRTUAL_SHOP + shop.getId())
                .replace("%permission-required%", plugin.lang().getBool(shop.isPermissionRequired()))
                .replace("%title%", shop.getView().getTitle())
        );

        List<String> lore2 = new ArrayList<>();
        for (String line : lore) {
            if (line.contains("%npc-id%")) {
                if (!Hooks.hasPlugin(Hooks.CITIZENS)) continue;
                for (int id : shop.getCitizensIds()) {
                    if (id < 0) continue;
                    NPC npc = CitizensAPI.getNPCRegistry().getById(id);
                    if (npc == null) continue;

                    lore2.add(line
                            .replace("%npc-name%", npc.getName())
                            .replace("%npc-id%", String.valueOf(id)));
                }
                continue;
            }
            lore2.add(line);
        }

        meta.setLore(lore2);
        item.setItemMeta(meta);
    }
}
