package su.nightexpress.nexshop.shop.auction.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.JIcon;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AuctionItem;

import java.util.*;

public abstract class IAuctionGUI<A extends AuctionItem> extends NGUI<ExcellentShop> {

    protected AuctionManager auctionManager;

    protected String       itemName;
    protected List<String> itemLore;
    protected int[]        itemSlots;

    protected Map<Player, UUID> seeOthers;

    public IAuctionGUI(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager.plugin, cfg, "");
        this.auctionManager = auctionManager;
        this.seeOthers = new HashMap<>();

        this.itemName = StringUT.color(cfg.getString("items.name", AuctionItem.PLACEHOLDER_NAME));
        this.itemLore = StringUT.color(cfg.getStringList("items.lore"));
        this.itemSlots = cfg.getIntArray("items.slots");
    }

    public void open(@NotNull Player player, int page, @NotNull UUID id) {
        if (!id.equals(player.getUniqueId())) {
            this.seeOthers.put(player, id);
        }
        this.open(player, page);
    }

    @NotNull
    protected abstract List<A> getItems(@NotNull Player player);

    @NotNull
    protected abstract GuiClick getItemClick(@NotNull A item, int page);

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
        int length = this.itemSlots.length;

        List<@NotNull A> list = this.getItems(player);
        List<List<A>> split = CollectionsUT.split(list, length);

        int pages = split.size();
        if (pages < 1) list = Collections.emptyList();
        else list = split.get(page - 1);

        int count = 0;
        for (A aucItem : list) {
            ItemStack item = new ItemStack(aucItem.getItemStack());
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(this.itemName.replace(AuctionItem.PLACEHOLDER_NAME, meta.getDisplayName()));

                List<String> loreMeta = meta.getLore();
                List<String> loreLis = new ArrayList<>(this.itemLore);
                loreLis.replaceAll(aucItem.replacePlaceholders());
                loreLis = StringUT.replace(loreLis, AuctionItem.PLACEHOLDER_LORE, false, loreMeta == null ? Collections.emptyList() : loreMeta);
                meta.setLore(loreLis);
                item.setItemMeta(meta);
            }

            JIcon icon = new JIcon(item);
            icon.setClick(this.getItemClick(aucItem, page));

            this.addButton(player, icon, this.itemSlots[count++]);
        }

        this.setUserPage(player, page, pages);
    }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);

        InventoryView view = player.getOpenInventory();
        if (view.getTopInventory().getHolder() instanceof IAuctionGUI<?>) return;

        this.seeOthers.remove(player);
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
