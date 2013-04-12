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

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.update.FBlockUpdate;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author FurmigaHumana
 */
public class FBlockListener implements Listener {

    public void loadListeners(FAntiXRay plugin) {
        FConfiguration config = FAntiXRay.getConfiguration();
        PluginManager pm = plugin.getServer().getPluginManager();

        if (config.update_piston) {
            pm.registerEvents(new FBlockPiston(), plugin);
        }
    }

    public class FBlockPiston implements Listener {
        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockPistonExtend(BlockPistonExtendEvent e) {
            if (!e.isCancelled()) {
                for (Block b : e.getBlocks()) {
                    FBlockUpdate.queueUpdate(b.getLocation());
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onBlockPistonRetract(BlockPistonRetractEvent e) {
            if (!e.isCancelled()) {
                FBlockUpdate.queueUpdate(e.getRetractLocation());
            }
        }
    }
}