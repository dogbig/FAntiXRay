package me.FurH.FAntiXRay.queue;

import java.util.ArrayList;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.Packet;
import net.minecraft.server.v1_4_R1.Packet51MapChunk;
import net.minecraft.server.v1_4_R1.Packet56MapChunkBulk;

public class FPriorityQueue extends ArrayList<Packet> {
    private static final long serialVersionUID = 1L;

    private EntityPlayer player;
    public FPriorityQueue(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public boolean add(Packet packet) {
        return super.add(packet);
    }
    
    @Override
    public Packet remove(int index) {
        
        Packet packet = super.remove(index);
        if (packet != null) {
            if (packet instanceof Packet56MapChunkBulk) {
                Packet56MapChunkBulk p56 = (Packet56MapChunkBulk)packet;
                packet = FObfuscator.obfuscate(player, p56, false);
                System.out.println("Process... " + Thread.currentThread().getName());
            } else
            if (packet instanceof Packet51MapChunk) {
                Packet51MapChunk p51 = (Packet51MapChunk)packet;
                packet = FObfuscator.obfuscate(player, p51, false);
            }
        }
        
        return packet;
    }
    
    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }
    
    @Override
    public Packet get(int index) {
        return super.get(index);
    }
}