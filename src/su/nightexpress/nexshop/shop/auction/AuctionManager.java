package su.nightexpress.nexshop.shop.auction;

import com.google.common.collect.Lists;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.data.StorageType;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.ClickText;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.hooks.EHook;
import su.nightexpress.nexshop.modules.EModule;
import su.nightexpress.nexshop.modules.ShopModule;
import su.nightexpress.nexshop.shop.auction.command.AuctionCommand;
import su.nightexpress.nexshop.shop.auction.command.ExpiredCommand;
import su.nightexpress.nexshop.shop.auction.command.HistoryCommand;
import su.nightexpress.nexshop.shop.auction.command.SellCommand;
import su.nightexpress.nexshop.shop.auction.compatibility.ImportAuctionHouse;
import su.nightexpress.nexshop.shop.auction.gui.*;
import su.nightexpress.nexshop.shop.auction.object.AuctionHistoryItem;
import su.nightexpress.nexshop.shop.auction.object.AuctionItem;
import su.nightexpress.nexshop.shop.auction.object.AuctionListing;

import java.util.*;

public class AuctionManager extends ShopModule {

    private AuctionConfig auctionConfig;

    private List<AuctionListing>     listings;
    private List<AuctionListing>     expired;
    private List<AuctionHistoryItem> history;

    private AuctionMainGUI    auctionGUI;
    private AuctionConfirmGUI auctionConfirmGUI;
    private AuctionExpiredGUI auctionExpiredGUI;
    private AuctionHistoryGUI auctionHistoryGUI;

    private VaultHK     vaultHook;
    private MenuUpdater menuUpdater;

