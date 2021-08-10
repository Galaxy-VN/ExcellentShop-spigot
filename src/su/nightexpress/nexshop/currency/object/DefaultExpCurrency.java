package su.nightexpress.nexshop.currency.object;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.nexshop.api.currency.AbstractShopCurrency;
import su.nightexpress.nexshop.currency.CurrencyType;

public class DefaultExpCurrency extends AbstractShopCurrency {

    public DefaultExpCurrency(@NotNull String name, @NotNull String format) {
        super(CurrencyType.EXP, name, format);
    }

    @Override
    public boolean hasOfflineSupport() {
        return false;
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return 0D;

        return PlayerUT.getTotalExperience(player);
    }

    @Override
    public void give(@NotNull OfflinePlayer offlinePlayer, double amount) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return;

        PlayerUT.setExp(player, (int) amount);
    }

    @Override
    public void take(@NotNull OfflinePlayer offlinePlayer, double amount) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return;

        PlayerUT.setExp(player, (int) -amount);
    }
}
