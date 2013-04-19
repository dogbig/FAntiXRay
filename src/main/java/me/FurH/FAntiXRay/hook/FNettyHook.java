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

package me.FurH.FAntiXRay.hook;

import java.util.Queue;
import me.FurH.Core.reflection.ReflectionUtils;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.queue.FPacketQueue;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.Packet;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FNettyHook extends FHookManager {

    @Override
    public void hook(Player p) {
        if (!FAntiXRay.isExempt(p.getName())) {
            EntityPlayer player = ((CraftPlayer)p).getHandle();

            io.netty.channel.Channel channel = (io.netty.channel.Channel) ReflectionUtils.getPrivateField(player.playerConnection.networkManager, "channel");
            channel.pipeline().remove("encoder");
            channel.pipeline().addLast("encoder", new FPacketEncoder(player));

            /*Queue<Packet> syncPackets = (Queue<Packet>) ReflectionUtils.getPrivateField(player.playerConnection.networkManager, "syncPackets");

            FPacketQueue newSyncPackets = new FPacketQueue(player);
            newSyncPackets.addAll(syncPackets);

            ReflectionUtils.setFinalField(player.playerConnection.networkManager, "syncPackets", newSyncPackets);*/
            
            startTask(p, FAntiXRay.getConfiguration().proximity_interval);
        }
    }
}