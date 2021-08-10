package su.nightexpress.nexshop.shop.chest.editor;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.editor.EditorHandler;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.api.chest.IShopChestProduct;
import su.nightexpress.nexshop.shop.chest.ChestShop;
import su.nightexpress.nexshop.shop.chest.editor.handler.EditorHandlerProduct;
import su.nightexpress.nexshop.shop.chest.editor.handler.EditorHandlerShop;

public class ChestEditorHandler extends EditorHandler<ExcellentShop> {

    public static JYML CONFIG_SHOP;
    public static JYML CONFIG_SHOP_PRODUCTS;
    public static JYML CONFIG_SHOP_PRODUCT;

    private EditorHandlerShop    handlerShop;
    private EditorHandlerProduct handlerProduct;

    public ChestEditorHandler(@NotNull ChestShop chestShop) {
        super(chestShop.plugin, ChestEditorType.class, null);

        CONFIG_SHOP = JYML.loadOrExtract(plugin, chestShop.getPath() + "editor/shop_main.yml");
        CONFIG_SHOP_PRODUCTS = JYML.loadOrExtract(plugin, chestShop.getPath() + "editor/product_list.yml");
        CONFIG_SHOP_PRODUCT = JYML.loadOrExtract(plugin, chestShop.getPath() + "editor/product_main.yml");

        this.handlerShop = new EditorHandlerShop();
        this.handlerProduct = new EditorHandlerProduct(plugin);
    }

    @Override
    protected boolean onType(@NotNull Player player, @Nullable Object obj, @NotNull Enum<?> type2, @NotNull String msg) {
        ChestEditorType type = (ChestEditorType) type2;
        if (obj instanceof IShopChest) {
            return this.getHandlerShop().onType(player, (IShopChest) obj, type, msg);
        }
        if (obj instanceof IShopChestProduct) {
            return this.getHandlerProduct().onType(player, (IShopChestProduct) obj, type, msg);
        }

        return true;
    }

    @NotNull
    public EditorHandlerShop getHandlerShop() {
        return this.handlerShop;
    }

    @NotNull
    public EditorHandlerProduct getHandlerProduct() {
        return this.handlerProduct;
    }
}
