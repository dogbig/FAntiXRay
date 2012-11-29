package me.FurH.FAntiXRay.listener;

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.update.FBlockUpdate;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author FurmigaHumana
 */
public class FBlockListener implements Listener {

    public void loadListeners(FAntiXRay plugin) {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        PluginManager pm = plugin.getServer().getPluginManager();
        if (config.block_place) {
            pm.registerEvents(new FBlockPlace(), plugin);
        }
        
        if (config.block_damage) {
            pm.registerEvents(new FBlockDamage(), plugin);
        }
        
        if (config.block_piston) {
            pm.registerEvents(new FBlockPiston(), plugin);
        }
        
        if (config.block_physics) {
            pm.registerEvents(new FBlockPhysics(), plugin);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) { return; }

        FBlockUpdate.update(e.getBlock(), false);
    }
    
    public class FBlockPlace implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockPlace(BlockPlaceEvent e) {
            if (e.isCancelled()) { return; }

            FBlockUpdate.update(e.getBlock(), true);
        }
    }
    
    public class FBlockDamage implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockDamage(BlockDamageEvent e) {
            if (e.isCancelled()) { return; }
            
            FBlockUpdate.update(e.getBlock(), true);
        }
    }
    
    public class FBlockPiston implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockPistonExtend(BlockPistonExtendEvent e) {
            if (e.isCancelled()) { return; }
            
            FBlockUpdate.update(e.getBlocks());
        }
        
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockPistonRetract(BlockPistonRetractEvent e) {
            if (e.isCancelled()) { return; }
            
            if (e.isSticky()) {
                FBlockUpdate.update(e.getBlock(), true);
            }
        }
    }
    
    public class FBlockPhysics implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockPhysics(BlockPhysicsEvent e) {
            if (e.isCancelled()) { return; }
            
            if (e.getBlock().getType() == Material.GRAVEL || e.getBlock().getType() == Material.SAND) {
                FBlockUpdate.update(e.getBlock(), true);
            }
        }
    }
}
    
