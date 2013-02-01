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

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FMessages;
import me.FurH.FAntiXRay.hook.FHookManager;
import me.FurH.FAntiXRay.obfuscation.FChestThread;
import me.FurH.FAntiXRay.util.FCommunicator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 *
 * @author FurmigaHumana
 */
public class FPlayerListener implements Listener  {
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        if (e.isCancelled()) { return; }
        
        FHookManager hook = FAntiXRay.getHookManager();
        if (hook.tasks.containsKey(e.getPlayer().getName())) {
            FChestThread thread = hook.tasks.get(e.getPlayer().getName());
            thread.setLastLoc(e.getFrom());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        FAntiXRay.getHookManager().stopTask(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerKickEvent e) {
        FAntiXRay.getHookManager().stopTask(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
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
        
        FAntiXRay.getHookManager().hook(p);
        if (!FAntiXRay.isExempt(p.getName())) {
            FAntiXRay.getHookManager().startTask(p, FAntiXRay.getConfiguration().chest_interval);
        }
        
        if (plugin.hasUpdate) {
            if (plugin.hasPerm(p, "Updates")) {
                com.msg(p, messages.update1, plugin.newVersion, plugin.currentVersion);
                com.msg(p, messages.update2);
            }
        }
    }
}
