package me.FurH.FAntiXRay.hook.manager;

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.hook.FPlayerConnection;
import net.minecraft.server.v1_4_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_4_R1.CraftServer;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FHookStandart extends FHookManager {

    @Override
    public void hook(Player p) {
        if (!FAntiXRay.isExempt(p.getName())) {

            CraftPlayer cp = (CraftPlayer)p;
            CraftServer s = (CraftServer)p.getServer();
            PlayerConnection pl = cp.getHandle().playerConnection;

            if (!FAntiXRay.isProtocolEnabled()) {
                if (!(pl instanceof FPlayerConnection)) {
                    FPlayerConnection handler = new FPlayerConnection(s.getServer(), pl.networkManager, pl.player);
                    pl.networkManager.a(handler);
                    pl = handler;
                }
            }
        }
    }
}
