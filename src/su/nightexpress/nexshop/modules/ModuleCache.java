package su.nightexpress.nexshop.modules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.shop.auction.AuctionManager;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class ModuleCache {

    private final ExcellentShop plugin;

    private VirtualShop    virtualShop;
    private ChestShop      chestShop;
    private AuctionManager auctionManager;

    public ModuleCache(@NotNull ExcellentShop plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        this.virtualShop = this.plugin.getModuleManager().register(new VirtualShop(plugin));
        this.chestShop = this.plugin.getModuleManager().register(new ChestShop(plugin));
        this.auctionManager = this.plugin.getModuleManager().register(new AuctionManager(plugin));
    }

    @Nullable
    public VirtualShop getVirtualShop() {
        return this.virtualShop;
    }

    @Nullable
    public ChestShop getChestShop() {
        return this.chestShop;
    }

    @Nullable
    public AuctionManager getAuctionManager() {
        return this.auctionManager;
    }
}
