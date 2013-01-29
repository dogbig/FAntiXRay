package me.FurH.FAntiXRay.threads;

import java.util.HashMap;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.threads.player.FPlayerManager;
import me.FurH.FAntiXRay.threads.queue.FQueueManager;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FThreadManager {
    public HashMap<String, FPlayerManager> threads = new HashMap<>();
    public FQueueManager queueManager;
    
    public FThreadManager() {
        queueManager = new FQueueManager();
    }
    
    public void add(FPacketData data) {
        if (FAntiXRay.getConfiguration().thread_enabled) {
            queueManager.add(data);
        } else
        if (FAntiXRay.getConfiguration().thread_player) {
            FPlayerManager manager = threads.get(data.player.name);
            if (threads.get(data.player.name) == null) {
                manager = new FPlayerManager(data.player);
                manager.data.add(data.packet);
                manager.start();
            } else {
                manager.data.add(data.packet);
            }
        }
    }
    
    public void start() {
        queueManager.start();
    }
    
    public void startPlayer(EntityPlayer player) {
        FPlayerManager manager = new FPlayerManager(player);
        manager.start();
        threads.put(player.name, manager);
    }
    
    public void stopPlayer(Player p) {
        FPlayerManager manager = threads.get(p.getName());
        if (manager != null) {
            manager.stop();
        }
    }
    
    public void stopQueue() {
        queueManager.stop();
    }

    public void stopAll() {
        for (String name : threads.keySet()) {
            FPlayerManager manager = threads.get(name);
            manager.stop();
        }
        queueManager.stop();
    }
}
