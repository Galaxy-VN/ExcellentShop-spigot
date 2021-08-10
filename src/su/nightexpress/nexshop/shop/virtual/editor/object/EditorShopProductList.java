package su.nightexpress.nexshop.shop.virtual.editor.object;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.*;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.shop.virtual.editor.VirtualEditorHandler;
import su.nightexpress.nexshop.shop.virtual.object.ShopVirtualProduct;

import java.util.*;
import java.util.stream.IntStream;

public class EditorShopProductList extends NGUI<ExcellentShop> {

    private static final Map<String, IShopVirtualProduct> PRODUCT_CACHE = new HashMap<>();
    private static       ItemStack                        freeSlot;

    private static String       productName;
    private static List<String> productLore;

    private       IShopVirtual  shop;
    private final NamespacedKey keyProductCache;

    public EditorShopProductList(@NotNull ExcellentShop plugin, @NotNull IShopVirtual shop) {
        super(plugin, shop.getView().getTitle(), shop.getView().getSize());
        this.shop = shop;
        this.keyProductCache = new NamespacedKey(plugin, "product_cache");

        JYML cfg = VirtualEditorHandler.SHOP_PRODUCT_LIST_YML;

        freeSlot = cfg.getItem("free-slot");
        if (ItemUT.isAir(freeSlot)) {
            JIcon icon = new JIcon(Material.LIME_STAINED_GLASS_PANE);
            icon.setName("&a&lFree Slot!");
            freeSlot = icon.build();
        }

        productName = StringUT.color(cfg.getString("product.name", "%name%"));
        productLore = StringUT.color(cfg.getStringList("product.lore"));
    }

    @NotNull
    private ItemStack cacheProduct(@NotNull IShopVirtualProduct product) {
        String pId = UUID.randomUUID().toString();
        PRODUCT_CACHE.put(pId, product);

        ItemStack stack = product.getPreview();
        DataUT.setData(stack, this.keyProductCache, pId);
        return stack;
    }

    @Nullable
    private IShopVirtualProduct getCachedProduct(@NotNull ItemStack stack) {
        String pId = DataUT.getStringData(stack, this.keyProductCache);
        if (pId == null) return null;

        DataUT.removeData(stack, this.keyProductCache);
        return PRODUCT_CACHE.remove(pId);
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
        GuiClick click = (p, type, e) -> {
            if (type instanceof ContentType) {
                ContentType type2 = (ContentType) type;
                if (type2 == ContentType.NEXT) {
                    this.open(p, page + 1);
                }
                else if (type2 == ContentType.BACK) {
                    this.open(p, page - 1);
                }
                else if (type2 == ContentType.RETURN) {
                    this.shop.getEditor().open(p, 1);
                }
            }
        };

        Set<Integer> contentSlots = new HashSet<>();
        for (GuiItem item : this.shop.getView().getContent().values()) {
            GuiItem clone = new GuiItem(item);
            clone.setClick(click);
            this.addButton(clone);
            contentSlots.addAll(IntStream.of(clone.getSlots()).boxed().toList());
        }

        for (IShopVirtualProduct shopProduct : this.shop.getProducts()) {
            if (shopProduct.getPage() != page) continue;

            JIcon item = new JIcon(shopProduct.getPreview());
            item.setName(productName.replace("%name%", ItemUT.getItemName(shopProduct.getPreview())));
            item.clearLore();
            for (String line : productLore) {
                item.addLore(line
                        .replace("%sell%", NumberUT.format(shopProduct.getPricer().getPriceSell()))
                        .replace("%buy%", NumberUT.format(shopProduct.getPricer().getPriceBuy(false))));
            }
            item.setClick((p, type, e) -> {
                if (!e.isLeftClick() && !e.isRightClick()) return;

                if (e.isShiftClick()) {
                    if (e.isLeftClick()) {
                        shopProduct.getEditor().open(p, 1);
                    }
                    else if (e.isRightClick()) {
                        this.shop.deleteProduct(shopProduct);
                        this.shop.save();
                        this.open(p, page);
                    }
                    return;
                }

                // Cache clicked product to item stack
                // then remove it from the shop
                ItemStack saved = this.cacheProduct(shopProduct);
                this.shop.deleteProduct(shopProduct);

                // If user wants to replace a clicked product
                // then create or load cached product from an itemstack
                // and add it to the shop
                ItemStack cursor = e.getCursor();
                if (cursor != null && !ItemUT.isAir(cursor)) {
                    IShopVirtualProduct cached = this.getCachedProduct(cursor);
                    if (cached == null) {
                        IShopCurrency currency = plugin.getCurrencyManager().getCurrencyFirst();
                        cached = new ShopVirtualProduct(this.shop, currency, cursor, e.getRawSlot(), page);
                    }
                    else {
                        cached.setSlot(e.getRawSlot());
                        cached.setPage(page);
                    }
                    this.shop.getProductMap().put(cached.getId(), cached);
                }

                this.shop.save();

                // Set cached item to cursor
                // so player can put it somewhere
                e.getView().setCursor(null);
                this.open(p, page);
                p.getOpenInventory().setCursor(saved);
            });
            this.addButton(player, item, shopProduct.getSlot());
            contentSlots.add(shopProduct.getSlot());
        }


        JIcon free = new JIcon(freeSlot);
        free.setClick((p, type, e) -> {
            ItemStack cursor = e.getCursor();
            if (cursor == null || ItemUT.isAir(cursor)) return;

            IShopVirtualProduct shopProduct = this.getCachedProduct(cursor);
            if (shopProduct == null) {
                IShopCurrency currency = plugin.getCurrencyManager().getCurrencyFirst();
                shopProduct = new ShopVirtualProduct(this.shop, currency, cursor, e.getRawSlot(), page);
            }
            else {
                shopProduct.setSlot(e.getRawSlot());
                shopProduct.setPage(page);
            }
            e.getView().setCursor(null);
            this.shop.getProductMap().put(shopProduct.getId(), shopProduct);
            this.shop.save();
            this.open(p, page);
        });

        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (contentSlots.contains(slot)) continue;
            this.addButton(player, free, slot);
        }

        this.setUserPage(player, page, this.shop.getPages()); // Hack for NEXT,BACK items display.
    }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        plugin.runTask((c) -> {
            InventoryView view = player.getOpenInventory();
            if (view.getTopInventory().getHolder() instanceof NGUI<?>) return;

            shop.getEditor().open(player, 1);
        }, false);

        super.onClose(player, e);
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return false;
    }

    @Override
    protected boolean ignoreNullClick() {
        return false;
    }
}
