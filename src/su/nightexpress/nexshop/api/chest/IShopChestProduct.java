package su.nightexpress.nexshop.api.chest;

import org.jetbrains.annotations.NotNull;

import su.nightexpress.nexshop.api.IShopProduct;

public interface IShopChestProduct extends IShopProduct {

    @Override
    @NotNull
    IShopChest getShop();
}
