package me.FurH.FAntiXRay.listener;

import java.util.HashSet;
import me.FurH.FAntiXRay.FAntiXRay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 *
 * @author FurmigaHumana
 */
public class FWorldListener implements Listener {
    public static HashSet<String> chunks = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onChunkload(ChunkLoadEvent e) {

        if (FAntiXRay.getConfiguration().disabled_worlds.contains(e.getWorld().getName())) {
            return;
        }

        if (e.isNewChunk()) {
            chunks.add(e.getChunk().getX() + ":" + e.getChunk().getZ());
        }
    }
}
