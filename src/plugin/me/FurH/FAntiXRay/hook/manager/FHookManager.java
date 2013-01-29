package me.FurH.FAntiXRay.hook.manager;

import java.util.HashMap;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.obfuscation.FChestThread;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author FurmigaHumana
 */
public abstract class FHookManager {
    public HashMap<String, FChestThread> tasks = new HashMap<>();
    
    public abstract void hook(Player p);
    
    public void startThread(Player p) {
        if (FAntiXRay.getConfiguration().thread_player) {
            FAntiXRay.getThreadManager().startPlayer(((CraftPlayer)p).getHandle());
        }
    }

    public void startTask(Player p, int interval) {
        if (FObfuscator.chest_enabled && !FAntiXRay.getConfiguration().chest_movement) {
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
