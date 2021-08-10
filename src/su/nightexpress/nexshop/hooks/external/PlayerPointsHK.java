package su.nightexpress.nexshop.hooks.external;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.AbstractShopCurrency;
import su.nightexpress.nexshop.currency.CurrencyType;

public class PlayerPointsHK extends NHook<ExcellentShop> {

    private PlayerPointsAPI api;

    public PlayerPointsHK(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        PlayerPoints points = (PlayerPoints) plugin.getPluginManager().getPlugin(this.getPlugin());
        if (points == null) return HookState.ERROR;

        this.api = points.getAPI();
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {

    }

    public class Currency extends AbstractShopCurrency {

        public Currency(@NotNull String name, @NotNull String format) {
            super(CurrencyType.PLAYER_POINTS, name, format);
        }

        @Override
        public boolean hasOfflineSupport() {
            return true;
        }

        @Override
        public double getBalance(@NotNull OfflinePlayer player) {
            return api.look(player.getUniqueId());
        }

        @Override
        public void give(@NotNull OfflinePlayer player, double amount) {
            api.give(player.getUniqueId(), (int) amount);
        }

        @Override
        public void take(@NotNull OfflinePlayer player, double amount) {
            api.take(player.getUniqueId(), (int) amount);
        }
    }
}
