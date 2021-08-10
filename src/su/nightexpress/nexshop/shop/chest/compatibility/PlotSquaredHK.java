package su.nightexpress.nexshop.shop.chest.compatibility;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.hooks.HookState;
import su.nexmedia.engine.hooks.NHook;
import su.nightexpress.nexshop.ExcellentShop;

import java.util.UUID;

public class PlotSquaredHK extends NHook<ExcellentShop> implements ClaimHook {

    //private PlotAPI api;

    public PlotSquaredHK(ExcellentShop plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        //this.api = new PlotAPI();
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {

    }

    @Override
    public boolean isInOwnClaim(@NotNull Player player, @NotNull Block block) {
        com.github.intellectualsites.plotsquared.plot.object.Location location = BukkitUtil.getLocation(block.getLocation());
        //PlotArea area = location.getPlotArea();
        if (!location.isPlotArea()) {
            return false;
        }
        Plot plot = location.getOwnedPlotAbs();
        if (plot == null) return false;

        UUID id = player.getUniqueId();
        return plot.getOwners().contains(id) || plot.getMembers().contains(id);
    }
}