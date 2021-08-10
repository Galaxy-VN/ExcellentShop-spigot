package su.nightexpress.nexshop.shop.virtual.editor.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.manager.editor.EditorManager;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorType;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class EditorHandlerProduct implements IEditorHandler<IShopProduct> {

    private ExcellentShop plugin;

    public EditorHandlerProduct(@NotNull ExcellentShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onType(
            @NotNull Player player, @Nullable IShopProduct product,
            @NotNull VirtualEditorType type, @NotNull String msg) {

        if (product == null) return true;

        switch (type) {
            case PRODUCT_CHANGE_COMMANDS: {
                product.getCommands().add(StringUT.colorRaw(msg));
                break;
            }
            case PRODUCT_CHANGE_CURRENCY: {
                String id = StringUT.colorOff(msg);
                IShopCurrency currency = plugin.getCurrencyManager().getCurrency(id);
                if (currency == null) {
                    EditorManager.errorCustom(player, plugin.lang().Virtual_Shop_Editor_Product_Error_Currency.getMsg());
                    return false;
                }

                product.setCurrency(currency);
                break;
            }
            case PRODUCT_CHANGE_PRICE_SELL_MIN:
            case PRODUCT_CHANGE_PRICE_SELL_MAX:
            case PRODUCT_CHANGE_PRICE_BUY_MAX:
            case PRODUCT_CHANGE_PRICE_BUY_MIN: {
                double price = StringUT.getDouble(StringUT.colorOff(msg), -99, true);
                if (price == -99) {
                    EditorManager.errorNumber(player, false);
                    return false;
                }

                if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MIN) {
                    product.getPricer().setPriceMin(TradeType.BUY, price);
                }
                else if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_BUY_MAX) {
                    product.getPricer().setPriceMax(TradeType.BUY, price);
                }
                else if (type == VirtualEditorType.PRODUCT_CHANGE_PRICE_SELL_MAX) {
                    product.getPricer().setPriceMax(TradeType.SELL, price);
                }
                else {
                    product.getPricer().setPriceMin(TradeType.SELL, price);
                }
                break;
            }
            case PRODUCT_CHANGE_LIMIT_BUY_AMOUNT: {
                double value = StringUT.getDouble(StringUT.colorOff(msg), -1, true);
                product.setBuyLimitAmount(TradeType.BUY, (int) value);
                break;
            }
            case PRODUCT_CHANGE_LIMIT_BUY_RESET: {
                int value = StringUT.getInteger(StringUT.colorOff(msg), -1, true);
                product.setBuyLimitCooldown(TradeType.BUY, value);
                break;
            }
            case PRODUCT_CHANGE_LIMIT_SELL_AMOUNT: {
                double value = StringUT.getDouble(StringUT.colorOff(msg), -1, true);
                product.setBuyLimitAmount(TradeType.SELL, (int) value);
                break;
            }
            case PRODUCT_CHANGE_LIMIT_SELL_RESET: {
                int value = StringUT.getInteger(StringUT.colorOff(msg), -1, true);
                product.setBuyLimitCooldown(TradeType.SELL, value);
                break;
            }
            case PRODUCT_CHANGE_PRICE_RND_TIME_DAY: {
                DayOfWeek day = CollectionsUT.getEnum(msg, DayOfWeek.class);
                if (day == null) {
                    EditorManager.errorEnum(player, DayOfWeek.class);
                    return false;
                }
                product.getPricer().getDays().add(day);
                break;
            }
            case PRODUCT_CHANGE_PRICE_RND_TIME_TIME: {
                String[] raw = msg.split(" ");
                LocalTime[] times = new LocalTime[raw.length];

                for (int count = 0; count < raw.length; count++) {
                    String[] split = raw[count].split(":");
                    int hour = StringUT.getInteger(split[0], 0);
                    int minute = StringUT.getInteger(split.length >= 2 ? split[1] : "0", 0);
                    times[count] = LocalTime.of(hour, minute);
                }
                if (times.length < 2) return false;

                product.getPricer().getTimes().add(times);
                break;
            }
            default: {
                break;
            }
        }

        product.getShop().save();
        product.getEditor().open(player, 1);
        return true;
    }
}
