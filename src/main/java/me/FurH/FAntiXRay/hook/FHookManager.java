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

import java.util.HashMap;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.obfuscation.FChestThread;
import me.FurH.FAntiXRay.update.FBlockUpdate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author FurmigaHumana
 */
public abstract class FHookManager {
    public HashMap<String, FChestThread> tasks = new HashMap<String, FChestThread>();
    public int taskId = -1;

    public void unhook(Player p) {
        stopTask(p);
    }
    
    public abstract void hook(Player p);

    public void startTask(Player p, int interval) {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        if (config.proximity_enabled || config.engine_chest) {
            stopTask(p);
            
            if (config.proximity_thread) {
                singleTask(interval);
                return;
            }

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
    
    public void singleTask(int interval) {
        if (taskId == -1) {
            taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(FAntiXRay.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    Player[] online = Bukkit.getOnlinePlayers();
                    for (int j = 0; j < online.length; j++) {
                        FBlockUpdate.update(online[j].getLocation(), false, true);
                    }
                }
            }, interval, interval).getTaskId();
        }
    }
}