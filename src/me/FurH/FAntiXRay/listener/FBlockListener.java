package me.FurH.FAntiXRay.listener;

import java.util.ArrayList;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import org.bukkit.Location;
import org.bukkit.block.Block;
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

        List<Player> entities = new ArrayList<>();

        entities.add(player);

        for (Entity ent : player.getNearbyEntities(10, 5, 10)) {
            if (ent instanceof Player) {
                Player player2 = (Player)ent;
                if (player2.hasLineOfSight(player)) {
                    entities.add(player2);
                }
            }
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
                    for (Player player2 : entities) {
                        Block block = location.getWorld().getBlockAt(a, b, c);
                        if (engine_mode == 0 || engine_mode == 1) {
                            if (config.hidden_blocks.contains(block.getTypeId())) {
                                player2.sendBlockChange(block.getLocation(), block.getType(), block.getData());
                            }
                        } else {
                            player2.sendBlockChange(block.getLocation(), block.getType(), block.getData());
                        }
                    }
                }
            }
        }
    }
}