    public AuctionManager(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    public String getId() {
        return EModule.AUCTION;
    }

    @Override
    @NotNull
    public String version() {
        return "1.50";
    }

    @Override
    public void setup() {
        this.vaultHook = plugin.getVault();
        if (this.vaultHook == null || !this.vaultHook.hasEconomy()) {
            this.interruptLoad();
            this.error("No Vault Economy found! Auction will be disabled!");
            return;
        }

        if (!this.isSynced()) {
            this.listings = new ArrayList<>(this.getListingsData());
            this.expired = new ArrayList<>(this.getExpiredData());
            this.history = new ArrayList<>(this.getHistoryData());
        }

        this.auctionConfig = new AuctionConfig(this);

        JYML cfgGui = JYML.loadOrExtract(plugin, this.getPath() + "auction.gui.yml");
        this.auctionGUI = new AuctionMainGUI(this, cfgGui);

        JYML cfgGuiConfirm = JYML.loadOrExtract(plugin, this.getPath() + "auction.confirm.gui.yml");
        this.auctionConfirmGUI = new AuctionConfirmGUI(this, cfgGuiConfirm);

        JYML cfgGuiExpired = JYML.loadOrExtract(plugin, this.getPath() + "expired.gui.yml");
        this.auctionExpiredGUI = new AuctionExpiredGUI(this, cfgGuiExpired);

        JYML cfgGuiHistory = JYML.loadOrExtract(plugin, this.getPath() + "history.gui.yml");
        this.auctionHistoryGUI = new AuctionHistoryGUI(this, cfgGuiHistory);

        this.moduleCommand.addDefaultCommand(new AuctionCommand(this));
        this.moduleCommand.addSubCommand(new SellCommand(this));
        this.moduleCommand.addSubCommand(new HistoryCommand(this));
        this.moduleCommand.addSubCommand(new ExpiredCommand(this));

        this.menuUpdater = new MenuUpdater();
        this.menuUpdater.start();

        this.plugin.getServer().getScheduler().runTaskLater(plugin, this::importData, 5L);
    }

    @Override
    public void shutdown() {
        if (this.menuUpdater != null) {
            this.menuUpdater.stop();
            this.menuUpdater = null;
        }
        if (this.auctionConfirmGUI != null) {
            this.auctionConfirmGUI.shutdown();
            this.auctionConfirmGUI = null;
        }
        if (this.auctionGUI != null) {
            this.auctionGUI.shutdown();
            this.auctionGUI = null;
        }
        if (this.auctionExpiredGUI != null) {
            this.auctionExpiredGUI.shutdown();
            this.auctionExpiredGUI = null;
        }
        if (this.auctionHistoryGUI != null) {
            this.auctionHistoryGUI.shutdown();
            this.auctionHistoryGUI = null;
        }

        if (this.listings != null) {
            this.listings.clear();
            this.listings = null;
        }
        if (this.expired != null) {
            this.expired.clear();
            this.expired = null;
        }
        if (this.history != null) {
            this.history.clear();
            this.history = null;
        }
    }

    public boolean isSynced() {
        return plugin.cfg().dataStorage == StorageType.MYSQL && plugin.cfg().dataSaveInstant;
    }

    private void importData() {
        if (AuctionConfig.STORAGE_IMPORT_PLUGIN.equalsIgnoreCase(Constants.NONE)) return;

        this.info("Preparing to import auction data...");
        String pluginName = AuctionConfig.STORAGE_IMPORT_PLUGIN;
        if (!Hooks.hasPlugin(pluginName)) {
            this.warn("Could not import data from " + pluginName + ": No such plugin installed.");
        }

        if (pluginName.equalsIgnoreCase(EHook.AUCTION_HOUSE)) {
            ImportAuctionHouse.importData(this);
        }
        else {
            this.warn("Could not import data from " + pluginName + ": Not supported.");
        }

        this.info("Import finished.");
        this.cfg.set("settings.storage.import-from", Constants.NONE);
        this.cfg.saveChanges();
    }

    public boolean isAllowedItem(@NotNull ItemStack item) {
        if (AuctionConfig.LISTINGS_DISABLED_MATERIALS.contains(item.getType().name())) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return true;

        String metaName = meta.getDisplayName();
        if (AuctionConfig.LISTINGS_DISABLED_NAMES.stream().anyMatch(metaName::contains)) {
            return false;
        }

        List<String> metaLore = meta.getLore();
        if (metaLore == null) return true;

        if (metaLore.stream().anyMatch(line -> {
            return AuctionConfig.LISTINGS_DISABLED_LORES.stream().anyMatch(line::contains);
        })) {
            return false;
        }

        return true;
    }

    public boolean add(@NotNull Player player, @NotNull ItemStack item, double price) {
        GameMode gameMode = player.getGameMode();
        if (AuctionConfig.DISABLED_GAMEMODES.contains(gameMode.name())) {
            plugin.lang().Auction_Listing_Add_Error_DisabledGamemode.send(player);
            return false;
        }

        if (!this.isAllowedItem(item)) {
            plugin.lang().Auction_Listing_Add_Error_BadItem
                    .replace("%item%", ItemUT.getItemName(item))
                    .send(player);
            return false;
        }

        price = NumberUT.round(price);
        if (price <= 0) {
            plugin.lang().Auction_Listing_Add_Error_Price_Negative.send(player);
            return false;
        }

        Material material = item.getType();
        double mOne = price / (double) item.getAmount();
        double mMin = AuctionConfig.getMaterialMinPrice(material);
        if (mMin >= 0 && mOne < mMin) {
            plugin.lang().Auction_Listing_Add_Error_Price_Material_Min
                    .replace("%material%", plugin.lang().getEnum(material))
                    .replace("%min%", NumberUT.format(mMin))
                    .send(player);
            return false;
        }

        double mMax = AuctionConfig.getMaterialMaxPrice(material);
        if (mMax >= 0 && mOne > mMax) {
            plugin.lang().Auction_Listing_Add_Error_Price_Material_Max
                    .replace("%max%", NumberUT.format(mMax))
                    .replace("%material%", plugin.lang().getEnum(material))
                    .send(player);
            return false;
        }

        if (!player.hasPermission(Perms.AUCTION_BYPASS_LISTING_PRICE)) {
            if (AuctionConfig.LISTINGS_PRICE_MAX > 0 && price > AuctionConfig.LISTINGS_PRICE_MAX) {
                plugin.lang().Auction_Listing_Add_Error_Price_Max.replace("%max%", AuctionConfig.LISTINGS_PRICE_MAX)
                        .send(player);
                return false;
            }
            if (AuctionConfig.LISTINGS_PRICE_MIN > 0 && price < AuctionConfig.LISTINGS_PRICE_MIN) {
                plugin.lang().Auction_Listing_Add_Error_Price_Min.replace("%min%", AuctionConfig.LISTINGS_PRICE_MIN)
                        .send(player);
                return false;
            }
        }

        double tax = player.hasPermission(Perms.AUCTION_BYPASS_LISTING_TAX) ? 0D : AuctionConfig.LISTINGS_PRICE_TAX;
        double taxPay = price * (tax / 100D);

        if (taxPay > 0) {
            double balance = this.vaultHook.getBalance(player);
            if (balance < taxPay) {
                plugin.lang().Auction_Listing_Add_Error_Price_Tax
                        .replace("%tax-percent%", tax)
                        .replace("%tax-amount%", taxPay)
                        .send(player);
                return false;
            }
            this.vaultHook.take(player, taxPay);
        }

        int bidsHas = this.getListings(player).size();
        int bidsCan = this.getListingsMaximum(player);
        if (bidsCan >= 0 && bidsHas >= bidsCan) {
            plugin.lang().Auction_Listing_Add_Error_Limit.replace("%amount%", bidsCan).send(player);
            return false;
        }

        AuctionListing listing = new AuctionListing(player, item, price);
        if (!this.isSynced()) this.getListings().add(0, listing);
        this.plugin.getData().addAuctionListing(listing, true);

        plugin.lang().Auction_Listing_Add_Success_Info
                .replace("%amount%", item.getAmount())
                .replace("%item%", ItemUT.getItemName(item))
                .replace("%price%", NumberUT.format(price))
                .send(player);

        if (taxPay > 0) {
            plugin.lang().Auction_Listing_Add_Success_Tax
                    .replace("%tax-percent%", tax)
                    .replace("%tax-amount%", taxPay)
                    .send(player);
        }

        if (AuctionConfig.LISTINGS_ANNOUNCE) {
            ClickText clickText = new ClickText(plugin.lang().Auction_Listing_Add_Success_Announce
                    .replace("%player%", player.getDisplayName())
                    .replace("%amount%", item.getAmount())
                    .replace("%price%", NumberUT.format(price))
                    .getMsg());

            clickText.createPlaceholder("%item%", ItemUT.getItemName(item)).showItem(item);
            clickText.send(new HashSet<>(plugin.getServer().getOnlinePlayers()));
        }

        this.getAuctionGUI().refill();
        return true;
    }

    public boolean buy(@NotNull Player buyer, @NotNull AuctionListing listing) {
        if (!this.getListings().contains(listing)) return false;

        double balance = this.vaultHook.getBalance(buyer);
        double price = listing.getPrice();
        if (balance < price) {
            plugin.lang().Auction_Listing_Buy_Error_NoMoney
                    .replace("%balance%", NumberUT.format(balance))
                    .replace("%price%", NumberUT.format(price))
                    .send(buyer);
            return false;
        }

        this.vaultHook.take(buyer, price);

        AuctionHistoryItem history = new AuctionHistoryItem(listing, buyer);

        if (!this.isSynced()) {
            this.getListings().remove(listing);
            this.getHistory().add(history);
        }

        plugin.getData().addAuctionHistory(history, !this.isSynced());
        plugin.getData().deleteAuctionListing(listing, !this.isSynced());

        ItemStack item = listing.getItemStack();
        ItemUT.addItem(buyer, item);

        plugin.lang().Auction_Listing_Buy_Success_Info
                .replace("%amount%", item.getAmount())
                .replace("%item%", ItemUT.getItemName(item))
                .replace("%seller%", listing.getOwnerName())
                .replace("%price%", NumberUT.format(price))
                .send(buyer);

        Player owner = this.plugin.getServer().getPlayer(listing.getOwner());
        if (owner != null) {
            this.payHistory(owner, history);
        }

        this.getAuctionGUI().refill();
        return true;
    }

    private boolean payHistory(@NotNull Player player, @NotNull AuctionHistoryItem historyItem) {
        if (historyItem.isPaid()) return historyItem.isPaid();

        this.vaultHook.give(player, historyItem.getPrice());
        historyItem.setPaid(true);
        this.plugin.getData().saveAuctionHistory(historyItem, !this.isSynced());

        ItemStack item = historyItem.getItemStack();

        plugin.lang().Auction_Listing_Sell_Success_Info
                .replace("%amount%", item.getAmount())
                .replace("%item%", ItemUT.getItemName(item))
                .replace("%buyer%", historyItem.getBuyerName())
                .replace("%price%", NumberUT.format(historyItem.getPrice()))
                .send(player);

        return historyItem.isPaid();
    }

    public void takeExpired(@NotNull Player player, @NotNull AuctionListing listing) {
        if (!this.getExpired().removeIf(list -> list.getId().equals(listing.getId()))) return;

        ItemUT.addItem(player, listing.getItemStack());
        this.plugin.getData().deleteAuctionListing(listing, !this.isSynced());

        this.getAuctionGUI().refill();
    }

    public void takeListing(@NotNull Player player, @NotNull AuctionListing listing) {
        if (!this.getListings().removeIf(list -> list.getId().equals(listing.getId()))) return;

        ItemUT.addItem(player, listing.getItemStack());
        this.plugin.getData().deleteAuctionListing(listing, !this.isSynced());

        this.getAuctionGUI().refill();
    }

    private boolean checkDisableds(@NotNull Player player) {
        if (player.hasPermission(Perms.ADMIN)) return true;

        World world = player.getWorld();
        if (AuctionConfig.DISABLED_WORLDS.contains(world.getName().toLowerCase())) {
            plugin.lang().Auction_Error_DisabledWorld.send(player);
            return false;
        }

        return true;
    }

    public boolean openAuction(@NotNull Player player) {
        return this.openAuctionGUI(player, this.getAuctionGUI());
    }

    public boolean openAuctionConfirm(@NotNull Player player, @NotNull AuctionListing listing) {
        if (!this.checkDisableds(player)) return false;

        this.auctionConfirmGUI.open(player, listing);
        return true;
    }

    public boolean openAuctionExpired(@NotNull Player player) {
        return this.openAuctionGUI(player, this.getAuctionExpiredGUI());
    }

    public boolean openAuctionHistory(@NotNull Player player) {
        return this.openAuctionGUI(player, this.getAuctionHistoryGUI());
    }

    private boolean openAuctionGUI(@NotNull Player player, @NotNull IAuctionGUI<?> gui) {
        if (!this.checkDisableds(player)) return false;

        gui.open(player, gui.getUserPage(player, 0));
        return true;
    }

    @NotNull
    public AuctionConfig getAuctionConfig() {
        return auctionConfig;
    }

    @NotNull
    public AuctionMainGUI getAuctionGUI() {
        return this.auctionGUI;
    }

    @NotNull
    public AuctionExpiredGUI getAuctionExpiredGUI() {
        return this.auctionExpiredGUI;
    }

    @NotNull
    public AuctionHistoryGUI getAuctionHistoryGUI() {
        return this.auctionHistoryGUI;
    }

    @Nullable
    public AuctionListing getListing(@NotNull String uuid) {
        Optional<AuctionListing> opt = this.getListings().stream()
                .filter(listing -> listing.getId().toString().equalsIgnoreCase(uuid)).findFirst();
        return opt.orElse(null);
    }

    @NotNull
    public List<AuctionListing> getListings() {
        List<AuctionListing> list = this.isSynced() ? this.getListingsData() : this.listings;

        list.removeIf(listing -> {
            if (!listing.isValid()) {
                this.plugin.getData().deleteAuctionListing(listing, true);
                return true;
            }
            if (listing.isExpired()) {
                if (!this.isSynced()) this.getExpired().add(listing);
                return true;
            }
            return false;
        });

        return list;
    }

    @NotNull
    private List<AuctionListing> getListingsData() {
        return Lists.reverse(plugin.getData().getAuctionListing().stream().filter(lis -> !lis.isExpired()).toList());
    }

    @NotNull
    public List<AuctionListing> getListings(@NotNull Player player) {
        return this.getListings(player.getUniqueId());
    }

    @NotNull
    public List<AuctionListing> getListings(@NotNull UUID id) {
        return this.getItems(id, this.getListings());
    }

    public int getListingsMaximum(@NotNull Player player) {
        return AuctionConfig.getPossibleListings(player);
    }

    @NotNull
    public List<AuctionListing> getExpired() {
        List<AuctionListing> list = this.isSynced() ? this.getExpiredData() : this.expired;

        list.removeIf(listing -> {
            if (!listing.isValid()) {
                this.plugin.getData().deleteAuctionListing(listing, true);
                return true;
            }
            return false;
        });

        return list;
    }

    @NotNull
    private List<AuctionListing> getExpiredData() {
        return Lists.reverse(plugin.getData().getAuctionListing().stream().filter(AuctionListing::isExpired).toList());
    }

    @NotNull
    public List<AuctionListing> getExpired(@NotNull Player player) {
        return this.getExpired(player.getUniqueId());
    }

    @NotNull
    public List<AuctionListing> getExpired(@NotNull UUID id) {
        return this.getItems(id, this.getExpired());
    }

    @NotNull
    public List<AuctionHistoryItem> getHistory() {
        List<AuctionHistoryItem> list = this.isSynced() ? this.getHistoryData() : this.history;

        list.removeIf(listing -> {
            if (!listing.isValid()) {
                this.plugin.getData().deleteAuctionHistory(listing, true);
                return true;
            }
            return false;
        });

        return list;
    }

    @NotNull
    private List<AuctionHistoryItem> getHistoryData() {
        return Lists.reverse(this.plugin.getData().getAuctionHistory());
    }

    @NotNull
    public List<AuctionHistoryItem> getHistory(@NotNull Player player) {
        return this.getHistory(player.getUniqueId());
    }

    @NotNull
    public List<AuctionHistoryItem> getHistory(@NotNull UUID id) {
        return this.getItems(id, this.getHistory());
    }

    @NotNull
    private <T extends AuctionItem> List<T> getItems(@NotNull UUID id, @NotNull List<T> from) {
        return from.stream().filter(listing -> listing != null && listing.getOwner().equals(id)).toList();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSellerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        this.getHistory(player).forEach(listing -> this.payHistory(player, listing));

        int expired = this.getExpired(player).size();
        if (expired > 0) {
            plugin.lang().Auction_Listing_Expired_Notify.replace("%amount%", expired).send(player);
        }
    }

    // A hack to update Auction GUIs to display a valid item expire time.
    // Internal animation will not work because items are added in onCreate method.
    class MenuUpdater extends ITask<ExcellentShop> {

        public MenuUpdater() {
            super(AuctionManager.this.plugin, 1, false);
        }

        @Override
        public void action() {
            if (auctionGUI.isAnimated()) {
                auctionGUI.refill();
            }
            if (auctionExpiredGUI.isAnimated()) {
                auctionExpiredGUI.getViewers().forEach(p -> openAuctionExpired(p));
            }
        }
    }
}
