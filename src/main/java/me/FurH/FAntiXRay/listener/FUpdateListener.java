package me.FurH.FAntiXRay.listener;

import java.util.HashMap;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.threads.UpdateThreads;
import me.FurH.FAntiXRay.threads.UpdateThreads.UpdateType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class FUpdateListener implements Listener {
    
    private HashMap<String, Location> locations = new HashMap<String, Location>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) { return; }

        UpdateThreads.
                update(e.getPlayer(), e.getBlock().getLocation(), FAntiXRay.getConfiguration().update_radius, UpdateType.BLOCK_BREAK);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) { return; }
        
        FConfiguration config = FAntiXRay.getConfiguration();
        
        if (config.light_enabled && config.light_radius > 0 &&
                config.light_blocks.contains(e.getBlockPlaced().getTypeId())) {

            UpdateThreads.update(e.getPlayer(), e.getBlockPlaced().getLocation(), config.light_radius, UpdateType.BLOCK_PLACE);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.isCancelled()) { return; }

        FConfiguration config = FAntiXRay.getConfiguration();

        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock() != null && config.engine_chest || config.engine_mode == 0 || 
                    config.engine_mode == 4 && e.getClickedBlock().getTypeId() == 54) {

                UpdateThreads.update(e.getPlayer(), e.getClickedBlock().getLocation(), 1, UpdateType.PLAYER_INTERACT);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        if (e.isCancelled()) { return; }
        
        FConfiguration config = FAntiXRay.getConfiguration();
        
        if (config.update_piston && config.update_radius > 0) {
            for (Block b : e.getBlocks()) {
                UpdateThreads.update(b.getLocation(), 1, UpdateType.BLOCK_PISTON);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        if (e.isCancelled()) { return; }
        
        FConfiguration config = FAntiXRay.getConfiguration();
        
        if (config.update_piston && config.update_radius > 0) {
            UpdateThreads.update(e.getRetractLocation(), 1, UpdateType.BLOCK_PISTON);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.isCancelled()) { return; }
        
        FConfiguration config = FAntiXRay.getConfiguration();
        if (config.update_explosion && config.update_radius > 0) {
            for (Block b : e.blockList()) {
                UpdateThreads.update(b.getLocation(), 1, UpdateType.BLOCK_EXPLOSION);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled() || e.getTo() == e.getFrom()) { return; }
        
        FConfiguration config = FAntiXRay.getConfiguration();
        if (e.getTo().distanceSquared(e.getFrom()) < config.proximity_radius) {
            return;
        }

        if (config.engine_chest || config.proximity_enabled && config.proximity_radius > 0) {
            UpdateThreads.update(e.getPlayer(), e.getTo(), (config.proximity_radius * 3), UpdateType.PLAYER_TELEPORT);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        if (e.isCancelled()) { return; }
        
        FConfiguration config = FAntiXRay.getConfiguration();
        if (config.engine_chest || config.proximity_enabled && config.proximity_radius > 0) {
            
            if (e.getTo().getBlockY() == e.getFrom().getBlockY() &&
                    e.getTo().getBlockX() == e.getFrom().getBlockX() && 
                    e.getTo().getBlockZ() == e.getFrom().getBlockZ()) {
                return;
            }

            if (!locations.containsKey(e.getPlayer().getName())) {
                locations.put(e.getPlayer().getName(), e.getFrom());
            }

            Location last = locations.get(e.getPlayer().getName());

            if (last.getWorld() != e.getTo().getWorld() || last.distanceSquared(e.getTo()) > config.proximity_distance) {

                UpdateThreads.update(e.getPlayer(), e.getTo(), config.proximity_radius, UpdateType.PLAYER_MOVE);

                locations.put(e.getPlayer().getName(), e.getTo());
            }
        }
    }
}
