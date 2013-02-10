package me.FurH.FAntiXRay.queue;

import java.util.concurrent.ConcurrentLinkedQueue;
import me.FurH.FAntiXRay.update.FBlockUpdate;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.Packet;
import net.minecraft.server.v1_4_R1.Packet14BlockDig;
import net.minecraft.server.v1_4_R1.Packet15Place;

/**
 *
 * @author FurmigaHumana
 */
public class FPacketQueue extends ConcurrentLinkedQueue<Packet> {
    private static final long serialVersionUID = -15478984462251L;
    private EntityPlayer player;
    
    public FPacketQueue(EntityPlayer player) {
        this.player = player;
    }
    
    @Override
    public boolean add(Packet packet) {

        if (packet instanceof Packet14BlockDig) {
            FBlockUpdate.update(player, (Packet14BlockDig)packet);
        } else
        if (packet instanceof Packet15Place) {
            FBlockUpdate.update(player, (Packet15Place)packet);
        }
        
        return super.add(packet);
    }
    
    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }
    
    @Override
    public void clear() {
        super.clear();
    }
    
    @Override
    public Packet poll() {
        return super.poll();
    }
}