package su.nightexpress.nexshop.shop.chest.compatibility;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.nexshop.ExcellentShop;

public class GriefPreventionHK extends NHook<ExcellentShop> implements ClaimHook {

    private GriefPrevention griefPrevention;

    public GriefPreventionHK(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        this.griefPrevention = GriefPrevention.instance;
        return this.griefPrevention != null ? HookState.SUCCESS : HookState.ERROR;
    }

    @Override
    protected void shutdown() {

    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        return this.griefPrevention.allowBuild(player, block.getLocation()) == null;
    }

}
