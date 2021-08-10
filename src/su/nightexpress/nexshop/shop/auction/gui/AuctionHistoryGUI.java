package su.nightexpress.nexshop.shop.auction.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AuctionHistoryItem;

import java.util.List;
import java.util.UUID;

public class AuctionHistoryGUI extends IAuctionGUI<AuctionHistoryItem> {

    public AuctionHistoryGUI(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
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

    @Override
    @NotNull
    protected List<AuctionHistoryItem> getItems(@NotNull Player player) {
        UUID id = this.seeOthers.getOrDefault(player, player.getUniqueId());
        return this.auctionManager.getHistory(id);
    }

    @Override
    @NotNull
    protected GuiClick getItemClick(@NotNull AuctionHistoryItem item, int page) {
        return (p, type, e) -> {

        };
    }
}
