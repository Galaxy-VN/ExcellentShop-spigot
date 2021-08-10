package su.nightexpress.nexshop.shop.chest.compatibility;

import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.nexshop.ExcellentShop;

import java.util.UUID;

public class LandsHK extends NHook<ExcellentShop> implements ClaimHook {

    private LandsIntegration lands;

    public LandsHK(@NotNull ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        this.lands = new LandsIntegration(this.plugin, false);
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {

    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        Land land = lands.getLand(block.getLocation());
        UUID id = player.getUniqueId();
        return land != null && (land.getOwnerUID().equals(id) || land.getTrustedPlayers().contains(id));
    }
}
