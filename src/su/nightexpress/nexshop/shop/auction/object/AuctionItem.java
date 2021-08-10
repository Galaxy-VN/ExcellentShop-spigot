package su.nightexpress.nexshop.shop.auction.object;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUT;

import java.util.UUID;
import java.util.function.UnaryOperator;

public abstract class AuctionItem {

    public static final String PLACEHOLDER_NAME   = "%item_name%";
    public static final String PLACEHOLDER_LORE   = "%item_lore%";
    public static final String PLACEHOLDER_SELLER = "%seller%";
    public static final String PLACEHOLDER_PRICE  = "%price%";

    protected final UUID      id;
    protected       UUID      owner;
    protected       String    ownerName;
    protected       ItemStack itemStack;
    protected       double    price;

    public AuctionItem(
            @NotNull UUID id,
            @NotNull UUID owner,
            @NotNull String ownerName,
            @NotNull ItemStack itemStack,
            double price
    ) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.itemStack = new ItemStack(itemStack);
        this.price = price;
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
                .replace(PLACEHOLDER_SELLER, this.getOwnerName())
                .replace(PLACEHOLDER_PRICE, NumberUT.formatGroup(this.getPrice()))
                ;
    }

    @NotNull
    public final UUID getId() {
        return id;
    }

    @NotNull
    public UUID getOwner() {
        return owner;
    }

    @NotNull
    public String getOwnerName() {
        return ownerName;
    }

    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getPrice() {
        return price;
    }

    public boolean isOwner(@NotNull Player player) {
        return player.getUniqueId().equals(this.getOwner());
    }
}
