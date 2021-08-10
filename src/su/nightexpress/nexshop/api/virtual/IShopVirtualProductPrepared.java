package su.nightexpress.nexshop.api.virtual;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.IProductPrepared;

public interface IShopVirtualProductPrepared extends IProductPrepared {

    @Override
    @NotNull
    default IShopVirtual getShop() {
        return this.getShopProduct().getShop();
    }

    @Override
    @NotNull
    IShopVirtualProduct getShopProduct();
}
