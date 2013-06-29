package me.FurH.FAntiXRay.threads;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class ObfuscationThreads {
    
    private static ThreadPoolExecutor thread;
    private static int counter = 1;
    private static double porcentage = 0.1; // 10 threads per 100 players
    
    public static void setup() {

        thread = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);

                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setName("FAntiXRay Obfuscation Thread #" + counter);
                counter++;

                return thread;
            }
        });

    }

    public static ThreadPoolExecutor getExecutor() {
        return thread;
    }
    
    public static void updatePools() {
        
        int max = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        int online = Bukkit.getOnlinePlayers().length;

        int executors = (int) Math.min(Math.max(Math.floor(
                online * porcentage), 1), max);

        if (thread.getCorePoolSize() != executors) {
            thread.setCorePoolSize(executors);
        }

        thread.prestartAllCoreThreads();
    }
}
