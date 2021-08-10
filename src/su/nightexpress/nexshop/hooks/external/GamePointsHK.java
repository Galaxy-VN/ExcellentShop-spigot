package su.nightexpress.nexshop.hooks.external;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.gamepoints.GamePointsAPI;
import su.nightexpress.gamepoints.data.objects.StoreUser;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.AbstractShopCurrency;
import su.nightexpress.nexshop.currency.CurrencyType;

public class GamePointsHK extends NHook<ExcellentShop> {

    public GamePointsHK(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {

    }

    public static class Currency extends AbstractShopCurrency {

        public Currency(@NotNull String name, @NotNull String format) {
            super(CurrencyType.GAME_POINTS, name, format);
        }

        @Override
        public boolean hasOfflineSupport() {
            return true;
        }

        @Override
        public double getBalance(@NotNull OfflinePlayer player) {
            StoreUser user = GamePointsAPI.getUserData(player.getUniqueId().toString(), true);
            return user == null ? 0 : user.getBalance();
        }

        @Override
        public void give(@NotNull OfflinePlayer player, double amount) {
            StoreUser user = GamePointsAPI.getUserData(player.getUniqueId().toString(), true);
            if (user != null) user.addPoints((int) amount);
        }

        @Override
        public void take(@NotNull OfflinePlayer player, double amount) {
            StoreUser user = GamePointsAPI.getUserData(player.getUniqueId().toString(), true);
            if (user != null) user.takePoints((int) amount);
        }
    }
}
