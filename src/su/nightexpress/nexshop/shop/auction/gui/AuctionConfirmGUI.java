package su.nightexpress.nexshop.shop.auction.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.*;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.util.Map;
import java.util.WeakHashMap;

public class AuctionConfirmGUI extends NGUI<ExcellentShop> {

    private final AuctionManager auctionManager;
    private final int            itemSlot;

    private final Map<Player, AuctionListing> cache;

    public AuctionConfirmGUI(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager.plugin, cfg, "");
        this.auctionManager = auctionManager;
        this.itemSlot = cfg.getInt("item-slot");
        this.cache = new WeakHashMap<>();

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType type2) {
                if (type2 == ContentType.ACCEPT) {
                    AuctionListing listing = this.cache.get(p);
                    if (listing != null) {
                        this.auctionManager.buy(p, listing);
                    }

                    this.auctionManager.openAuction(p);
                }
                else if (type2 == ContentType.DECLINE) {
                    this.auctionManager.openAuction(p);
                }
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
    }

    public void open(@NotNull Player player, @NotNull AuctionListing listing) {
        ItemStack item = new ItemStack(listing.getItemStack()); // Copy to prevent modifying

        JIcon icon = new JIcon(item);
        this.addButton(player, icon, this.itemSlot);
        this.cache.put(player, listing);
        this.open(player, 1);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {

    }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        this.cache.remove(player);
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
