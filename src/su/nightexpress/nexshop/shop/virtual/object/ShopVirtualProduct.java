package su.nightexpress.nexshop.shop.virtual.object;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.api.IProductPrepared;
import su.nightexpress.nexshop.api.IProductPricer;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.shop.ProductPricer;
import su.nightexpress.nexshop.shop.virtual.editor.object.EditorShopProduct;

import java.util.*;

public class ShopVirtualProduct implements IShopVirtualProduct {

    private final IShopVirtual shop;
    private final String       id;

    private ItemStack shopPreview;
    private int       shopSlot;
    private int       shopPage;

    private boolean                isDiscountAllowed;
    private boolean                itemMetaEnabled;
    private Map<TradeType, long[]> limit;

    private IShopCurrency  currency;
    private IProductPricer pricer;

    private ItemStack    rewardItem;
    private List<String> rewardCommands;

    private EditorShopProduct editor;

    public ShopVirtualProduct(@NotNull IShopVirtual shop, @NotNull IShopCurrency currency, @NotNull ItemStack item, int slot, int page) {
        this(
                shop, UUID.randomUUID().toString(),

                item, slot, page,

                true, true,                 // Discount, meta
                new HashMap<>(),            // Buy Limit

                currency,
                new ProductPricer(),        // Price manager

                item, new ArrayList<>()     // Reward item, commands
        );
    }

    public ShopVirtualProduct(
            @NotNull IShopVirtual shop,
            @NotNull String id,

            @NotNull ItemStack shopPreview,
            int shopSlot,
            int shopPage,

            boolean isDiscountAllowed,
            boolean itemMetaEnabled,
            @NotNull Map<TradeType, long[]> limit,

            @NotNull IShopCurrency currency,
            @NotNull IProductPricer pricer,

            @Nullable ItemStack rewardItem,
            @NotNull List<String> rewardCommands
    ) {
        this.shop = shop;
        this.id = id;

        this.setPreview(shopPreview);
        this.setSlot(shopSlot);
        this.setPage(shopPage);

        this.setItemMetaEnabled(itemMetaEnabled);
        this.limit = limit;

        this.setCurrency(currency);
        this.pricer = pricer;
        this.pricer.setProduct(this);
        this.setDiscountAllowed(isDiscountAllowed);

        this.setItem(rewardItem);
        this.setCommands(rewardCommands);
    }

    @Override
    public void clear() {
        if (this.editor != null) {
            this.editor.shutdown();
            this.editor = null;
        }
    }

    @Override
    @NotNull
    public EditorShopProduct getEditor() {
        if (this.editor == null) {
            this.editor = new EditorShopProduct(this.getShop().plugin(), this);
        }
        return this.editor;
    }

    @Override
    @NotNull
    public IShopVirtual getShop() {
        return this.shop;
    }

    @Override
    @NotNull
    public String getId() {
        return this.id;
    }

    @Override
    @NotNull
    public IProductPricer getPricer() {
        return this.pricer;
    }

    @Override
    @NotNull
    public IShopCurrency getCurrency() {
        return this.currency;
    }

    @Override
    public void setCurrency(@NotNull IShopCurrency currency) {
        this.currency = currency;
    }

    @Override
    @NotNull
    public IProductPrepared getPrepared(@NotNull TradeType buyType) {
        return new VirtualProductPrepared(this, buyType);
    }

    @Override
    public boolean isDiscountAllowed() {
        return this.isDiscountAllowed;
    }

    @Override
    public void setDiscountAllowed(boolean isAllowed) {
        this.isDiscountAllowed = isAllowed;
    }

    @Override
    public boolean isItemMetaEnabled() {
        return this.itemMetaEnabled;
    }

    @Override
    public void setItemMetaEnabled(boolean isEnabled) {
        this.itemMetaEnabled = isEnabled;
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return new ItemStack(this.shopPreview);
    }

    @Override
    public void setPreview(@NotNull ItemStack preview) {
        this.shopPreview = new ItemStack(preview);
    }

    @Override
    public int getSlot() {
        return this.shopSlot;
    }

    @Override
    public void setSlot(int slot) {
        this.shopSlot = slot;
    }

    @Override
    public int getPage() {
        return this.shopPage;
    }

    @Override
    public void setPage(int page) {
        this.shopPage = page;
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        return this.rewardItem;
    }

    @Override
    public void setItem(@Nullable ItemStack item) {
        this.rewardItem = item == null ? new ItemStack(Material.AIR) : new ItemStack(item);
        this.rewardItem.setAmount(1);
    }

    @Override
    @NotNull
    public List<String> getCommands() {
        return this.rewardCommands;
    }

    @Override
    public void setCommands(@NotNull List<String> commands) {
        this.rewardCommands = commands;
    }

    @Override
    public int getBuyLimitAmount(@NotNull TradeType tradeType) {
        return (int) this.limit.getOrDefault(tradeType, new long[]{-1, -1})[0];
    }

    @Override
    public void setBuyLimitAmount(@NotNull TradeType tradeType, int buyLimitAmount) {
        long[] has = this.limit.getOrDefault(tradeType, new long[]{-1, -1});
        has[0] = buyLimitAmount;
        this.limit.put(tradeType, has);
    }

    @Override
    public long getBuyLimitCooldown(@NotNull TradeType tradeType) {
        return this.limit.getOrDefault(tradeType, new long[]{-1, -1})[1];
    }

    @Override
    public void setBuyLimitCooldown(@NotNull TradeType tradeType, long buyLimitCooldown) {
        long[] has = this.limit.getOrDefault(tradeType, new long[]{-1, -1});
        has[1] = buyLimitCooldown;
        this.limit.put(tradeType, has);
    }
}
