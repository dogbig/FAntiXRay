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
import me.FurH.FAntiXRay.update.FBlockUpdate;
import net.minecraft.server.v1_4_R1.ChunkPosition;
import net.minecraft.server.v1_4_R1.WorldServer;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 *
 * @author FurmigaHumana
 */
public class FEntityListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.isCancelled()) { return; }

        List<ChunkPosition> positions = new ArrayList<ChunkPosition>();
        WorldServer worldServer = ((CraftWorld) e.getLocation().getWorld()).getHandle();
        
        for (Block b : e.blockList()) {
            positions.add(new ChunkPosition(b.getX(), b.getY(), b.getZ()));
        }
        
        FBlockUpdate.update(worldServer, positions);
    }
}
