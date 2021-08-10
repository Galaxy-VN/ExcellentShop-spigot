package su.nightexpress.nexshop.shop.virtual.editor;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.editor.EditorHandler;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IShopDiscount;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.handler.EditorHandlerDiscount;
import su.nightexpress.nexshop.shop.virtual.editor.handler.EditorHandlerProduct;
import su.nightexpress.nexshop.shop.virtual.editor.handler.EditorHandlerShop;
import su.nightexpress.nexshop.shop.virtual.editor.object.EditorShopList;

public class VirtualEditorHandler extends EditorHandler<ExcellentShop> {

    public static JYML SHOP_LIST_YML;
    public static JYML SHOP_MAIN_YML;
    public static JYML SHOP_DISCOUNTS_YML;
    public static JYML SHOP_PRODUCT_LIST_YML;
    public static JYML SHOP_PRODUCT_MAIN_YML;

    private final EditorShopList editorShopList;

    private final EditorHandlerShop     handlerShop;
    private final EditorHandlerProduct  handlerProduct;
    private final EditorHandlerDiscount handlerDiscount;

    public VirtualEditorHandler(@NotNull VirtualShop virtualShop) {
        super(virtualShop.plugin, VirtualEditorType.class, null);

        SHOP_LIST_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_list.yml");
        SHOP_MAIN_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_main.yml");
        SHOP_PRODUCT_LIST_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_product_list.yml");
        SHOP_PRODUCT_MAIN_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_product.yml");
        SHOP_DISCOUNTS_YML = JYML.loadOrExtract(plugin, virtualShop.getPath() + "editor/shop_discounts.yml");

        this.editorShopList = new EditorShopList(virtualShop);

        this.handlerShop = new EditorHandlerShop(virtualShop);
        this.handlerProduct = new EditorHandlerProduct(this.plugin);
        this.handlerDiscount = new EditorHandlerDiscount(virtualShop);
    }

    @Override
    protected boolean onType(@NotNull Player player, @Nullable Object obj, @NotNull Enum<?> type2, @NotNull String msg) {
        VirtualEditorType type = (VirtualEditorType) type2;

        if (obj instanceof IShopVirtual || (type == VirtualEditorType.CREATE_SHOP && obj == null)) {
            return this.getHandlerShop().onType(player, (IShopVirtual) obj, type, msg);
        }
        if (obj instanceof IShopProduct) {
            return this.getHandlerProduct().onType(player, (IShopProduct) obj, type, msg);
        }
        if (obj instanceof IShopDiscount) {
            return this.getHandlerDiscount().onType(player, (IShopDiscount) obj, type, msg);
        }

        return true;
    }

    @NotNull
    public EditorShopList getEditorShopList() {
        return this.editorShopList;
    }

    @NotNull
    public EditorHandlerShop getHandlerShop() {
        return this.handlerShop;
    }

    @NotNull
    public EditorHandlerProduct getHandlerProduct() {
        return this.handlerProduct;
    }

    @NotNull
    public EditorHandlerDiscount getHandlerDiscount() {
        return this.handlerDiscount;
    }
}
