package su.nightexpress.nexshop.shop.auction.object;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.StringUT;

import java.util.Set;
import java.util.stream.Collectors;

public class AuctionCategory {

    private final String      id;
    private final String      name;
    private final Set<String> materials;

    public AuctionCategory(@NotNull String id, @NotNull String name, @NotNull Set<String> materials) {
        this.id = id.toLowerCase().replace(" ", "_");
        this.name = StringUT.color(name);
        this.materials = materials.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public Set<String> getMaterials() {
        return this.materials;
    }

    public boolean isItemOfThis(@NotNull ItemStack item) {
        return this.isItemOfThis(item.getType());
    }

    public boolean isItemOfThis(@NotNull Material material) {
        return this.isItemOfThis(material.name());
    }

    public boolean isItemOfThis(@NotNull String name) {
        return this.materials.contains(name.toLowerCase()) || this.materials.contains(Constants.MASK_ANY);
    }
}
