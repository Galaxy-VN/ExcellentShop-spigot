package su.nightexpress.nexshop.shop.virtual.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.modules.command.ShopModuleCommand;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

import java.util.List;

public class OpenCommand extends ShopModuleCommand<VirtualShop> {

    public OpenCommand(@NotNull VirtualShop guiShop) {
        super(guiShop, new String[]{"open"}, Perms.VIRTUAL_CMD_OPEN);
    }

    @Override
    @NotNull
    public String description() {
        return plugin.lang().Virtual_Shop_Command_Open_Desc.getMsg();
    }

    @Override
    @NotNull
    public String usage() {
        return plugin.lang().Virtual_Shop_Command_Open_Usage.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    @NotNull
    public List<@NotNull String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            return module.getShops(player);
        }
        return super.getTab(player, i, args);
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (args.length < 2) {
            module.openMainMenu(player);
            return;
        }

        IShopVirtual shopGUI = this.module.getShopById(args[1]);
        if (shopGUI == null) {
            plugin.lang().Virtual_Shop_Open_Error_InvalidShop.send(player);
            return;
        }

        if (!sender.hasPermission(Perms.VIRTUAL_CMD_OPEN + "." + shopGUI.getId())) {
            this.errPerm(sender);
            return;
        }

        shopGUI.open(player, 1);
    }
}
