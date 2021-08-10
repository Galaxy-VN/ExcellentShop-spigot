package su.nightexpress.nexshop.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.gui.*;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IProductPrepared;
import su.nightexpress.nexshop.api.IShop;
import su.nightexpress.nexshop.api.IShopProduct;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.config.Config;

import java.util.Map;
import java.util.WeakHashMap;

public class ShopGUIAmount extends NGUI<ExcellentShop> {

    private final int[] productSlots;

    private final Map<Player, IProductPrepared> products;
    private final Map<Player, Double>           balance;

    enum ButtonType {
        ADD, SET, TAKE;
    }

    public ShopGUIAmount(@NotNull ExcellentShop plugin, @NotNull JYML cfg, @NotNull TradeType buyType) {
        super(plugin, cfg, buyType.name());
        this.products = new WeakHashMap<>();
        this.balance = new WeakHashMap<>();

        String path = buyType.name() + ".";
        this.productSlots = cfg.getIntArray(path + "product-slots");

        GuiClick click = (p, type, e) -> {
            if (type == null) return;

            IProductPrepared prepared = this.products.get(p);
            if (prepared == null) {
                p.closeInventory();
                return;
            }

            IShopProduct product = prepared.getShopProduct();
            IShop shop = product.getShop();

            if (type instanceof ContentType type2) {
                switch (type2) {
                    case ACCEPT, DECLINE -> {
                        if (type2 == ContentType.ACCEPT) prepared.trade(p, false);
                        int page = 1;
                        if (product instanceof IShopVirtualProduct) {
                            page = ((IShopVirtualProduct) product).getPage();
                        }
                        shop.open(p, page);
                    }
                    default -> {}
                }
                return;
            }

            if (type instanceof ButtonType type2) {
                switch (type2) {
                    case ADD, SET, TAKE -> {
                        ItemStack clicked = e.getCurrentItem();
                        if (clicked == null) return;

                        int btnAmount = clicked.getAmount();
                        int hasAmount = prepared.getAmount();

                        if (type2 == ButtonType.ADD) {
                            hasAmount += btnAmount;
                        }
                        else if (type2 == ButtonType.TAKE) {
                            hasAmount -= btnAmount;
                        }
                        else if (type2 == ButtonType.SET) {
                            hasAmount = btnAmount;
                        }
                        MsgUT.sound(p, Config.SOUND_CART_ADDITEM);

                        int possible = product.getStockAmountLeft(p, prepared.getTradeType());
                        int max = this.getMaxPossibleAmount(prepared);
                        int space = prepared.getTradeType() == TradeType.BUY ? PlayerUT.countItemSpace(p, product.getPreview()) : 9999;
                        hasAmount = possible >= 0 && possible < hasAmount ? possible : hasAmount;
                        hasAmount = Math.min(hasAmount, max);
                        hasAmount = Math.min(space, hasAmount);

                        prepared.setAmount(hasAmount);

                        // Re-open to replace placeholders.
                        this.open(p, 1);
                    }
                    default -> throw new IllegalArgumentException("Unexpected value: " + type2);
                }
            }
        };

        for (String id : cfg.getSection(path + "custom-items")) {
            GuiItem guiItem = cfg.getGuiItem(path + "custom-items." + id, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }

            this.addButton(guiItem);
        }

        for (String id : cfg.getSection(path + "buttons")) {
            GuiItem guiItem = cfg.getGuiItem(path + "buttons." + id, ButtonType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }

            this.addButton(guiItem);
        }
    }

    @Override
    public void open(@NotNull Player player, int page) {
        if (!this.products.containsKey(player)) {
            throw new IllegalStateException("Trying to open an empty amount GUI!");
        }
        super.open(player, page);
    }

    public void open(@NotNull Player player, @NotNull IProductPrepared prepared) {
        this.products.put(player, prepared);
        this.open(player, 1);
    }

    private int getMaxPossibleAmount(@NotNull IProductPrepared prepared) {
        ItemStack preview = prepared.getShopProduct().getPreview();
        int size = preview.getType().getMaxStackSize();
        return size * this.productSlots.length;
    }

    @Override
    protected void onCreate(@NotNull Player player, @NotNull Inventory inv, int page) {
        IProductPrepared prepared = this.products.get(player);
        if (prepared == null) return;

        ItemStack preview = prepared.getShopProduct().getPreview();
        int size = preview.getType().getMaxStackSize();
        int amount = prepared.getAmount();
        int count = 0;
        while (amount > 0 && count < this.productSlots.length) {
            ItemStack preview2 = prepared.getShopProduct().getPreview();
            preview2.setAmount(Math.min(amount, size));

            amount -= size;
            this.addButton(player, new JIcon(preview2), this.productSlots[count++]);
        }
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean ignoreNullClick() {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return true;
    }

    @Override
    protected void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        if (!this.isCacheLocked(player)) {
            this.balance.remove(player);
            this.products.remove(player);
        }
        super.onClose(player, e);
    }

    @Override
    protected void replaceMeta(@NotNull Player player, @NotNull ItemStack item, @NotNull GuiItem guiItem) {
        super.replaceMeta(player, item, guiItem);

        IProductPrepared prepared = this.products.get(player);
        if (prepared == null) return;

        IShopProduct shopProduct = prepared.getShopProduct();
        IShopCurrency currency = shopProduct.getCurrency();

        ItemStack preview = shopProduct.getPreview();
        int amount = prepared.getAmount();
        int stacks = (int) ((double) amount / (double) preview.getType().getMaxStackSize());
        double balance = this.balance.computeIfAbsent(player, k -> currency.getBalance(player));

        ItemUT.replace(item, line -> currency.replacePlaceholders().apply(line)
                .replace("%amount%", NumberUT.formatGroup(amount))
                .replace("%amount_stacks%", String.valueOf(stacks))
                .replace(IShopCurrency.PLACEHOLDER_PRICE, currency.format(prepared.getPrice()))
                .replace(IShopCurrency.PLACEHOLDER_BALANCE, currency.format(balance))
        );
    }
}
