package su.nightexpress.nexshop.shop.chest.nms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.hooks.Hooks;
import su.nightexpress.nexshop.api.chest.IShopChest;

import java.util.Arrays;

public interface ChestNMS {

    ItemStack UNKNOWN = new ItemStack(Material.BARRIER);

    @Nullable
    ArmorStand createHologram(@NotNull IShopChest shop);

    @Nullable
    Item createItem(@NotNull IShopChest shop);

    // Very cool fix for shop display entities.
    // I have no idea why there must be a real player in a chunk to spawn them proreply,
    // but this works so who cares xD
    default boolean isSafeCreation(@NotNull Location loc) {
        return !Arrays.stream(loc.getChunk().getEntities()).noneMatch(e -> e.getType() == EntityType.PLAYER && !Hooks.isNPC(e));
    }
}
