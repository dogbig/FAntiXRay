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
import java.util.HashMap;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.obfuscation.FChestThread;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import me.FurH.FAntiXRay.queue.FPriorityQueue;
import me.FurH.FAntiXRay.util.FReflectField;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author FurmigaHumana
 */
public class FHookManager {
    public HashMap<String, FChestThread> tasks = new HashMap<String, FChestThread>();

    public void unhook(Player p) {
        stopTask(p);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void hook(Player p) {
        if (!FAntiXRay.isExempt(p.getName())) {
            final EntityPlayer player = ((CraftPlayer)p).getHandle();
            
            startTask(p, FAntiXRay.getConfiguration().chest_interval);

            boolean natty = false;
            try {
                org.spigotmc.netty.NettyNetworkManager nattyManager = null;
                natty = true;
            } catch (NoClassDefFoundError ex) {
                natty = false;
            }

            if (natty) {
                io.netty.channel.Channel channel = (io.netty.channel.Channel) FReflectField.getPrivateField(player.playerConnection.networkManager, "channel");
                channel.pipeline().remove("encoder");
                channel.pipeline().addLast("encoder", new FPacketEncoder(player));
                return;
            }

            List newhighPriorityQueue = Collections.synchronizedList(new FPriorityQueue(player));
            List newlowPriorityQueue = Collections.synchronizedList(new FPriorityQueue(player));

            List highPriorityQueue = (List) FReflectField.getPrivateField(player.playerConnection.networkManager, "highPriorityQueue");
            List lowPriorityQueue = (List) FReflectField.getPrivateField(player.playerConnection.networkManager, "lowPriorityQueue");

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

            FReflectField.setFinalField(player.playerConnection.networkManager, "highPriorityQueue", newhighPriorityQueue);
            FReflectField.setFinalField(player.playerConnection.networkManager, "lowPriorityQueue", newlowPriorityQueue);
        }
    }

    public void startTask(Player p, int interval) {
        if (FObfuscator.chest_enabled) {
            stopTask(p);

            FChestThread thread = new FChestThread(p);

            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(FAntiXRay.getPlugin(), thread, interval, interval);
            thread.setId(task.getTaskId());

            tasks.put(p.getName(), thread);
        }
    }

    public void stopTask(Player p) {
        if (tasks.containsKey(p.getName())) {
            Bukkit.getScheduler().cancelTask(tasks.get(p.getName()).getId());
        }
    }
}