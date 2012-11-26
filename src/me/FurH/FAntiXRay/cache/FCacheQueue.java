/*
 * Copyright (C) 2011-2012 FurmigaHumana.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.FurH.FAntiXRay.cache;

import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import me.FurH.FAntiXRay.FAntiXRay;
import net.minecraft.server.World;
import org.bukkit.Bukkit;

/**
 *
 * @author FurmigaHumana
 */
public class FCacheQueue {
    private static final Queue<FCacheWriteData> queue = new LinkedBlockingQueue();
    private static final HashSet<String> queued = new HashSet<> ();
    private static final Lock lock = new ReentrantLock();
    private static int queueid = 0;

    public static void onQueue(World world, int x, int z, byte[] obfuscated, long hash, int engine) {
        if (!queued.contains(x + ":" + z)) {
            queued.add(x + ":" + z);
            queue.add(new FCacheWriteData(world, x, z, obfuscated, hash, engine));
        }
    }
    
    public static void saveQueue() {
        Bukkit.getScheduler().cancelTask(queueid);
        
        final FChunkCache cache = FAntiXRay.getCache();
        while ((!queue.isEmpty())) {
            FCacheWriteData data = queue.poll();
            if (data == null) { continue; }
            cache.write(data.world, data.x, data.z, data.obfuscated, data.hash, data.engine);
        }
    }
    
    public static void queue() {
        final FChunkCache cache = FAntiXRay.getCache();
        
        queueid = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(FAntiXRay.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (queue.isEmpty() || !lock.tryLock()) { return; }
                int count = 0;
                while ((!queue.isEmpty()) && (count < FAntiXRay.getConfiguration().writes_sec)) {
                    FCacheWriteData data = queue.poll();
                    if (data == null) { continue; }

                    int x = data.x;
                    int z = data.z;

                    queued.remove(x + ":" + z);

                    cache.write(data.world, x, z, data.obfuscated, data.hash, data.engine);

                    count++;
                }
                lock.unlock();
            }
        }, 5 * 20, 5 * 20);
    }
    
    public static class FCacheWriteData {
        public World world;
        public int x;
        public int z;
        public byte[] obfuscated;
        public long hash;
        public int engine;
        
        public FCacheWriteData(World world, int x, int z, byte[] obfuscated, long hash, int engine) {
            this.world = world;
            this.x = x;
            this.z = z;
            this.obfuscated = obfuscated;
            this.hash = hash;
            this.engine = engine;
        }
    }
}
