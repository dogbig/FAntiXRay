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

package me.FurH.FAntiXRay.listener;

import me.FurH.Core.util.Communicator;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import me.FurH.FAntiXRay.threads.UpdateThreads;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

/**
 *
 * @author FurmigaHumana
 */
public class FPlayerListener implements Listener  {
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent e) {
        FAntiXRay.getSQLDatbase().load(e.getWorld());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldInit(WorldInitEvent e) {
        FAntiXRay.getSQLDatbase().load(e.getWorld());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        FAntiXRay plugin = FAntiXRay.getPlugin();
        Communicator com = plugin.getCommunicator();

        if (plugin.hasPerm(p, "FAntiXRay.Deobfuscate")) {

            com.msg(p, "&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=\n"
                    + "&7=&8-&3YOU HAVE PERMISSION TO BYPASS THE FANTIXRAY &8-&7=\n"
                    + "&7=&8-           &3THE PLUGIN IS STILL WORKING              &8-&7=\n"
                    + "&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=&8-&7=");

            FAntiXRay.exempt(p.getName());
        }

        if (plugin.hasPerm(p, "FAntiXRay.Quiet.Deobfuscate")) {
            FAntiXRay.exempt(p.getName());
        }

        if (plugin.updater.isUpdateAvailable()) {
            if (plugin.hasPerm(p, "FAntiXRay.Updates")) {
                plugin.updater.announce(p);
            }
        }

        UpdateThreads.updatePools();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent e) {
        if (e.getResult() == Result.ALLOWED) {
            FObfuscator.teleport(e.getPlayer(), Bukkit.getViewDistance());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        UpdateThreads.updatePools(); FObfuscator.unload_player(e.getPlayer());
    }
}