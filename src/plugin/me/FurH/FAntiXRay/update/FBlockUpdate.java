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

import java.util.HashSet;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import net.minecraft.server.v1_4_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FBlockUpdate {

    public static void update(Player p, Block b) {
        FConfiguration config = FAntiXRay.getConfiguration();

        if (FAntiXRay.isExempt(p.getName())) {
            return;
        }
        
        if (config.disabled_worlds.contains(b.getWorld().getName())) {
            return;
        }

        int radius = config.dark_radius;
        if (radius <= 0) {
            return;
        }

        WorldServer worldServer = ((CraftWorld) b.getWorld()).getHandle();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block center = b.getWorld().getBlockAt(b.getX() + x, b.getY() + y, b.getZ() + z);

                    if (center.getType() != Material.AIR && center != null) {
                        if (!config.caves_enabled && config.hidden_blocks.contains(center.getTypeId()) || config.dark_extra.contains(center.getTypeId())) {
                            update(center, b, radius, worldServer, x, y, z);
                            if (center.getLocation().distance(b.getLocation()) <= radius) {
                                if (isBlocksInLight(center)) {
                                    worldServer.notify(b.getX() + x, b.getY() + y, b.getZ() + z);
                                }
                            }
                        } else
                        if (config.caves_enabled) {
                            update(center, b, radius, worldServer, x, y, z);
                        }
                    }
                }
            }
        }
    }
    
    private static void update(Block center, Block b, int radius, WorldServer worldServer, int x, int y, int z) {
        if (center.getLocation().distance(b.getLocation()) <= radius) {
            if (isBlocksInLight(center)) {
                worldServer.notify(b.getX() + x, b.getY() + y, b.getZ() + z);
            }
        }
    }
    
    private static boolean isBlocksInLight(Block center) {
        if (center.getRelative(BlockFace.UP).getLightLevel() > 0) {
            return true;
        } else
        if (center.getRelative(BlockFace.DOWN).getLightLevel() > 0) {
            return true;
        } else
        if (center.getRelative(BlockFace.NORTH).getLightLevel() > 0) {
            return true;
        } else
        if (center.getRelative(BlockFace.SOUTH).getLightLevel() > 0) {
            return true;
        } else
        if (center.getRelative(BlockFace.EAST).getLightLevel() > 0) {
            return true;
        } else
        if (center.getRelative(BlockFace.WEST).getLightLevel() > 0) {
            return true;
        }
        return false;
    }

    public static void update(World w, List<Block> blocks) {
        HashSet<Integer[]> hash = new HashSet<>();

        if (blocks.isEmpty()) { return; }

        FConfiguration config = FAntiXRay.getConfiguration();
        if (config.disabled_worlds.contains(w.getName())) {
            return;
        }

        int radius = config.update_radius;
        if (radius <= 0) {
            return;
        }

        for (Block block : blocks) {
            if (block.getTypeId() != 0) {
                HashSet<Integer[]> bls = getBlocks(block.getLocation(), 1);
                for (Integer[] ints : bls) {
                    if (!hash.contains(ints)) {
                        hash.add(ints);
                    }
                }
            }
        }

        WorldServer worldServer = ((CraftWorld) blocks.get(0).getWorld()).getHandle();
        for (Integer[] data : hash) {
            worldServer.notify(data[0], data[1], data[2]);
        }
    }

    public static void update(Player p, Block b, boolean fast) {
        if (!FAntiXRay.isExempt(p.getName())) {
            update(b, fast);
        }
    }
    
    public static void update(Block block, boolean fast) {        
        Location location = block.getLocation();
        FConfiguration config = FAntiXRay.getConfiguration();

        if (config.disabled_worlds.contains(block.getWorld().getName())) {
            return;
        }
        
        if (config.dark_only) {
            if (!isBlocksInLight(block)) {
                return;
            }
        }

        int radius = config.update_radius;
        if (fast) {
            radius = 1;
        }
        
        if (radius <= 0) {
            return;
        }

        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();

        HashSet<Integer[]> blocks = getBlocks(location, radius);
        if (!blocks.isEmpty()) {
            for (Integer[] data : blocks) {
                worldServer.notify(data[0], data[1], data[2]);
            }
        }
    }
    
    private static HashSet<Integer[]> getBlocks(Location loc, int radius) {
        HashSet<Integer[]> blocks = new HashSet<>();

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        /*
         * old school but efficient! Rather than making a loop, with this I only get the blocks I want.
         */
        if (radius >= 1) {
            blocks.add(newInt(x + 1, y - 1, z));
            blocks.add(newInt(x - 1, y - 1, z));
            blocks.add(newInt(x, y - 1, z));
            blocks.add(newInt(x, y - 1, z + 1));
            blocks.add(newInt(x, y - 1, z - 1));
        }

        if (radius >= 2) {
            blocks.add(newInt(x + 1, y - 2, z));
            blocks.add(newInt(x - 1, y - 2, z));
            blocks.add(newInt(x, y - 2, z));
            blocks.add(newInt(x, y - 2, z + 1));
            blocks.add(newInt(x, y - 2, z - 1));
            
            blocks.add(newInt(x, y - 1, z));
            blocks.add(newInt(x, y - 1, z - 1));
            blocks.add(newInt(x, y - 1, z + 1));
            
            blocks.add(newInt(x + 2, y, z));
            blocks.add(newInt(x - 2, y, z));
            blocks.add(newInt(x, y, z + 2));
            blocks.add(newInt(x, y, z - 2));
 
            blocks.add(newInt(x + 1, y, z + 1));
            blocks.add(newInt(x - 1, y, z + 1));
            blocks.add(newInt(x + 1, y , z - 1));
            blocks.add(newInt(x - 1, y, z - 1));
            
            blocks.add(newInt(x + 1, y - 1, z + 1));
            blocks.add(newInt(x - 1, y - 1, z + 1));
            blocks.add(newInt(x + 1, y - 1 , z - 1));
            blocks.add(newInt(x - 1, y - 1, z - 1));
            
            blocks.add(newInt(x + 1, y + 1, z + 1));
            blocks.add(newInt(x - 1, y + 1, z + 1));
            blocks.add(newInt(x + 1, y + 1, z - 1));
            blocks.add(newInt(x - 1, y + 1, z - 1));
            blocks.add(newInt(x + 2, y + 1, z));
            blocks.add(newInt(x - 2, y + 1, z));
            blocks.add(newInt(x, y + 1, z + 2));
            blocks.add(newInt(x, y + 1, z - 2));
            blocks.add(newInt(x, y + 2, z - 1));
            blocks.add(newInt(x, y + 2, z + 1));
            blocks.add(newInt(x + 1, y + 2, z));
            blocks.add(newInt(x - 1, y + 2, z));
        }

        if (radius >= 3) {
            blocks.add(newInt(x + 2, y - 1, z));
            blocks.add(newInt(x - 2, y - 1, z));
            blocks.add(newInt(x, y - 1, z + 2));
            blocks.add(newInt(x, y - 1, z - 2));
            
            blocks.add(newInt(x + 1, y - 1, z + 1));
            blocks.add(newInt(x - 1, y - 1, z + 1));
            blocks.add(newInt(x + 1, y - 1 , z - 1));
            blocks.add(newInt(x - 1, y - 1, z - 1));
        }
        
        return blocks;
    }
    
    private static Integer[] newInt(int x, int y, int z) {
        return new Integer[] { x, y, z };
    }
}