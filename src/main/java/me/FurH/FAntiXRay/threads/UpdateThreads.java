package me.FurH.FAntiXRay.threads;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import me.FurH.FAntiXRay.update.FBlockUpdate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class UpdateThreads {
    
    public static enum UpdateType { BLOCK_PLACE, BLOCK_BREAK, PLAYER_MOVE, PLAYER_TELEPORT, PLAYER_INTERACT, BLOCK_PISTON, BLOCK_EXPLOSION; }

    private static ThreadPoolExecutor placeThread;
    private static int placeCounter = 1;
    private static double placePorcentage = 0.02; // 2 threads per 100 players
    
    private static ThreadPoolExecutor breakThread;
    private static int breakCounter = 1;
    private static double breakPorcentage = 0.05; // 5 threads per 100 players
    
    private static ThreadPoolExecutor movesThread;
    private static int movesCounter = 1;
    private static double movesPorcentage = 0.02; // 2 threads per 100 players
    
    private static ThreadPoolExecutor miscsThread;
    private static int miscsCounter = 1;
    private static double miscsPorcentage = 0.01; // 1 thread per 100 players
    
    public static void update(Player p, Location loc, int radius, UpdateType type) {
        getExecutor(type).execute(new FBlockUpdate(p, loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), radius, type));
    }

    public static void update(Location loc, int radius, UpdateType type) {
        update(null, loc, radius, type);
    }

    public static void setup() {

        placeThread = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);

                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setName("FAntiXRay Light Thread #" + placeCounter);
                placeCounter++;

                return thread;
            }
        });
        
        breakThread = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                
                thread.setDaemon(true);
                thread.setPriority(Thread.MAX_PRIORITY);
                thread.setName("FAntiXRay Break Thread #" + breakCounter);
                breakCounter++;

                return thread;
            }
        });
        
        movesThread = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setName("FAntiXRay Proximity Thread #" + movesCounter);
                movesCounter++;

                return thread;
            }
        });
        
        miscsThread = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                
                thread.setDaemon(true);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setName("FAntiXRay Misc Thread #" + miscsCounter);
                miscsCounter++;

                return thread;
            }
        });
    }
    
    private static ThreadPoolExecutor getExecutor(UpdateType type) {
        
        switch (type) {
            case BLOCK_PLACE:
                return placeThread;
            case BLOCK_BREAK:
                return breakThread;
            case PLAYER_MOVE:
                return movesThread;
            case PLAYER_TELEPORT:
                return movesThread;
            case PLAYER_INTERACT:
                return placeThread;
            case BLOCK_PISTON:
                return miscsThread;
            case BLOCK_EXPLOSION:
                return miscsThread;
            default:
                return miscsThread;
        }
        
    }
    
    public static void updatePools() {

        int max = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        int online = Bukkit.getOnlinePlayers().length;

        int executors = 1;

        executors = (int) Math.min(Math.max(Math.floor(
                online * placePorcentage), 1), max);

        if (placeThread.getCorePoolSize() != executors) {
            placeThread.setCorePoolSize(executors);
        }

        executors = (int) Math.min(Math.max(Math.floor(
                online * breakPorcentage), 1), max);

        if (breakThread.getCorePoolSize() != executors) {
            breakThread.setCorePoolSize(executors);
        }

        executors = (int) Math.min(Math.max(Math.floor(
                online * movesPorcentage), 1), max);

        if (movesThread.getCorePoolSize() != executors) {
            movesThread.setCorePoolSize(executors);
        }

        executors = (int) Math.min(Math.max(Math.floor(
                online * movesPorcentage), 1), max);

        if (movesThread.getCorePoolSize() != executors) {
            movesThread.setCorePoolSize(executors);
        }

        executors = (int) Math.min(Math.max(Math.floor(
                online * placePorcentage), 1), max);

        if (placeThread.getCorePoolSize() != executors) {
            placeThread.setCorePoolSize(executors);
        }

        executors = (int) Math.min(Math.max(Math.floor(
                online * miscsPorcentage), 1), max);

        if (miscsThread.getCorePoolSize() != executors) {
            miscsThread.setCorePoolSize(executors);
        }

        executors = (int) Math.min(Math.max(Math.floor(
                online * miscsPorcentage), 1), max);

        if (miscsThread.getCorePoolSize() != executors) {
            miscsThread.setCorePoolSize(executors);
        }

        executors = (int) Math.min(Math.max(Math.floor(
                online * miscsPorcentage), 1), max);

        if (miscsThread.getCorePoolSize() != executors) {
            miscsThread.setCorePoolSize(executors);
        }
        
        ObfuscationThreads.updatePools();
    }
}
