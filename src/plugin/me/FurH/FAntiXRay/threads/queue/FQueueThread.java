package me.FurH.FAntiXRay.threads.queue;

import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import me.FurH.FAntiXRay.threads.FPacketData;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.Packet;
import net.minecraft.server.v1_4_R1.Packet51MapChunk;
import net.minecraft.server.v1_4_R1.Packet56MapChunkBulk;

/**
 *
 * @author FurmigaHumana
 */
public class FQueueThread extends Thread {
    public FQueueManager manager;

    public FQueueThread(FQueueManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        
        while (!manager.lock.get()) {
            FPacketData data = manager.queue.poll();

            if (data == null) {
                try {
                    sleep(500);
                } catch (InterruptedException ex) { }
            } else {

                while (data.player.playerConnection.disconnected) {
                    data = manager.queue.poll();
                }
                
                if (data == null) {
                    try {
                        sleep(500);
                    } catch (InterruptedException ex) { }
                }
                
                int size = manager.queue.size();

                Packet packet = data.packet;
                EntityPlayer player = data.player;

                if (packet instanceof Packet51MapChunk) {
                    FObfuscator.obfuscate(player, (Packet51MapChunk) packet, true);
                }
                if (packet instanceof Packet56MapChunkBulk) {
                    FObfuscator.obfuscate(player, (Packet56MapChunkBulk) packet, true);
                }

                player = null;
                packet = null;
            }
        }
        
        interrupt();
    }
}
