package su.nightexpress.nexshop.api.virtual;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.TimeUT;
import su.nightexpress.nexshop.Perms;
import su.nightexpress.nexshop.api.AbstractShopView;
import su.nightexpress.nexshop.api.IShop;
import su.nightexpress.nexshop.api.IShopDiscount;
import su.nightexpress.nexshop.shop.virtual.editor.object.EditorShop;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.function.UnaryOperator;

public interface IShopVirtual extends IShop, Cleanable {

    String PLACEHOLDER_PERMISSION_NEED    = "%shop_permission_required%";
    String PLACEHOLDER_DISCOUNT_AVAILABLE = "%shop_discount_available%";
    String PLACEHOLDER_DISCOUNT_AMOUNT    = "%shop_discount_amount%";
    String PLACEHOLDER_DISCOUNT_TIMELEFT  = "%shop_discount_timeleft%";

    @Override
    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        IShopDiscount discount = this.getDiscount();
        String hhTimeLeft;
        if (discount != null) {
            LocalTime[] times = discount.getCurrentTimes();
            if (times != null) {
                Duration dur = Duration.between(LocalTime.now(), times[1]);
                hhTimeLeft = TimeUT.formatTime(dur.toMillis());
            }
            else hhTimeLeft = "-";
        }
        else hhTimeLeft = "-";


        return str -> str
                .replace(PLACEHOLDER_NAME, this.getName())
                .replace(PLACEHOLDER_PERMISSION_NEED, plugin().lang().getBool(this.isPermissionRequired()))
                .replace(PLACEHOLDER_DISCOUNT_AVAILABLE, plugin().lang().getBool(discount != null))
                .replace(PLACEHOLDER_DISCOUNT_AMOUNT, discount != null ? NumberUT.format(discount.getDiscountRaw()) : "-")
                .replace(PLACEHOLDER_DISCOUNT_TIMELEFT, hhTimeLeft)
                ;
    }

    @Override
    @NotNull
    EditorShop getEditor();

    @Override
    @NotNull
    AbstractShopView<IShopVirtual> getView();

    boolean isPermissionRequired();

    void setPermissionRequired(boolean isPermission);

    default boolean hasPermission(@NotNull Player player) {
        if (!this.isPermissionRequired()) return true;

        return player.hasPermission(Perms.VIRTUAL_SHOP + Constants.MASK_ANY)
                || player.hasPermission(Perms.VIRTUAL_SHOP + this.getId());
    }

    @NotNull
    ItemStack getIcon();

    void setIcon(@NotNull ItemStack icon);

    int getPages();

    void setPages(int pages);

    int[] getCitizensIds();

    void setCitizensIds(int[] npcIds);

    @NotNull
    @Override
    Map<String, IShopVirtualProduct> getProductMap();

    @Override
    @NotNull
    default Collection<IShopVirtualProduct> getProducts() {
        return this.getProductMap().values();
    }
}
