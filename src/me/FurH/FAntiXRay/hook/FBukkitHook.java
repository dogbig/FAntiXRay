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
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.queue.FPacketQueue;
import me.FurH.FAntiXRay.queue.FPriorityQueue;
import me.FurH.FAntiXRay.util.FReflectField;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FBukkitHook extends FHookManager {
    
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void hook(Player p) {
        if (!FAntiXRay.isExempt(p.getName())) {
            final EntityPlayer player = ((CraftPlayer)p).getHandle();
            
            startTask(p, FAntiXRay.getConfiguration().chest_interval);

            List newhighPriorityQueue = Collections.synchronizedList(new FPriorityQueue(player));
            List newlowPriorityQueue = Collections.synchronizedList(new FPriorityQueue(player));
            
            Queue newinboundQueue = new FPacketQueue(player);

            List highPriorityQueue = (List) FReflectField.getPrivateField(player.playerConnection.networkManager, "highPriorityQueue");
            List lowPriorityQueue = (List) FReflectField.getPrivateField(player.playerConnection.networkManager, "lowPriorityQueue");
            Queue inboundQueue = (Queue) FReflectField.getPrivateField(player.playerConnection.networkManager, "inboundQueue");

            if (highPriorityQueue != null) {
                if (highPriorityQueue instanceof FPriorityQueue) {
                    return;
                }

                newhighPriorityQueue.addAll(highPriorityQueue);
                
                highPriorityQueue.clear();
            }
            
            if (lowPriorityQueue != null) {
                if (lowPriorityQueue instanceof FPriorityQueue) {
                    return;
                }

                newlowPriorityQueue.addAll(lowPriorityQueue);
                
                lowPriorityQueue.clear();
            }
            
            if (inboundQueue != null) {
                if (inboundQueue instanceof FPacketQueue) {
                    return;
                }
                
                newinboundQueue.addAll(inboundQueue);
                
                inboundQueue.clear();
            }

            FReflectField.setFinalField(player.playerConnection.networkManager, "highPriorityQueue", newhighPriorityQueue);
            FReflectField.setFinalField(player.playerConnection.networkManager, "lowPriorityQueue", newlowPriorityQueue);
            FReflectField.setFinalField(player.playerConnection.networkManager, "inboundQueue", newinboundQueue);
        }
    }
}