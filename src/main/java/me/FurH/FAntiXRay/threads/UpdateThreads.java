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

        int executors = 1;
        
        switch (type) {
            case BLOCK_PLACE:
                
                executors = (int) Math.max(Math.floor(
                        Bukkit.getOnlinePlayers().length * placePorcentage), 1);

                if (placeThread.getMaximumPoolSize() != executors) {
                    placeThread.setMaximumPoolSize(executors);
                }
                
                return placeThread;
            case BLOCK_BREAK:
                
                executors = (int) Math.max(Math.floor(
                        Bukkit.getOnlinePlayers().length * breakPorcentage), 1);

                if (breakThread.getMaximumPoolSize() != executors) {
                    breakThread.setMaximumPoolSize(executors);
                }
                
                return breakThread;
            case PLAYER_MOVE:
                
                executors = (int) Math.max(Math.floor(
                        Bukkit.getOnlinePlayers().length * movesPorcentage), 1);

                if (movesThread.getMaximumPoolSize() != executors) {
                    movesThread.setMaximumPoolSize(executors);
                }
                
                return movesThread;
            case PLAYER_TELEPORT:
                
                executors = (int) Math.max(Math.floor(
                        Bukkit.getOnlinePlayers().length * movesPorcentage), 1);

                if (movesThread.getMaximumPoolSize() != executors) {
                    movesThread.setMaximumPoolSize(executors);
                }
                
                return movesThread;
            case PLAYER_INTERACT:
                
                executors = (int) Math.max(Math.floor(
                        Bukkit.getOnlinePlayers().length * placePorcentage), 1);

                if (placeThread.getMaximumPoolSize() != executors) {
                    placeThread.setMaximumPoolSize(executors);
                }
                
                return placeThread;
            case BLOCK_PISTON:
                
                executors = (int) Math.max(Math.floor(
                        Bukkit.getOnlinePlayers().length * miscsPorcentage), 1);

                if (miscsThread.getMaximumPoolSize() != executors) {
                    miscsThread.setMaximumPoolSize(executors);
                }
                
                return miscsThread;
            case BLOCK_EXPLOSION:
                
                executors = (int) Math.max(Math.floor(
                        Bukkit.getOnlinePlayers().length * miscsPorcentage), 1);

                if (miscsThread.getMaximumPoolSize() != executors) {
                    miscsThread.setMaximumPoolSize(executors);
                }
                
                return miscsThread;
            default:
                
                executors = (int) Math.max(Math.floor(
                        Bukkit.getOnlinePlayers().length * miscsPorcentage), 1);

                if (miscsThread.getMaximumPoolSize() != executors) {
                    miscsThread.setMaximumPoolSize(executors);
                }
                
                return miscsThread;
        }
    }
}
