package su.nightexpress.nexshop.currency;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.hooks.external.VaultHK;
import su.nexmedia.engine.manager.IManager;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.currency.object.DefaultExpCurrency;
import su.nightexpress.nexshop.currency.object.DefaultItemCurrency;
import su.nightexpress.nexshop.currency.object.VaultCurrency;
import su.nightexpress.nexshop.hooks.external.GamePointsHK;
import su.nightexpress.nexshop.hooks.external.MySQLTokensHK;
import su.nightexpress.nexshop.hooks.external.PlayerPointsHK;

import java.util.*;

public class CurrencyManager extends IManager<ExcellentShop> {

    private Map<String, IShopCurrency> currencyMap;

    public CurrencyManager(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    public void setup() {
        this.currencyMap = new HashMap<>();
        this.loadDefault();
        this.loadCustom();
    }

    private void loadDefault() {
        this.addMissing(CurrencyType.EXP);
        this.addMissing(CurrencyType.VAULT);
        this.addMissing(CurrencyType.GAME_POINTS);
        this.addMissing(CurrencyType.MYSQL_TOKENS);
        this.addMissing(CurrencyType.PLAYER_POINTS);

        JYML cfg = plugin.cfg().getJYML();
        for (String sId : cfg.getSection("Currency.Default")) {
            String path = "Currency.Default." + sId + ".";
            if (!cfg.getBoolean(path + "Enabled", true)) continue;

            String cName = StringUT.color(cfg.getString(path + "Name", sId));
            String cFormat = StringUT.color(cfg.getString(path + "Format", IShopCurrency.PLACEHOLDER_PRICE));

            IShopCurrency currency = switch (sId) {
                case CurrencyType.EXP -> new DefaultExpCurrency(cName, cFormat);
                case CurrencyType.VAULT -> {
                    VaultHK vault = plugin.getVault();
                    yield vault == null || !vault.hasEconomy() ? null : new VaultCurrency(cName, cFormat, vault);
                }
                case CurrencyType.GAME_POINTS -> {
                    GamePointsHK points = plugin.getHook(GamePointsHK.class);
                    yield points != null ? new GamePointsHK.Currency(cName, cFormat) : null;
                }
                case CurrencyType.MYSQL_TOKENS -> {
                    MySQLTokensHK mTokens = plugin.getHook(MySQLTokensHK.class);
                    yield mTokens != null ? mTokens.new Currency(cName, cFormat) : null;
                }
                case CurrencyType.PLAYER_POINTS -> {
                    PlayerPointsHK points = plugin.getHook(PlayerPointsHK.class);
                    yield points != null ? points.new Currency(cName, cFormat) : null;
                }
                default -> null;
            };
            if (currency == null) continue;
            this.registerCurrency(currency);
        }
    }

    private void loadCustom() {
        JYML cfg = plugin.cfg().getJYML();
        for (String sId : cfg.getSection("Currency.Custom_Item")) {
            String path2 = "Currency.Custom_Item." + sId + ".";
            if (!cfg.getBoolean(path2 + "Enabled", true)) continue;

            String cName = StringUT.color(cfg.getString(path2 + "Name", sId));
            String cFormat = StringUT.color(cfg.getString(path2 + "Format", IShopCurrency.PLACEHOLDER_PRICE));
            ItemStack cItem = cfg.getItem(path2 + "Item");
            if (ItemUT.isAir(cItem)) {
                plugin.error("Invalid item in custom item currency '" + sId + "'.");
                continue;
            }

            DefaultItemCurrency itemCur = new DefaultItemCurrency(sId, cName, cFormat, cItem);
            this.registerCurrency(itemCur);
        }
    }

    private void addMissing(@NotNull String id) {
        JYML cfg = plugin.cfg().getJYML();
        String type = /*currency instanceof DefaultItemCurrency ? "Custom_Item" : */"Default";
        String path = "Currency." + type + "." + id + ".";

        cfg.addMissing(path + "Enabled", true);
        cfg.addMissing(path + "Format", IShopCurrency.PLACEHOLDER_PRICE + " " + IShopCurrency.PLACEHOLDER_NAME);
        cfg.addMissing(path + "Name", StringUT.capitalizeFully(id.replace("_", " ")));
        cfg.saveChanges();
    }

    @Override
    public void shutdown() {
        if (this.currencyMap != null) {
            this.currencyMap.clear();
            this.currencyMap = null;
        }
    }

    public void registerCurrency(@NotNull IShopCurrency currency) {
        this.currencyMap.put(currency.getId(), currency);
        this.plugin.info("Registered currency: " + currency.getId());
    }

    public boolean hasCurrency() {
        return !this.currencyMap.isEmpty();
    }

    @NotNull
    public Collection<IShopCurrency> getCurrencies() {
        return currencyMap.values();
    }

    @Nullable
    public IShopCurrency getCurrency(@NotNull String id) {
        return this.currencyMap.get(id.toLowerCase());
    }

    @NotNull
    public IShopCurrency getCurrencyFirst() {
        Optional<IShopCurrency> opt = this.getCurrencies().stream().filter(Objects::nonNull).findFirst();
        if (opt.isEmpty()) throw new IllegalArgumentException("No currencies are installed!");
        return opt.get();
    }
}
