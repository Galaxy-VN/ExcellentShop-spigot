package su.nightexpress.nexshop.shop.auction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.modules.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.auction.AuctionManager;

public class AuctionCommand extends ShopModuleCommand<AuctionManager> {

    public AuctionCommand(@NotNull AuctionManager module) {
        super(module, new String[]{"open"}, Perms.AUCTION_CMD_OPEN);
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Auction_Command_Open_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        this.module.openAuction((Player) sender);
    }
}
