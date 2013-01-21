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
import me.FurH.FAntiXRay.util.FCommunicator;
import me.FurH.server.FAntiXRay.hooks.FPlayerConnection;
import org.bukkit.craftbukkit.v1_4_R1.CraftServer;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author FurmigaHumana
 */
public class FPlayerListener implements Listener  {
    
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
        
        if (FAntiXRay.getProtocol() == null) {
            if (!FAntiXRay.isExempt(p.getName())) {
                CraftPlayer cp = (CraftPlayer)p;
                CraftServer s = (CraftServer)p.getServer();
                if (!(cp.getHandle().playerConnection instanceof FPlayerConnection)) {
                    FPlayerConnection handler = new FPlayerConnection(s.getServer(), cp.getHandle().playerConnection.networkManager, cp.getHandle().playerConnection.player);
                    cp.getHandle().playerConnection.networkManager.a(handler);
                    cp.getHandle().playerConnection = handler;
                }
            }
        }
        
        if (plugin.hasUpdate) {
            if (plugin.hasPerm(p, "Updates")) {
                com.msg(p, messages.update1, plugin.newVersion, plugin.currentVersion);
                com.msg(p, messages.update2);
            }
        }
    }
}
