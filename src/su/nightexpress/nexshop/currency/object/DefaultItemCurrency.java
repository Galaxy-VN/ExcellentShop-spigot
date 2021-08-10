package su.nightexpress.nexshop.currency.object;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.nexshop.api.currency.AbstractShopCurrency;

public class DefaultItemCurrency extends AbstractShopCurrency {

    private ItemStack item;

    public DefaultItemCurrency(@NotNull String id, @NotNull String name, @NotNull String format, @NotNull ItemStack item) {
        super(id, name, format);
        this.item = item;
    }

    @Override
    public boolean hasOfflineSupport() {
        return false;
    }

    @NotNull
    public ItemStack getItem() {
        return this.item;
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return 0;

        return PlayerUT.countItem(player, this.getItem());
    }

    @Override
    public void give(@NotNull OfflinePlayer offlinePlayer, double amount) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return;

        for (int i = 0; i < amount; i++) {
            ItemUT.addItem(player, this.item);
        }
    }

    @Override
    public void take(@NotNull OfflinePlayer offlinePlayer, double amount) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return;

        PlayerUT.takeItem(player, this.getItem(), (int) amount);
    }
}
