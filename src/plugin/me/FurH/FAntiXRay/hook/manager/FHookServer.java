package me.FurH.FAntiXRay.hook.manager;

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.hook.FObfuscatorHook;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import net.minecraft.server.v1_4_R1.PlayerConnection;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FHookServer extends FHookManager {

    @Override
    public void hook(Player p) {
        if (!FAntiXRay.isExempt(p.getName())) {

            CraftPlayer cp = (CraftPlayer)p;
            PlayerConnection pl = cp.getHandle().playerConnection;
            
            if (FObfuscator.server_mode) {
                FObfuscatorHook hook = new FObfuscatorHook();
                pl.networkManager.a(hook);
            }

            startTask(p, FAntiXRay.getConfiguration().chest_interval);
        }
    }
}
