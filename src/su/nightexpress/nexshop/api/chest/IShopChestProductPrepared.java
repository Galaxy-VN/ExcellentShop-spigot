package su.nightexpress.nexshop.api.chest;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.IProductPrepared;

public interface IShopChestProductPrepared extends IProductPrepared {

    @Override
    @NotNull
    default IShopChest getShop() {
        return this.getShopProduct().getShop();
    }

    @Override
    @NotNull
    IShopChestProduct getShopProduct();
}
