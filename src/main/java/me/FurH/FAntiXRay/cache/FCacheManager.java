/*
 * Copyright (C) 2011-2013 FurmigaHumana.  All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import me.FurH.Core.util.Communicator;
import me.FurH.Core.util.Utils;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
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
                Communicator com = FAntiXRay.getPlugin().getCommunicator();
                FConfiguration config = FAntiXRay.getConfiguration();
                
                long size = getCacheSize();
                long limit = config.cache_size;

                if (limit > 0 && size > limit) {
                    com.log("[TAG] The cache is too big, {0} of {1} allowed in {2} files, cleaning up!", Utils.getFormatedBytes(size), Utils.getFormatedBytes((limit)), files.size());
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

    public static long getCacheSize() {
        long total = 0;
        
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