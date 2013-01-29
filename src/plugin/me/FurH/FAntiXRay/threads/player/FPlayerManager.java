package me.FurH.FAntiXRay.threads.player;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import me.FurH.FAntiXRay.FAntiXRay;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.Packet;

/**
 *
 * @author FurmigaHumana
 */
public class FPlayerManager {
    public ConcurrentLinkedQueue<Packet> data = new ConcurrentLinkedQueue<>();
    public AtomicBoolean lock = new AtomicBoolean(false);
    public EntityPlayer player;
    
    public FPlayerManager(EntityPlayer player) {
        this.player = player;
    }

    public void start() {
        FPlayerThread thread = new FPlayerThread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setName("FAntiXRay Player Thread #" + (FAntiXRay.threads++));
        thread.start();
    }

    public void stop() {
        lock.set(true);
    }

    public void process(Packet packet) {
        data.add(packet);
    }
}