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

import java.util.concurrent.ConcurrentLinkedQueue;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.Packet14BlockDig;
import net.minecraft.server.v1_5_R3.Packet15Place;
import net.minecraft.server.v1_5_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FBlockUpdate {
    
    private static ConcurrentLinkedQueue<Location> queued = new ConcurrentLinkedQueue<Location>();
    private static boolean running = false;
    
    public static void queueUpdate(Location loc) {
        
        if (queued.contains(loc)) {
            return;
        }
        
        if (!running) {
            setup();
        }
        
        queued.add(loc);
    }
    
    public static void setup() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    running = true;
                    try {
                        if (queued.isEmpty()) {
                            sleep(500); return;
                        }
                        
                        Location loc = queued.poll();
                        if (loc != null) {
                            update(loc, true, false);
                        }
                        
                    } catch (InterruptedException ex) { }
                }
            }
        };
        thread.setName("FAntiXRay Update Thread");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    
    public static void update(EntityPlayer player, Packet14BlockDig packet) {
        if (packet.e == 0) {
            update(new Location(player.world.getWorld(), packet.a, packet.b, packet.c), false, false);
        }
    }
    
    public static void update(EntityPlayer player, Packet15Place packet) {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        if (!config.light_enabled) {
            return;
        }
        
        int id = packet.getItemStack().id;
        if (!config.light_blocks.contains(id)) {
            return;
        }

        update(player.o(), packet.d(), packet.f(), packet.g(), false, false, true);
    }
    
    public static void update(Player p, Location loc, boolean fast) {

        if (FAntiXRay.isExempt(p.getName())) {
            return;
        }
        
        update(loc, fast, false);
    }

    public static void update(Location loc, boolean fast, boolean walk) {
        update(((CraftWorld) loc.getWorld()).getHandle(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), fast, walk, false);
    }
    
    public static void update(WorldServer world, int x, int y, int z, boolean fast, boolean walk, boolean light) {

        FConfiguration config = FAntiXRay.getConfiguration();
        int radius = fast ? 1 : walk ? config.proximity_radius : config.update_radius;
        
        if (light) {
            radius = config.light_radius;
        }
        
        if (radius <= 0) {
            return;
        }
        
        if (config.disabled_worlds.contains(world.getWorld().getName())) {
            return;
        }
        
        if (radius == 1) {
            update(world, x, y, z);
            return;
        }

        Location start = new Location(world.getWorld(), x, y, z);

        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                for (int c = -radius; c <= radius; c++) {

                    if (x == a && y == b && c == z) {
                        continue;
                    }

                    Location center = new Location(world.getWorld(), a + x, b + y, c + z);

                    if (center.distanceSquared(start) > radius) {
                        continue;
                    }

                    if (light) { if (FObfuscator.isBlocksInLight(world, a + x, b + y, c + z)) {
                        world.notify(center.getBlockX(), center.getBlockY(), center.getBlockZ());
                    } } else {
                        world.notify(center.getBlockX(), center.getBlockY(), center.getBlockZ());
                    }
                }
            }
        }
    }
    
    public static void update(WorldServer world, int x, int y, int z) {
        Integer[][] arrays = newUpdateInt(x, y, z);
        for (Integer[] data : arrays) {
            world.notify(data[0], data[1], data[2]);
        }
    }

    private static Integer[][] newUpdateInt(int x, int y, int z) {
        return new Integer[][] { { x + 1, y - 1, z }, { x - 1, y - 1, z },
            { x, y - 1, z }, { x, y - 1, z + 1 }, { x, y - 1, z - 1 } };
    }
}