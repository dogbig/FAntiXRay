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

package me.FurH.FAntiXRay.hook;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import me.FurH.FAntiXRay.FAntiXRay;
import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.Packet56MapChunkBulk;
import org.bukkit.Bukkit;

/**
 *
 * @author FurmigaHumana
 */
public class FChunkRWork {
    public static Queue<FChunkData> queue = new LinkedBlockingQueue<>();

    public static class FChunkData {
        public Packet56MapChunkBulk packet;
        public EntityPlayer player;

        public FChunkData(Packet56MapChunkBulk packet, EntityPlayer player) {
            this.packet = packet;
            this.player = player;
        }
    }

    public static void toReloadW() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(FAntiXRay.getPlugin(), new Runnable() {
            @Override
            public void run() {
                FChunkData c = queue.poll();
                if (c != null) {
                    c.player.playerConnection.sendPacket(c.packet);
                }
            }
        }, 1L, 1L);
    }
}
