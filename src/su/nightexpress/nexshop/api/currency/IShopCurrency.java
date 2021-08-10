package su.nightexpress.nexshop.api.currency;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public interface IShopCurrency {

    String PLACEHOLDER_PRICE   = "%price%";
    String PLACEHOLDER_BALANCE = "%balance%";
    String PLACEHOLDER_NAME    = "%currency_name%";

    @NotNull
    UnaryOperator<String> replacePlaceholders();

    boolean hasOfflineSupport();

    @NotNull
    String getId();

    @NotNull
    String getName();

    @NotNull
    String format(double price);

    double getBalance(@NotNull OfflinePlayer player);

    void give(@NotNull OfflinePlayer player, double amount);

    void take(@NotNull OfflinePlayer player, double amount);
}
