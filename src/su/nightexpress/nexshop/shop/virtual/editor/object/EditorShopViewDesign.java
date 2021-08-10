package su.nightexpress.nexshop.shop.virtual.editor.object;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.object.ShopVirtual;

import java.util.*;

public class EditorShopViewDesign extends NGUI<ExcellentShop> {

    private IShopVirtual shop;

    private final        NamespacedKey keyItemType;
    private static final String        TAG_LORE = "gui_type";

    public EditorShopViewDesign(@NotNull ExcellentShop plugin, @NotNull IShopVirtual shop) {
        super(plugin, shop.getView().getTitle(), shop.getView().getSize());
        this.keyItemType = new NamespacedKey(plugin, "gui_item_type");
        this.shop = shop;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
        this.setUserPage(player, 1, 1000); // Hack for NEXT,BACK items display.

        for (GuiItem item : this.shop.getView().getContent().values()) {
            Enum<?> type = item.getType();
            ItemStack stack = item.getItemRaw();
            DataUT.setData(stack, keyItemType, type != null ? type.name() : "null");
            this.updateItem(stack);
            for (int slot : item.getSlots()) {
                inv.setItem(slot, stack);
            }
        }
    }

    @Override
    protected void click(@NotNull Player player, @Nullable ItemStack item, int slot, @NotNull InventoryClickEvent e) {
        if (e.getClick() == ClickType.MIDDLE && !this.isPlayerInv(slot) && item != null) {
            ContentType type2 = this.getType(item);
            DataUT.setData(item, keyItemType, type2 == null ? ContentType.NONE.name() : CollectionsUT.toggleEnum(type2).name());
            this.updateItem(item);
            e.setCancelled(true);
            return;
        }
        if (item != null) {
            this.updateItem(item);
        }

        super.click(player, item, slot, e);
    }

    @Override
    protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
        this.updateItem(item);
        super.replaceMeta(player, item, guiItem);
    }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        plugin.runTask((c) -> {
            save(e.getInventory());
            shop.getEditor().open(player, 1);
        }, false);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) ItemUT.delLore(item, TAG_LORE);
        }

        super.onClose(player, e);
    }

    @Nullable
    private ContentType getType(@NotNull ItemStack item) {
        String typeRaw = DataUT.getStringData(item, this.keyItemType);
        ContentType type2 = typeRaw == null ? null : CollectionsUT.getEnum(typeRaw, ContentType.class);
        return type2;
    }

    private void updateItem(@NotNull ItemStack item) {
        ItemUT.delLore(item, TAG_LORE);

        ContentType type2 = this.getType(item);
        String str = StringUT.color("&eItem Type (MMB): &6" + (type2 == null ? "null" : type2.name()));
        ItemUT.addLore(item, TAG_LORE, str, -1);
    }

    private void save(@NotNull Inventory inv) {
        Map<ContentType, Map<ItemStack, List<Integer>>> items = new HashMap<>();
        for (int slot2 = 0; slot2 < inv.getSize(); slot2++) {
            ItemStack item2 = inv.getItem(slot2);
            if (item2 == null || ItemUT.isAir(item2)) continue;

            ContentType type = this.getType(item2);
            Map<ItemStack, List<Integer>> map = items.computeIfAbsent(type, k -> new HashMap<>());

            map.computeIfAbsent(item2, k -> new ArrayList<>()).add(slot2);
        }

        ShopVirtual shop = (ShopVirtual) this.shop;
        JYML cfg = shop.getConfigView();
        cfg.set("custom-items", null);

        items.forEach((type, map) -> {
            map.forEach((item2, slots) -> {
                ItemUT.delLore(item2, TAG_LORE);

                String id = UUID.randomUUID().toString();
                String path = "custom-items." + id + ".";

                String typeRaw = DataUT.getStringData(item2, this.keyItemType);
                ContentType type2 = typeRaw == null ? null : CollectionsUT.getEnum(typeRaw, ContentType.class);

                cfg.setItem(path, item2);
                cfg.setIntArray(path + "slots", slots.stream().mapToInt(i -> i).toArray());
                cfg.set(path + "type", type2 == null ? ContentType.NONE.name() : type2.name());
            });
        });

        cfg.saveChanges();
        shop.setupView();
    }

    @Override
    protected boolean ignoreNullClick() {
        return false;
    }

    @Override
    protected boolean cancelClick(int slot) {
        return false;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return false;
    }
}
