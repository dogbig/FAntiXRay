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

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import me.FurH.Core.reflection.ReflectionUtils;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.queue.FPacketQueue;
import me.FurH.FAntiXRay.queue.FPriorityQueue;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FBukkitHook extends FHookManager {
    
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void hook(Player p) {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        if (!FAntiXRay.isExempt(p.getName())) {
            EntityPlayer player = ((CraftPlayer)p).getHandle();

            List newhighPriorityQueue = Collections.synchronizedList(new FPriorityQueue(player));
            //List newlowPriorityQueue = Collections.synchronizedList(new FPriorityQueue(player));
            //Queue newinboundQueue = new FPacketQueue(player);

            List highPriorityQueue = (List) ReflectionUtils.getPrivateField(player.playerConnection.networkManager, "highPriorityQueue");
            //List lowPriorityQueue = (List) ReflectionUtils.getPrivateField(player.playerConnection.networkManager, "lowPriorityQueue");
            //Queue inboundQueue = (Queue) ReflectionUtils.getPrivateField(player.playerConnection.networkManager, "inboundQueue");

            if (highPriorityQueue != null) {
                newhighPriorityQueue.addAll(highPriorityQueue);
                highPriorityQueue.clear();
            }
            
            /*if (lowPriorityQueue != null) {
                newlowPriorityQueue.addAll(lowPriorityQueue);
                lowPriorityQueue.clear();
            }
            
            if (inboundQueue != null) {
                newinboundQueue.addAll(inboundQueue);
                inboundQueue.clear();
            }*/

            ReflectionUtils.setFinalField(player.playerConnection.networkManager, "highPriorityQueue", newhighPriorityQueue);
            //ReflectionUtils.setFinalField(player.playerConnection.networkManager, "lowPriorityQueue", newlowPriorityQueue);
            //ReflectionUtils.setFinalField(player.playerConnection.networkManager, "inboundQueue", newinboundQueue);
            
            startTask(p, config.proximity_interval);
        }
    }
}