package me.FurH.FAntiXRay.threads.player;

import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import net.minecraft.server.v1_4_R1.Packet;
import net.minecraft.server.v1_4_R1.Packet51MapChunk;
import net.minecraft.server.v1_4_R1.Packet56MapChunkBulk;

/**
 *
 * @author FurmigaHumana
 */
public class FPlayerThread extends Thread {
    private FPlayerManager manager;
    
    public FPlayerThread(FPlayerManager manager) {
        this.manager = manager;
    }
    
    @Override
    public void run() {
        try {
            while (!manager.lock.get() && !manager.player.playerConnection.disconnected) {
                Packet packet = manager.data.poll();
                if (packet == null) {
                    sleep(500);
                } else {

                    if (packet instanceof Packet51MapChunk) {
                        FObfuscator.obfuscate(manager.player, (Packet51MapChunk) packet, true);
                    }
                    if (packet instanceof Packet56MapChunkBulk) {
                        FObfuscator.obfuscate(manager.player, (Packet56MapChunkBulk) packet, true);
                    }

                    packet = null;
                }
            }

            manager.data.clear(); interrupt();
        } catch (Exception ex) { }
    }
}
