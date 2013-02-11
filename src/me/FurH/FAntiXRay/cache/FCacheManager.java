package me.FurH.FAntiXRay.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.util.FUtils;
import org.bukkit.Bukkit;

/**
 *
 * @author FurmigaHumana
 */
public class FCacheManager {
    public static List<File> directories = new ArrayList<File>();
    public static List<File> files = new ArrayList<File>();

    public static void getCacheSizeTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(FAntiXRay.getPlugin(), new Runnable() {
            @Override
            public void run() {
                double size = getCacheSize();
                double limit = FAntiXRay.getConfiguration().size_limit;
                
                if (size > limit) {
                    FAntiXRay.getCommunicator().log("[TAG] The cache is too big, {0} of {1} allowed in {2} files, cleaning up!", FUtils.format(size), FUtils.format((limit)), files.size());
                    clearCache();
                }
            }
        }, 3600 * 20, 3600 * 20);
    }

    public static int clearCache() {
        int total = 0;
        
        files.clear();
        directories.clear();
        
        for (File dir : getCacheDirectories()) {
            for (File file : getCacheFiles(dir)) {
                if (!file.delete()) {
                    total++;
                }
            }
        }
        
        for (File dirs : directories) {
            if (!dirs.delete()) {
                total++;
            }
        }
        
        return total;
    }
    
    private static List<File> getCacheFiles(File directory) {

        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                directories.add(file);
                getCacheFiles(file);
            }
        }

        return files;
    }

    private static List<File> getCacheDirectories() {
        List<File> dirs = new ArrayList<File>();

        files.clear();
        directories.clear();

        for (File file : FAntiXRay.getPlugin().getDataFolder().listFiles()) {
            if (file.isDirectory()) {
                dirs.add(file);
            }
        }
        
        return dirs;
    }

    public static double getCacheSize() {
        double total = 0;
        
        files.clear();
        directories.clear();
        
        for (File dir : getCacheDirectories()) {
            for (File file : getCacheFiles(dir)) {
                total += file.length();
            }
        }

        return total;
    }
}