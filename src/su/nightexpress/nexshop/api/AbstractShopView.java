package su.nightexpress.nexshop.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.GuiItem;
import su.nexmedia.engine.manager.api.gui.NGUI;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.nexshop.ExcellentShop;

public abstract class AbstractShopView<T extends IShop> extends NGUI<ExcellentShop> {

    protected final @NotNull T shop;

    public AbstractShopView(@NotNull T shop, @NotNull JYML cfg) {
        super(shop.plugin(), cfg, "");
        this.shop = shop;
    }

    @NotNull
    public T getShop() {
        return this.shop;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
        this.displayProducts(player, inv, page);
    }

    public abstract void displayProducts(@NotNull Player player, @NotNull Inventory inv, int page);

    @Override
    protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
        super.replaceMeta(player, item, guiItem);

        ItemUT.replace(item, this.shop.replacePlaceholders());
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return true;
    }

    @Override
    protected boolean ignoreNullClick() {
        return true;
    }
}
