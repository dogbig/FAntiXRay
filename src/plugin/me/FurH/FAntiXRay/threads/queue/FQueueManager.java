package me.FurH.FAntiXRay.threads.queue;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.threads.FPacketData;

/**
 *
 * @author FurmigaHumana
 */
public class FQueueManager {
    public ConcurrentLinkedQueue<FPacketData> queue = new ConcurrentLinkedQueue<>();
    public AtomicBoolean lock = new AtomicBoolean(false);

    public void start() {
        FConfiguration config = FAntiXRay.getConfiguration();

        for (int i = 0; i < config.thread_number; i++) {
            FQueueThread thread = new FQueueThread(this);
            thread.setName("FAntiXRay Queue Thread #"+(i+1));
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    public void stop() {
        lock.set(true);
    }

    public void add(FPacketData data) {
        queue.add(data);
    }
}
