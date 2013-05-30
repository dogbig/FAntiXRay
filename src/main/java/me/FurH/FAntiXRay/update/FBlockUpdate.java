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

import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import me.FurH.FAntiXRay.threads.UpdateThreads.UpdateType;
import net.minecraft.server.v1_5_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FBlockUpdate implements Runnable {

    private UpdateType type;
    private Player player;
    private WorldServer world;
    private int x;
    private int y;
    private int z;
    private int radius;

    public FBlockUpdate(Player player, World world, int x, int y, int z, int radius, UpdateType type) {
        this.world = ((CraftWorld)world).getHandle();
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.type = type;
    }
    
    @Override
    public void run() {

        Location start = new Location(world.getWorld(), x, y, z);
        int notifications = 0;

        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                for (int c = -radius; c <= radius; c++) {

                    if (notifications > 63) {
                        break;
                    }

                    if (x == a && y == b && c == z) {
                        continue;
                    }

                    int i = a + x;
                    int j = b + y;
                    int k = c + z;

                    Location center = new Location(world.getWorld(), i, j, k);

                    if (center.distanceSquared(start) > radius) {
                        continue;
                    }

                    int id = world.getTypeId(i, j, k);
                    if (id < 1) {
                        continue;
                    }

                    if (id == 54 && player != null) {
                        player.sendBlockChange(center, id, (byte) world.getData(i, j, k)); notifications++; continue;
                    }
                    
                    if (type == UpdateType.PLAYER_TELEPORT) {
                        continue;
                    }

                    if (radius > 3 && !FObfuscator.isBlocksTransparent(world, i, j, k)) {
                        continue;
                    }

                    notifications++; world.notify(i, j, k);
                }
            }
        }
    }
}