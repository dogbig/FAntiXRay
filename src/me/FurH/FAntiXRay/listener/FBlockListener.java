package me.FurH.FAntiXRay.listener;

import java.util.ArrayList;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import net.minecraft.server.WorldServer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 *
 * @author FurmigaHumana
 */
public class FBlockListener implements Listener {
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) { return; }

        Player player = e.getPlayer();
        Location location = e.getBlock().getLocation();
        FConfiguration config = FAntiXRay.getConfiguration();

        if (config.disabled_worlds.contains(player.getWorld().getName())) {
            return;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        int radius = config.update_radius;

        int engine_mode = config.engine_mode;

        for (int a = x-radius; a <= x + radius; a++) {
            for (int b = y-radius; b <= y + radius; b++) {
                for (int c = z-radius; c <= z + radius; c++) {
                    if (a == x && b == y && c == z) { continue; }
                    Block block = location.getWorld().getBlockAt(a, b, c);
                    WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
                    if (engine_mode == 0 || engine_mode == 1) {
                        if (config.hidden_blocks.contains(block.getTypeId())) {
                            worldServer.notify(a, b, c);
                        }
                    } else {
                        worldServer.notify(a, b, c);
                    }
                }
            }
        }
    }
}
