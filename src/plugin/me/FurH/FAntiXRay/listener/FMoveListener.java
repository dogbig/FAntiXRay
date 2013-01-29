package me.FurH.FAntiXRay.listener;

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.hook.manager.FHookManager;
import me.FurH.FAntiXRay.obfuscation.FChestThread;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author FurmigaHumana
 */
public class FMoveListener implements Listener {
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.isCancelled()) { return; }
        
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() &&
            e.getFrom().getBlockY() == e.getTo().getBlockY() &&
            e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }
        
        FHookManager hook = FAntiXRay.getHookManager();
        if (hook.tasks.containsKey(e.getPlayer().getName())) {
            FChestThread thread = hook.tasks.get(e.getPlayer().getName());
            thread.execute();
        }
    }
}
