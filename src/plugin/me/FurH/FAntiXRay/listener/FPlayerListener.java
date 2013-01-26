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

package me.FurH.FAntiXRay.listener;

import java.util.HashMap;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FMessages;
import me.FurH.FAntiXRay.hook.FObfuscatorHook;
import me.FurH.FAntiXRay.hook.FPlayerConnection;
import me.FurH.FAntiXRay.obfuscation.FChestThread;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import me.FurH.FAntiXRay.util.FCommunicator;
import net.minecraft.server.v1_4_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_4_R1.CraftServer;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author FurmigaHumana
 */
public class FPlayerListener implements Listener  {
    public static HashMap<String, FChestThread> tasks = new HashMap<>();
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) { return; }
        
        if (tasks.containsKey(e.getPlayer().getName())) {
            tasks.get(e.getPlayer().getName()).update();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        stopTask(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerKickEvent e) {
        stopTask(e.getPlayer());
    }
    
    private static void stopTask(Player p) {
        if (tasks.containsKey(p.getName())) {
            Bukkit.getScheduler().cancelTask(tasks.get(p.getName()).getId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        FCommunicator com = FAntiXRay.getCommunicator();
        FMessages messages = FAntiXRay.getMessages();
        FAntiXRay plugin = FAntiXRay.getPlugin();

        if (plugin.hasPerm(p, "Deobfuscate")) {
            com.msg(p, messages.deobfuscated);
            FAntiXRay.exempt(p.getName());
        }

        if (plugin.hasPerm(p, "Quiet.Deobfuscate")) {
            FAntiXRay.exempt(p.getName());
        }

        hookPlayer(p);
        
        if (plugin.hasUpdate) {
            if (plugin.hasPerm(p, "Updates")) {
                com.msg(p, messages.update1, plugin.newVersion, plugin.currentVersion);
                com.msg(p, messages.update2);
            }
        }
    }

    public static void hookPlayer(Player p) {
        if (!FAntiXRay.isExempt(p.getName())) {
            
            CraftPlayer cp = (CraftPlayer)p;
            if (FObfuscator.server_mode) {
                PlayerConnection pl = cp.getHandle().playerConnection;
                FObfuscatorHook hook = new FObfuscatorHook();
                pl.networkManager.a(hook);
            } else {
                CraftServer s = (CraftServer)p.getServer();
                if (!(cp.getHandle().playerConnection instanceof FPlayerConnection)) {
                    FPlayerConnection handler = new FPlayerConnection(s.getServer(), cp.getHandle().playerConnection.networkManager, cp.getHandle().playerConnection.player);
                    cp.getHandle().playerConnection.networkManager.a(handler);
                    cp.getHandle().playerConnection = handler;
                }
            }

            startTask(p, FAntiXRay.getConfiguration().chest_interval);
        }
    }

    private static void startTask(Player p, int interval) {
        if (FObfuscator.chest_enabled) {
            stopTask(p);

            FChestThread thread = new FChestThread(p);

            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(FAntiXRay.getPlugin(), thread, interval, interval);
            thread.setId(task.getTaskId());

            tasks.put(p.getName(), thread);
        }
    }
}
