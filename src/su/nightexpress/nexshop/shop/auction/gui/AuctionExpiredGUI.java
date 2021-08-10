package su.nightexpress.nexshop.shop.auction.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.util.List;
import java.util.UUID;

public class AuctionExpiredGUI extends IAuctionGUI<AuctionListing> {

    public AuctionExpiredGUI(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType type2) {
                switch (type2) {
                    case EXIT -> p.closeInventory();
                    case NEXT -> this.open(p, this.getUserPage(p, 0) + 1);
                    case BACK -> this.open(p, this.getUserPage(p, 0) - 1);
                    case RETURN -> this.auctionManager.getAuctionGUI().open(p, 1);
                    default -> {}
                }
                return;
            }

            if (type instanceof ItemType type2) {
                if (type2 == ItemType.AUCTION_EXPIRED_TAKE_ALL) {
                    this.auctionManager.getExpired(p).forEach(listing -> {
                        this.auctionManager.takeExpired(p, listing);
                    });
                    this.open(p, 1);
                }
            }
        };

        for (String sId : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + sId, ItemType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }
    }

    private enum ItemType {
        AUCTION_EXPIRED_TAKE_ALL
    }

    @Override
    @NotNull
    protected List<AuctionListing> getItems(@NotNull Player player) {
        UUID id = this.seeOthers.getOrDefault(player, player.getUniqueId());
        return this.auctionManager.getExpired(id);
    }

    @Override
    @NotNull
    protected GuiClick getItemClick(@NotNull AuctionListing item, int page) {
        return (p2, type, e) -> {
            this.auctionManager.takeExpired(p2, item);
            this.open(p2, page);
        };
    }
}
