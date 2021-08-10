package su.nightexpress.nexshop.hooks.external;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.bukkit.mTokens.Inkzzz.Tokens;
import me.bukkit.mTokens.Inkzzz.API.MySQLTokensAPI;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.AbstractShopCurrency;
import su.nightexpress.nexshop.currency.CurrencyType;

public class MySQLTokensHK extends NHook<ExcellentShop> {

    private MySQLTokensAPI api;

    public MySQLTokensHK(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        this.api = Tokens.getInstance().getAPI();

        if (this.api != null) {
            return HookState.SUCCESS;
        }
        return HookState.ERROR;
    }

    @Override
    protected void shutdown() {

    }

    public class Currency extends AbstractShopCurrency {

        public Currency(@NotNull String name, @NotNull String format) {
            super(CurrencyType.MYSQL_TOKENS, name, format);
        }

        @Override
        public boolean hasOfflineSupport() {
            return true;
        }

        @Override
        public double getBalance(@NotNull OfflinePlayer offlinePlayer) {
            Player player = offlinePlayer.getPlayer();
            if (player == null) return 0;

            return api.getTokens(player);
        }

        @Override
        public void take(@NotNull OfflinePlayer offlinePlayer, double amount) {
            Player player = offlinePlayer.getPlayer();
            if (player == null) return;

            api.takeTokens(player, (int) amount);
        }

        @Override
        public void give(@NotNull OfflinePlayer offlinePlayer, double amount) {
            Player player = offlinePlayer.getPlayer();
            if (player == null) return;

            api.giveTokens(player, (int) amount);
        }
    }
}
