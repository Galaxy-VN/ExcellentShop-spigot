package su.nightexpress.nexshop.modules.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.modules.ShopModule;

public class ModuleReloadCmd extends ShopModuleCommand<ShopModule> {

    public ModuleReloadCmd(@NotNull ShopModule m) {
        super(m, new String[]{"reload"}, Perms.ADMIN);
    }

    @Override
    @NotNull
    public String usage() {
        return "";
    }

    @Override
    @NotNull
    public String description() {
        return "Reload the module.";
    }

    @Override
    public boolean playersOnly() {
        return false;
    }

    @Override
    public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        this.module.reload();
        this.plugin.lang().Module_Cmd_Reload.replace("%module%", module.name()).send(sender);
    }
}
