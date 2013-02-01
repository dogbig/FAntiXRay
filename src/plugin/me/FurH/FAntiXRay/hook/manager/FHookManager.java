package me.FurH.FAntiXRay.hook.manager;

import java.util.HashMap;
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
    public HashMap<String, FChestThread> tasks = new HashMap<>();

    public void hook(Player p) {
        if (!FAntiXRay.isExempt(p.getName())) {
            EntityPlayer player = ((CraftPlayer) p).getHandle();

            FReflectField.setFinalField(player.playerConnection.networkManager, "highPriorityQueue", new FPriorityQueue(player));
            FReflectField.setFinalField(player.playerConnection.networkManager, "lowPriorityQueue", new FPriorityQueue(player));
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
