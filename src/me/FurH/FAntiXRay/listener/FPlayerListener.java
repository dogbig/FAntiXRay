package me.FurH.FAntiXRay.listener;

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FMessages;
import me.FurH.FAntiXRay.hook.FNetServerHandler;
import me.FurH.FAntiXRay.util.FCommunicator;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
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
        
        if (!plugin.hasPerm(p, "Deobfuscate")) {
            CraftPlayer cp = (CraftPlayer)p;
            CraftServer s = (CraftServer)p.getServer();
            if (!(cp.getHandle().netServerHandler instanceof FNetServerHandler)) {
                FNetServerHandler handler = new FNetServerHandler(s.getServer(), cp.getHandle().netServerHandler);
                cp.getHandle().netServerHandler.networkManager.a(handler);
                cp.getHandle().netServerHandler = handler;
            }
        } else {
            com.msg(p, messages.deobfuscated);
        }
        
        if (plugin.hasUpdate) {
            if (plugin.hasPerm(p, "Updates")) {
                com.msg(p, messages.update1, plugin.newVersion, plugin.currentVersion);
                com.msg(p, messages.update2);
            }
        }
    }
}
