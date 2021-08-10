package su.nightexpress.nexshop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nightexpress.nexshop.data.UserManager;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;

public class ShopAPI {

    private static ExcellentShop plugin;

    static {
        plugin = ExcellentShop.getInstance();
    }

    @Nullable
    public static VirtualShop getVirtualShop() {
        return plugin.getVirtualShop();
    }

    @Nullable
    public static ChestShop getChestShop() {
        return plugin.getChestShop();
    }

    @NotNull
    public static UserManager getUserManager() {
        return (UserManager) plugin.getUserManager();
    }
}
