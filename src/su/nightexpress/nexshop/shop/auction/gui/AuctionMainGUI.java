package su.nightexpress.nexshop.shop.auction.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.ContentType;
import su.nexmedia.engine.manager.api.gui.GuiClick;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.config.Config;
import su.nightexpress.nexshop.shop.auction.AuctionConfig;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.auction.object.AuctionCategory;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class AuctionMainGUI extends IAuctionGUI<AuctionListing> {

    private final Map<Player, AuctionSortType> sortTypes;
    private final Map<Player, Integer>         categories;


    public AuctionMainGUI(@NotNull AuctionManager auctionManager, @NotNull JYML cfg) {
        super(auctionManager, cfg);
        this.sortTypes = new WeakHashMap<>();
        this.categories = new WeakHashMap<>();

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            if (type instanceof ContentType type2) {
                switch (type2) {
                    case EXIT -> p.closeInventory();
                    case NEXT -> this.open(p, this.getUserPage(p, 0) + 1);
                    case BACK -> this.open(p, this.getUserPage(p, 0) - 1);
                    default -> {}
                }
                return;
            }

            if (type instanceof AuctionItemType type2) {
                switch (type2) {
                    case AUCTION_EXPIRED: {
                        this.auctionManager.getAuctionExpiredGUI().open(p, 1);
                        break;
                    }
                    case AUCTION_HISTORY: {
                        this.auctionManager.getAuctionHistoryGUI().open(p, 1);
                        break;
                    }
                    case AUCTION_SORTING: {
                        this.setSortType(p, CollectionsUT.toggleEnum(this.getSortType(p)));
                        this.open(p, this.getUserPage(p, 0));
                        break;
                    }
                    case AUCTION_CATEGORY: {
                        int index = this.categories.getOrDefault(p, 0);
                        if (index < 0) return;
                        if ((index + 1) >= AuctionConfig.CATEGORIES.size()) index = -1;

                        this.setCategory(p, index + 1);
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
            GuiItem guiItem = cfg.getGuiItem("content." + sId, AuctionItemType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }
    }

    @NotNull
    private AuctionSortType getSortType(@NotNull Player player) {
        return this.sortTypes.computeIfAbsent(player, type -> AuctionSortType.NEWEST);
    }

    private void setSortType(@NotNull Player player, @NotNull AuctionSortType sortType) {
        this.sortTypes.put(player, sortType);
    }

    @Nullable
    private AuctionCategory getCategory(@NotNull Player player) {
        if (AuctionConfig.CATEGORIES.isEmpty()) return null;

        return AuctionConfig.CATEGORIES.get(this.categories.computeIfAbsent(player, cat -> 0));
    }

    private void setCategory(@NotNull Player player, int index) {
        this.categories.put(player, index);
    }

    @Override
    protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
        super.replaceMeta(player, item, guiItem);

        AuctionCategory category = this.getCategory(player);

        ItemUT.replace(item, line -> line
                .replace("%tax%", NumberUT.format(AuctionConfig.LISTINGS_PRICE_TAX))
                .replace("%expire%", TimeUT.formatTime(AuctionConfig.STORAGE_EXPIRE_IN))
                .replace("%sort-type%", plugin.lang().getEnum(this.getSortType(player)))
                .replace("%category-name%", category != null ? category.getName() : "-")
        );
    }

    @Override
    @NotNull
    protected List<AuctionListing> getItems(@NotNull Player player) {
        AuctionCategory category = this.getCategory(player);

        return this.auctionManager.getListings().stream()
                .filter(listing -> category == null || category.isItemOfThis(listing.getItemStack()))
                .sorted(this.getSortType(player).getComparator()).toList();
    }

    @Override
    @NotNull
    protected GuiClick getItemClick(@NotNull AuctionListing item, int page) {
        return (p2, type, e) -> {
            if (e.isShiftClick() && e.isRightClick()) {
                if (item.isOwner(p2) || p2.hasPermission(Perms.ADMIN)) {
                    this.auctionManager.takeListing(p2, item);
                    this.open(p2, page);
                }
                return;
            }
            if (item.isOwner(p2)) return;

            if (!Config.GENERAL_BUY_WITH_FULL_INVENTORY && p2.getInventory().firstEmpty() < 0) {
                plugin.lang().Shop_Product_Error_FullInventory.send(p2);
                return;
            }
            this.auctionManager.openAuctionConfirm(p2, item);
        };
    }

    private enum AuctionItemType {
        AUCTION_EXPIRED,
        AUCTION_HISTORY,
        AUCTION_SORTING,
        AUCTION_CATEGORY,
    }

    public enum AuctionSortType {

        NAME((l1, l2) -> {
            String name1 = ItemUT.getItemName(l1.getItemStack());
            String name2 = ItemUT.getItemName(l2.getItemStack());
            return name1.compareTo(name2);
        }),
        MATERIAL((l1, l2) -> {
            String type1 = l1.getItemStack().getType().name();
            String type2 = l2.getItemStack().getType().name();
            return type1.compareTo(type2);
        }),
        SELLER((l1, l2) -> {
            return l1.getOwnerName().compareTo(l2.getOwnerName());
        }),
        NEWEST((l1, l2) -> {
            return Long.compare(l2.getExpireDate(), l1.getExpireDate());
        }),
        OLDEST((l1, l2) -> {
            return Long.compare(l1.getExpireDate(), l2.getExpireDate());
        }),
        MOST_EXPENSIVE((l1, l2) -> {
            return Double.compare(l2.getPrice(), l1.getPrice());
        }),
        LEAST_EXPENSIVE((l1, l2) -> {
            return Double.compare(l1.getPrice(), l2.getPrice());
        }),
        ;

        private final Comparator<AuctionListing> comparator;

        AuctionSortType(@NotNull Comparator<AuctionListing> comparator) {
            this.comparator = comparator;
        }

        @NotNull
        public Comparator<AuctionListing> getComparator() {
            return this.comparator;
        }
    }
}
