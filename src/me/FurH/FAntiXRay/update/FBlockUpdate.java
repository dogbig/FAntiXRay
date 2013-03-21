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

package me.FurH.FAntiXRay.update;

import java.util.HashSet;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import net.minecraft.server.v1_5_R2.ChunkPosition;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.Packet14BlockDig;
import net.minecraft.server.v1_5_R2.Packet15Place;
import net.minecraft.server.v1_5_R2.Packet60Explosion;
import net.minecraft.server.v1_5_R2.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FBlockUpdate {

    public static void update(EntityPlayer player, Packet15Place packet) {
        if (FAntiXRay.getConfiguration().block_threaded) {
            if (FAntiXRay.getConfiguration().dark_update) {
                update(new Location(player.world.getWorld(), packet.d(), packet.f(), packet.g()), true);
            } else
            if (FAntiXRay.getConfiguration().block_place) {
                update(new Location(player.world.getWorld(), packet.d(), packet.f(), packet.g()));
            }
        }
    }

    public static void update(EntityPlayer player, Packet14BlockDig packet) {
        if (FAntiXRay.getConfiguration().block_threaded) {
            if (packet.e == 0) {
                update(new Location(player.world.getWorld(), packet.a, packet.b, packet.c), false);
            }
        }
    }

    public static void update(EntityPlayer player, Packet60Explosion packet) {
        if (FAntiXRay.getConfiguration().block_explosion && FAntiXRay.getConfiguration().block_threaded) {
            update(player.world.getWorld().getHandle(), packet.e);
        }
    }
    
    public static void update(Player player, Location loc) {
        if (FAntiXRay.isExempt(player.getName())) {
            return;
        }

        update(loc);
    }

    public static void update(Location loc) {
        FConfiguration config = FAntiXRay.getConfiguration();

        int radius = config.dark_radius;
        if (radius <= 0) {
            return;
        }

        if (FObfuscator.disabled_worlds.contains(loc.getWorld().getName())) {
            return;
        }

        WorldServer worldServer = ((CraftWorld) loc.getWorld()).getHandle();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    if ((loc.getBlockX() + x) == loc.getBlockX() && (loc.getBlockY() + y) == loc.getBlockY() && (loc.getBlockZ() + z) == loc.getBlockZ()) {
                        continue;
                    }

                    Location center = new Location(loc.getWorld(), loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                    int id = worldServer.getTypeId(center.getBlockX(), center.getBlockY(), center.getBlockZ());

                    if (center.distanceSquared(loc) > radius) {
                        continue;
                    }

                    boolean update = false;
                    if (FObfuscator.caves_enabled && id == 1) {
                        update = true;
                    } else
                    if (!FObfuscator.caves_enabled && FObfuscator.hidden_blocks.contains(id) && isBlocksInLight(worldServer, center)) {
                        update = true;
                    }

                    if (update) {
                        worldServer.notify(center.getBlockX(), center.getBlockY(), center.getBlockZ());
                    }
                }
            }
        }
    }

    private static boolean isBlocksInLight(WorldServer worldServer, Location center) {
        if (worldServer.getLightLevel(center.getBlockX() + 1, center.getBlockY(), center.getBlockZ()) > 0) {
            return true;
        }
        if (worldServer.getLightLevel(center.getBlockX() - 1, center.getBlockY(), center.getBlockZ()) > 0) {
            return true;
        }
        if (worldServer.getLightLevel(center.getBlockX(), center.getBlockY(), center.getBlockZ() + 1) > 0) {
            return true;
        }
        if (worldServer.getLightLevel(center.getBlockX(), center.getBlockY(), center.getBlockZ() - 1) > 0) {
            return true;
        }
        if (worldServer.getLightLevel(center.getBlockX(), center.getBlockY() + 1, center.getBlockZ()) > 0) {
            return true;
        }
        if (worldServer.getLightLevel(center.getBlockX(), center.getBlockY() - 1, center.getBlockZ()) > 0) {
            return true;
        }
        return false;
    }

    public static void update(WorldServer worldServer, List chunkPositions) {
        HashSet<Integer[]> hash = new HashSet<Integer[]>();

        if (chunkPositions.isEmpty()) { return; }

        FConfiguration config = FAntiXRay.getConfiguration();
        if (FObfuscator.disabled_worlds.contains(worldServer.getWorld().getName())) {
            return;
        }

        int radius = config.update_radius;
        if (radius <= 0) {
            return;
        }

        for (Object obj : chunkPositions) {
            ChunkPosition position = (ChunkPosition)obj;
            
            if (worldServer.getTypeId(position.x, position.y, position.z) != 0) {
                HashSet<Integer[]> bls = getBlocks(position);
                for (Integer[] ints : bls) {
                    if (!hash.contains(ints)) {
                        hash.add(ints);
                    }
                }
            }
        }

        for (Integer[] data : hash) {
            worldServer.notify(data[0], data[1], data[2]);
        }
    }

    public static void update(Player p, Location loc, boolean fast) {
        if (FAntiXRay.isExempt(p.getName())) {
            return;
        }
        
        update(loc, fast);
    }
    
    public static void update(Location loc, boolean fast) {
        FConfiguration config = FAntiXRay.getConfiguration();

        int radius = config.update_radius;
        if (radius <= 0) {
            return;
        }
        
        if (fast) {
            radius = 1;
        }

        if (FObfuscator.disabled_worlds.contains(loc.getWorld().getName())) {
            return;
        }

        WorldServer worldServer = ((CraftWorld) loc.getWorld()).getHandle();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    if ((loc.getBlockX() + x) == loc.getBlockX() && (loc.getBlockY() + y) == loc.getBlockY() && (loc.getBlockZ() + z) == loc.getBlockZ()) {
                        continue;
                    }

                    Location center = new Location(loc.getWorld(), loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                    int id = worldServer.getTypeId(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);

                    if (center.distanceSquared(loc) > radius) {
                        continue;
                    }

                    boolean update = false;
                    if (FObfuscator.caves_enabled && id == 1) {
                        update = true;
                    } else
                    if (!FObfuscator.caves_enabled && FObfuscator.hidden_blocks.contains(id)) {
                        update = true;
                    } else
                    if (FObfuscator.engine_mode >= 2) {
                        if (id == 1 || id == 3 || id == 13) {
                            update = true;
                        }
                    }

                    if (update) {
                        worldServer.notify(center.getBlockX(), center.getBlockY(), center.getBlockZ());
                    }
                }
            }
        }
    }

    private static HashSet<Integer[]> getBlocks(ChunkPosition position) {
        HashSet<Integer[]> blocks = new HashSet<Integer[]>();

        int x = position.x;
        int y = position.y;
        int z = position.z;

        blocks.add(newInt(x + 1, y - 1, z));
        blocks.add(newInt(x - 1, y - 1, z));
        blocks.add(newInt(x, y - 1, z));
        blocks.add(newInt(x, y - 1, z + 1));
        blocks.add(newInt(x, y - 1, z - 1));

        return blocks;
    }
    
    private static Integer[] newInt(int x, int y, int z) {
        return new Integer[] { x, y, z };
    }
}