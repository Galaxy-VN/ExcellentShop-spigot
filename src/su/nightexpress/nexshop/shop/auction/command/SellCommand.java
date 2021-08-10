package su.nightexpress.nexshop.shop.auction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.modules.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.auction.AuctionManager;

public class SellCommand extends ShopModuleCommand<AuctionManager> {

    public SellCommand(@NotNull AuctionManager module) {
        super(module, new String[]{"sell"}, Perms.AUCTION_CMD_SELL);
    }

    @Override
    @NotNull
    public String usage() {
        return plugin.lang().Auction_Command_Sell_Usage.getMsg();
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Auction_Command_Sell_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            this.printUsage(sender);
            return;
        }

        Player player = (Player) sender;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUT.isAir(item)) {
            this.errItem(sender);
            return;
        }

        double price = this.getNumD(sender, args[1], -1, false);
        if (price < 0) return;

        if (this.module.add(player, item, price)) {
            player.getInventory().setItemInMainHand(null);
        }
    }
}
