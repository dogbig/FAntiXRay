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
import me.FurH.Core.Core;
import me.FurH.Core.util.Communicator;
import me.FurH.Core.util.Utils;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.database.FSQLDatabase;
import org.bukkit.Bukkit;

/**
 *
 * @author FurmigaHumana
 */
public class FCacheManager {
    
    public static void getCacheSizeTask() {
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(FAntiXRay.getPlugin(), new Runnable() {
            @Override
            public void run() {
                Communicator com = FAntiXRay.getPlugin().getCommunicator();
                FConfiguration config = FAntiXRay.getConfiguration();
                
                long size = getCacheSize();
                long limit = config.cache_size;

                if (limit > 0 && size > limit) {
                    com.log("[TAG] The cache is too big, {0} of {1} allowed!", Utils.getFormatedBytes(size), Utils.getFormatedBytes((limit)));
                    clearCache();
                }
            }
        }, 20, 3600 * 20);
        
    }
    
    public static void clearCache() {
        
        final FSQLDatabase db = FAntiXRay.getSQLDatbase();
        
        if (Thread.currentThread() != Core.main_thread) {
            db.deleteAll();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(FAntiXRay.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    db.deleteAll();
                }
            });
        }
        
    }

    public static long getCacheSize() {
        return new File(FAntiXRay.getPlugin().getDataFolder(), "database.db").length();
    }
}