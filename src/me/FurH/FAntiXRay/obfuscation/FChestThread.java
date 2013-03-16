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

package me.FurH.FAntiXRay.obfuscation;

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import net.minecraft.server.v1_5_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R1.CraftWorld;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FChestThread implements Runnable {
    private Location lastLoc;
    private Player player;
    private int taskId;

    public FChestThread(Player player) {
        this.player = player;
        this.lastLoc = player.getLocation();
    }
    
    public int getId() {
        return taskId;
    }
    
    public void setId(int id) {
        this.taskId = id;
    }
    
    public void setLastLoc(Location loc) {
        this.lastLoc = loc;
    }

    @Override
    public void run() {
        execute();
    }
    
    public void execute() {
        FConfiguration config = FAntiXRay.getConfiguration();

        boolean update = false;
        
        if (lastLoc.getWorld() != player.getWorld()) {
            update = true;
        } else
        if (lastLoc.distanceSquared(player.getLocation()) >= config.chest_wark) {
            update = true;
        }

        if (update) {
            update();
        }
    }
    
    public void update() {
        FConfiguration config = FAntiXRay.getConfiguration();
        int radius = config.chest_radius;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    if (x == lastLoc.getBlockX() && y == lastLoc.getBlockY() && z == lastLoc.getBlockZ()) {
                        continue;
                    }

                    Location newLoc = new Location(lastLoc.getWorld(), lastLoc.getBlockX() + x, lastLoc.getBlockY() + y, lastLoc.getBlockZ() + z);
                    if (lastLoc.distanceSquared(newLoc) >= config.chest_radius) {
                        continue;
                    }

                    WorldServer worldServer = ((CraftWorld) lastLoc.getWorld()).getHandle();
                    int id = worldServer.getTypeId(newLoc.getBlockX(), newLoc.getBlockY(), newLoc.getBlockZ());
                    
                    if (id == 54 || config.chest_obf && FObfuscator.hidden_blocks.contains(id)) {
                        player.sendBlockChange(newLoc, id, (byte) worldServer.getData(newLoc.getBlockX(), newLoc.getBlockY(), newLoc.getBlockZ()));
                        //worldServer.notify(newLoc.getBlockX(), newLoc.getBlockY(), newLoc.getBlockZ());
                    }
                }
            }
        }
        this.lastLoc = player.getLocation();
    }
}
