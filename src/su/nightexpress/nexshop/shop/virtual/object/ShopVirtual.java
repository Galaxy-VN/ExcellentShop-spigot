package su.nightexpress.nexshop.shop.virtual.object;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.LoadableItem;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.*;
import su.nightexpress.nexshop.api.currency.IShopCurrency;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.api.virtual.IShopVirtual;
import su.nightexpress.nexshop.api.virtual.IShopVirtualProduct;
import su.nightexpress.nexshop.currency.CurrencyType;
import su.nightexpress.nexshop.shop.ProductPricer;
import su.nightexpress.nexshop.shop.ShopDiscount;
import su.nightexpress.nexshop.shop.virtual.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.editor.object.EditorShop;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ShopVirtual extends LoadableItem implements IShopVirtual {

    private final ExcellentShop plugin;
    private final JYML          configProducts;
    private final JYML          configView;

    private int                              pages;
    private boolean                          isPermissionRequired;
    private Map<TradeType, Boolean>          purchaseAllowed;
    private ItemStack                        icon;
    private int[]                            citizensIds;
    private Collection<IShopDiscount>        discounts;
    private Map<String, IShopVirtualProduct> products;

    private AbstractShopView<IShopVirtual> view;
    private EditorShop                     editor;

    public ShopVirtual(@NotNull VirtualShop virtualShop, @NotNull JYML cfg) {
        super(virtualShop.plugin, cfg);
        this.plugin = virtualShop.plugin;

        this.cfg.addMissing("Icon.material", Material.STONE.name());
        this.cfg.saveChanges();

        this.configProducts = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "products.yml");
        this.configView = new JYML(cfg.getFile().getParentFile().getAbsolutePath(), "view.yml");
        this.configView.addMissing("title", StringUT.capitalizeFully(this.getId()));
        this.configView.addMissing("size", 54);
        this.configView.saveChanges();

        this.setPages(cfg.getInt("Pages", 1));
        this.setPermissionRequired(cfg.getBoolean("Permission_Required", false));

        this.purchaseAllowed = new HashMap<>();
        for (TradeType buyType : TradeType.values()) {
            this.setPurchaseAllowed(buyType, cfg.getBoolean("Transaction_Allowed." + buyType.name(), true));
        }

        this.setIcon(cfg.getItem("Icon"));
        this.setCitizensIds(cfg.getIntArray("Citizens.Attached_NPC"));


        this.discounts = new HashSet<>();
        for (String sId : cfg.getSection("Discounts.Custom")) {
            String path = "Discounts.Custom." + sId + ".";
            double dDiscount = cfg.getDouble(path + "Discount", 1D);
            if (dDiscount <= 0) continue;

            Set<DayOfWeek> days = AbstractTimed.parseDays(cfg.getString(path + "Times.Days", ""));
            Set<LocalTime[]> times = AbstractTimed.parseTimes(cfg.getStringList(path + "Times.Times"));
            ShopDiscount discount = new ShopDiscount(days, times, dDiscount);
            this.discounts.add(discount);
        }

        this.products = new HashMap<>();
        for (String sId : this.configProducts.getSection("List")) {
            String path = "List." + sId + ".";

            String path2 = path + "Shop_View.";
            ItemStack shopPreview = configProducts.getItem64(path2 + "Preview");
            if (shopPreview == null || ItemUT.isAir(shopPreview)) {
                virtualShop.error("Invalid product preview of '" + sId + "' in '" + getId() + "' shop!");
                continue;
            }
            int shopSlot = configProducts.getInt(path2 + "Slot", -1);
            int shopPage = configProducts.getInt(path2 + "Page", -1);

            path2 = path + "Discount.";
            boolean isDiscountAllowed = configProducts.getBoolean(path2 + "Allowed");

            path2 = path + "Purchase.";
            String currencyId = configProducts.getString(path2 + "Currency", CurrencyType.VAULT);
            IShopCurrency currency = plugin.getCurrencyManager().getCurrency(currencyId);
            if (currency == null) {
                virtualShop.error("Invalid product currency of '" + sId + "' in '" + getId() + "' shop!");
                continue;
            }

            Map<TradeType, long[]> limit = new HashMap<>();
            Map<TradeType, double[]> priceMinMax = new HashMap<>();
            boolean itemMetaEnabled = configProducts.getBoolean(path2 + "Item_Meta_Enabled");
            for (TradeType tradeType : TradeType.values()) {
                String path3 = path2 + tradeType.name() + ".";
                double priceMin = configProducts.getDouble(path3 + "Price_Min");
                double priceMax = configProducts.getDouble(path3 + "Price_Max");

                priceMinMax.put(tradeType, new double[]{priceMin, priceMax});

                String path4 = path + "Limit." + tradeType.name() + ".";
                int buyLimitAmount = configProducts.getInt(path4 + "Amount", -1);
                long buyLimitCooldown = configProducts.getLong(path4 + "Cooldown", 0);
                limit.put(tradeType, new long[]{buyLimitAmount, buyLimitCooldown});
            }

            path2 = path + "Purchase.Randomizer.";
            boolean isRndEnabled = configProducts.getBoolean(path2 + "Enabled");
            Set<DayOfWeek> rndDays = AbstractTimed.parseDays(configProducts.getString(path2 + "Times.Days", ""));
            Set<LocalTime[]> rndTimes = AbstractTimed.parseTimes(configProducts.getStringList(path2 + "Times.Times"));

            path2 = path + "Reward.";
            ItemStack rewardItem = configProducts.getItem64(path2 + "Item");
            List<String> rewardCommands = configProducts.getStringList(path2 + "Commands");

            IProductPricer pricer = new ProductPricer(priceMinMax, isRndEnabled, rndDays, rndTimes);

            IShopVirtualProduct product = new ShopVirtualProduct(
                    this, sId,

                    shopPreview, shopSlot, shopPage,
                    isDiscountAllowed, itemMetaEnabled,
                    limit,

                    currency, pricer,

                    rewardItem, rewardCommands);

            this.products.put(product.getId(), product);
        }

        this.setupView();
    }

    public ShopVirtual(@NotNull VirtualShop virtualShop, @NotNull String path) {
        this(virtualShop, new JYML(new File(path)));
        this.save();
    }

    @Override
    @NotNull
    public ExcellentShop plugin() {
        return this.plugin;
    }

    @Override
    public void clear() {
        if (this.editor != null) {
            this.editor.shutdown();
            this.editor = null;
        }
        if (this.view != null) {
            this.view.shutdown();
            this.view = null;
        }
        if (this.products != null) {
            this.products.values().forEach(IShopProduct::clear);
            this.products.clear();
        }
    }

    @Override
    protected void save(@NotNull JYML cfg) {
        configView.set("title", this.view.getTitle());
        configView.set("size", this.view.getSize());
        configView.saveChanges();

        cfg.set("Pages", this.getPages());
        cfg.set("Permission_Required", this.isPermissionRequired());
        this.purchaseAllowed.forEach((type, isAllowed) -> {
            cfg.set("Transaction_Allowed." + type.name(), isAllowed);
        });
        cfg.setItem("Icon", this.getIcon());
        cfg.setIntArray("Citizens.Attached_NPC", this.getCitizensIds());

        int c = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
        cfg.set("Discounts.Custom", null);
        for (IShopDiscount discount : this.getDiscounts()) {
            String path = "Discounts.Custom." + (c++) + ".";
            cfg.set(path + "Discount", discount.getDiscountRaw());
            cfg.set(path + "Times.Days", discount.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
            cfg.set(path + "Times.Times", discount.getTimes().stream()
                    .map(times -> times[0].format(formatter) + "-" + times[1].format(formatter))
                    .collect(Collectors.toList()));
        }


        configProducts.set("List", null);
        for (IShopVirtualProduct shopProduct : this.getProducts()) {
            String path = "List." + shopProduct.getId() + ".";

            configProducts.setItem64(path + "Shop_View.Preview", shopProduct.getPreview());
            configProducts.set(path + "Shop_View.Slot", shopProduct.getSlot());
            configProducts.set(path + "Shop_View.Page", shopProduct.getPage());
            configProducts.set(path + "Discount.Allowed", shopProduct.isDiscountAllowed());

            for (TradeType tradeType : TradeType.values()) {
                configProducts.set(path + "Limit." + tradeType.name() + ".Amount", shopProduct.getBuyLimitAmount(tradeType));
                configProducts.set(path + "Limit." + tradeType.name() + ".Cooldown", shopProduct.getBuyLimitCooldown(tradeType));
            }

            configProducts.set(path + "Purchase.Currency", shopProduct.getCurrency().getId());
            configProducts.set(path + "Purchase.Item_Meta_Enabled", shopProduct.isItemMetaEnabled());
            IProductPricer pricer = shopProduct.getPricer();
            for (TradeType buyType : TradeType.values()) {
                String path2 = path + "Purchase." + buyType.name() + ".";
                configProducts.set(path2 + "Price_Min", pricer.getPriceMin(buyType));
                configProducts.set(path2 + "Price_Max", pricer.getPriceMax(buyType));
            }

            String path3 = path + "Purchase.Randomizer.";
            configProducts.set(path3 + "Enabled", pricer.isRandomizerEnabled());
            configProducts.set(path3 + "Times.Days", pricer.getDays().stream().map(DayOfWeek::name).collect(Collectors.joining(",")));
            configProducts.set(path3 + "Times.Times", pricer.getTimes().stream()
                    .map(times -> times[0].format(formatter) + "-" + times[1].format(formatter))
                    .collect(Collectors.toList()));

            configProducts.setItem64(path + "Reward.Item", shopProduct.getItem());
            configProducts.set(path + "Reward.Commands", shopProduct.getCommands());
        }
        configProducts.saveChanges();
    }

    @Override
    @NotNull
    public String getName() {
        return this.getView().getTitle();
    }

    @NotNull
    public JYML getConfigProducts() {
        return this.configProducts;
    }

    @NotNull
    public JYML getConfigView() {
        return this.configView;
    }

    @Override
    @NotNull
    public EditorShop getEditor() {
        if (this.editor == null) {
            this.editor = new EditorShop(this.plugin(), this);
        }
        return this.editor;
    }

    @Override
    public void setupView() {
        this.configView.reload();
        this.view = new ShopVirtualView(this, this.getConfigView());
        this.getEditor().rebuild();
    }

    @Override
    @NotNull
    public AbstractShopView<IShopVirtual> getView() {
        return this.view;
    }

    @Override
    @Deprecated
    public void open(@NotNull Player player, int page) {
        if (!this.hasPermission(player)) {
            plugin.lang().Error_NoPerm.replace("%id%", this.getId()).send(player);
            return;
        }
        VirtualShop guiShop = plugin.getVirtualShop();
        if (guiShop != null && !guiShop.isShopAllowed(player)) {
            return;
        }
        this.getView().open(player, page);
    }

    @Override
    public boolean isPermissionRequired() {
        return this.isPermissionRequired;
    }

    @Override
    public void setPermissionRequired(boolean isPermission) {
        this.isPermissionRequired = isPermission;
    }

    @Override
    public boolean isPurchaseAllowed(@NotNull TradeType buyType) {
        return this.purchaseAllowed.getOrDefault(buyType, true);
    }

    @Override
    public void setPurchaseAllowed(@NotNull TradeType buyType, boolean isAllowed) {
        this.purchaseAllowed.put(buyType, isAllowed);
    }

    @Override
    @NotNull
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(this.icon);
        ItemUT.replace(icon, this.replacePlaceholders());
        return icon;
    }

    @Override
    public void setIcon(@NotNull ItemStack icon) {
        this.icon = new ItemStack(icon);
        this.icon.setAmount(1);
    }

    @Override
    public int getPages() {
        return this.pages;
    }

    @Override
    public void setPages(int pages) {
        this.pages = Math.max(1, pages);
    }

    @Override
    public int[] getCitizensIds() {
        return this.citizensIds;
    }

    @Override
    public void setCitizensIds(int[] npcIds) {
        this.citizensIds = npcIds;
    }

    @Override
    @NotNull
    public Collection<IShopDiscount> getDiscounts() {
        return this.discounts;
    }

    @Override
    @NotNull
    public Map<String, IShopVirtualProduct> getProductMap() {
        return this.products;
    }
}
