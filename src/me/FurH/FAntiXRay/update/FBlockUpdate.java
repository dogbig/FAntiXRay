/*
 * Copyright (C) 2011-2012 FurmigaHumana.  All rights reserved.
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

package me.FurH.FAntiXRay.update;

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import net.minecraft.server.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FBlockUpdate {

    /* TODO: BETTER BLOCK UPDATE */
    public static void update(Block block, boolean explosion) {
        Location location = block.getLocation();
        FConfiguration config = FAntiXRay.getConfiguration();

        if (config.disabled_worlds.contains(block.getWorld().getName())) {
            return;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        int radius = config.update_radius;

        if (explosion) {
            radius = 1;
        }

        int engine_mode = FAntiXRay.getConfiguration().engine_mode;

        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        
        for (int a = x-radius; a <= x + radius; a++) {
            for (int b = y-radius; b <= y + radius; b++) {
                for (int c = z-radius; c <= z + radius; c++) {
                    if (a == x && b == y && c == z) { continue; }
                    
                    Block block2 = block.getWorld().getBlockAt(a, b, c);

                    if (engine_mode == 0 || engine_mode == 1) {
                        if (FAntiXRay.getConfiguration().hidden_blocks.contains(block2.getTypeId())) {
                            if (engine_mode == 0) {
                                if (radius <= 1 & !explosion) {
                                    worldServer.notify(a, b, c);
                                } else {
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        if (p.getLocation().distance(block2.getLocation()) <= 100) {
                                            p.sendBlockChange(block2.getLocation(), block2.getType(), block2.getData());
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (radius <= 1 & !explosion) {
                            worldServer.notify(a, b, c);
                        } else {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.getLocation().distance(block2.getLocation()) <= 100) {
                                    p.sendBlockChange(block2.getLocation(), block2.getType(), block2.getData());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
