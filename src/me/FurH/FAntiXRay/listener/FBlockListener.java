/*
 * Copyright (C) 2011-2013 FurmigaHumana.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.FurH.FAntiXRay.listener;

import java.util.ArrayList;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.update.FBlockUpdate;
import net.minecraft.server.v1_4_R1.ChunkPosition;
import net.minecraft.server.v1_4_R1.WorldServer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
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
        
        if (!config.block_threaded && config.block_damage) {
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
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled()) { return; }

        FConfiguration config = FAntiXRay.getConfiguration();
        if (!config.block_threaded && config.block_place) {
            FBlockUpdate.update(e.getPlayer(), e.getBlock().getLocation(), true);
        }

        if (!config.block_threaded && config.dark_update && config.dark_blocks.contains(e.getBlockAgainst().getTypeId())) {
            FBlockUpdate.update(e.getPlayer(), e.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) { return; }

        FConfiguration config = FAntiXRay.getConfiguration();
        if (!config.block_threaded) {
            FBlockUpdate.update(e.getPlayer(), e.getBlock().getLocation(), false);
        }
    }

    public class FBlockDamage implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockDamage(BlockDamageEvent e) {
            if (e.isCancelled()) { return; }

            FConfiguration config = FAntiXRay.getConfiguration();
            if (!config.block_threaded) {
                FBlockUpdate.update(e.getPlayer(), e.getBlock().getLocation(), true);
            }
        }
    }
    
    public class FBlockPiston implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockPistonExtend(BlockPistonExtendEvent e) {
            if (e.isCancelled()) { return; }
            
            List<ChunkPosition> positions = new ArrayList<ChunkPosition>();
            WorldServer worldServer = ((CraftWorld) e.getBlock().getWorld()).getHandle();

            for (Block b : e.getBlocks()) {
                positions.add(new ChunkPosition(b.getX(), b.getY(), b.getZ()));
            }

            FBlockUpdate.update(worldServer, positions);
        }
        
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockPistonRetract(BlockPistonRetractEvent e) {
            if (e.isCancelled()) { return; }

            FBlockUpdate.update(e.getBlock().getLocation(), true);
        }
    }
    
    public class FBlockPhysics implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockPhysics(BlockPhysicsEvent e) {
            if (e.isCancelled()) { return; }
            
            if (e.getBlock().getType() == Material.GRAVEL || e.getBlock().getType() == Material.SAND) {
                FBlockUpdate.update(e.getBlock().getLocation(), true);
            }
        }
    }
}
    
