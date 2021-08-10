package su.nightexpress.nexshop.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.manager.api.Editable;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.type.TradeType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface IShop extends Editable {

    String PLACEHOLDER_NAME = "%shop_name%";

    @NotNull
    ExcellentShop plugin();

    @NotNull
    String getId();

    @NotNull
    String getName();

    @NotNull
    UnaryOperator<String> replacePlaceholders();

    @Override
    @NotNull
    NGUI<?> getEditor();

    void save();

    @NotNull
    AbstractShopView<? extends IShop> getView();

    void setupView();

    void open(@NotNull Player player, int page);

    boolean isPurchaseAllowed(@NotNull TradeType buyType);

    void setPurchaseAllowed(@NotNull TradeType buyType, boolean isAllowed);

    @NotNull
    Collection<IShopDiscount> getDiscounts();

    default boolean isDiscountAvailable() {
        return this.getDiscount() != null;
    }

    /**
     * Find the first available Discound for this shop of the current day time.
     *
     * @return
     */
    @Nullable
    default IShopDiscount getDiscount() {
        Optional<IShopDiscount> opt = this.getDiscounts().stream().filter(IShopDiscount::isAvailable).findFirst();
        return opt.orElse(null);
    }

    default double getDiscountModifier() {
        IShopDiscount discount = this.getDiscount();
        return discount != null ? discount.getDiscount() : 1D;
    }

    @NotNull
    Map<String, ? extends IShopProduct> getProductMap();

    @NotNull
    default Collection<? extends IShopProduct> getProducts() {
        return this.getProductMap().values();
    }

    @Nullable
    default IShopProduct getProductById(@NotNull String id) {
        return this.getProductMap().get(id);
    }

    default void deleteProduct(@NotNull IShopProduct product) {
        this.deleteProduct(product.getId());
    }

    default void deleteProduct(@NotNull String id) {
        this.getProductMap().remove(id);
    }
}

