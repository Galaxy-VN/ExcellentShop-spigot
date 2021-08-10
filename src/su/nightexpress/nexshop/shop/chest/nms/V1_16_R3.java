package su.nightexpress.nexshop.shop.chest.nms;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nexshop.api.chest.IShopChest;
import su.nightexpress.nexshop.shop.chest.ChestDisplayHandler;
import su.nightexpress.nexshop.shop.chest.ChestShopConfig;

public class V1_16_R3 implements ChestNMS {

    @Override
    public ArmorStand createHologram(@NotNull IShopChest shop) {
        Location loc = shop.getDisplayLocation();
        if (!this.isSafeCreation(loc)) return null;

        org.bukkit.World world = shop.getChest().getWorld();

        World nmsWorld = ((CraftWorld) world).getHandle();
        CustomStand entity = new CustomStand(nmsWorld);
        entity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        entity.setHeadRotation(0);
        entity.setInvisible(true);
        entity.setInvulnerable(true);
        entity.setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(ChestShopConfig.DISPLAY_SHOWCASE));
        entity.setSmall(false);
        entity.setNoGravity(true);
        entity.setCustomNameVisible(true);
        entity.setSilent(true);
        entity.getBukkitEntity().setCustomName(shop.getDisplayText().get(0));

        entity.getEntityType().createCreature(nmsWorld.getMinecraftWorld(),
                null,
                null,
                null,
                new BlockPosition(loc.getX(), loc.getY(), loc.getZ()),
                null, false, false);

        nmsWorld.addEntity(entity);
        entity.getBukkitEntity().teleport(loc);

        return (ArmorStand) entity.getBukkitEntity();
    }

    @Override
    public Item createItem(@NotNull IShopChest shop) {
        Location loc = shop.getDisplayItemLocation();
        if (!this.isSafeCreation(loc)) return null;

        org.bukkit.World world = shop.getChest().getWorld();
        World nmsWorld = ((CraftWorld) world).getHandle();
        EntityItem customItem = new CustomItem(nmsWorld);
        customItem.setPosition(loc.getX(), loc.getY(), loc.getZ());
        customItem.setItemStack(CraftItemStack.asNMSCopy(UNKNOWN));
        customItem.setPickupDelay(Short.MAX_VALUE);
        customItem.setInvulnerable(true);
        customItem.setCustomName(new ChatComponentText(""));

        customItem.getEntityType().createCreature(nmsWorld.getMinecraftWorld(),
                null,
                null,
                null,
                new BlockPosition(loc.getX(), loc.getY(), loc.getZ()),
                null, false, false);

        nmsWorld.addEntity(customItem);
        customItem.getBukkitEntity().teleport(loc);

        return (Item) customItem.getBukkitEntity();
    }

    static class CustomItem extends EntityItem {

        public CustomItem(World world) {
            super(EntityTypes.ITEM, world);
        }

        @Override
        public void tick() {
        }

        @Override
        public void pickup(EntityHuman entityhuman) {
        }

        @Override
        public void inactiveTick() {
        }

        @Override
        public boolean isAlive() {
            return false;
        }

        @Override
        public boolean damageEntity(DamageSource damagesource, float f2) {
            return false;
        }

        @Override
        public void die() {
            if (ChestDisplayHandler.ALLOW_REMOVE) {
                super.die();
            }
        }

        @Override
        public void killEntity() {
            if (ChestDisplayHandler.ALLOW_REMOVE) {
                super.killEntity();
            }
        }
    }

    static class CustomStand extends EntityArmorStand {

        public CustomStand(World world) {
            super(EntityTypes.ARMOR_STAND, world);
            this.disabledSlots = EnumItemSlot.HEAD.getSlotFlag();
        }

        @Override
        public boolean hasArms() {
            return false;
        }

        // .push
        @Override
        protected void C(Entity entity) {
        }

        // .interact
        @Override
        public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, EnumHand enumhand) {
            return EnumInteractionResult.FAIL;
        }

        @Override
        public EnumInteractionResult a(EntityHuman entityhuman, EnumHand enumhand) {
            return EnumInteractionResult.FAIL;
        }

        @Override
        public boolean isAlive() {
            return false;
        }

        @Override
        public boolean isFireProof() {
            return true;
        }

        @Override
        protected void collideNearby() {
        }

        @Override
        public boolean damageEntity(DamageSource damagesource, float f2) {
            return false;
        }

        @Override
        public void die() {
            if (ChestDisplayHandler.ALLOW_REMOVE) {
                super.die();
            }
        }

        @Override
        public void killEntity() {
            if (ChestDisplayHandler.ALLOW_REMOVE) {
                super.killEntity();
            }
        }
    }
}
