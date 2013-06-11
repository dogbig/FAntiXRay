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

import me.FurH.Core.cache.CoreSafeCache;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.util.Communicator;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.database.FSQLDatabase;

/**
 *
 * @author FurmigaHumana
 */
public class FChunkCache {

    public CoreSafeCache<String, FCacheData> cache = new CoreSafeCache<String, FCacheData>(3600);

    private int commit = 0;
        
    public void FChunkCache() {
        FConfiguration config = FAntiXRay.getConfiguration();
        cache = new CoreSafeCache<String, FCacheData>(config.cache_memory);
    }

    public void clear() {
        cache.clear();
    }

    public byte[] read(String world, long key, long hash, int engine_mode) {
        
        Communicator com = FAntiXRay.getPlugin().getCommunicator();
        FConfiguration config = FAntiXRay.getConfiguration();
        FSQLDatabase db = FAntiXRay.getSQLDatbase();
        
        try {
            
            String cacheKey = cacheKey(world, key);
            
            if (cache.containsKey(cacheKey)) {
                FCacheData data = cache.get(cacheKey);
                
                if (data.engine != engine_mode) {
                    return null;
                }
                
                if (data.hash != hash) {
                    cache.remove(cacheKey);
                    return null;
                }
                
                return data.inflatedBuffer;
            }
            
            if (commit > config.cache_callgc) {
                
                commit = 0;
                
                try {
                    db.commit();
                } catch (CoreException ex) {
                    com.error(ex);
                }
                
            }
            
            FCacheData data = db.getDataFrom(world, key, hash, engine_mode);
            
            if (data != null) {
                return data.inflatedBuffer;
            }
        
        } catch (Exception ex) {
            com.error(ex, "Failed to write world buffer for key: " + key + ", world: " + world + ", hash: " + hash);
        } finally {
            commit++;
        }
        
        return null;
    }

    public void write(String world, long key, byte[] inflatedBuffer, long hash, int engine_mode) {

        FSQLDatabase db = FAntiXRay.getSQLDatbase();
        FConfiguration config = FAntiXRay.getConfiguration();
        Communicator com = FAntiXRay.getPlugin().getCommunicator();
        
        try {
            
            FCacheData data = new FCacheData(world, key, inflatedBuffer, hash, engine_mode);
            String cacheKey = cacheKey(world, key);

            cache.put(cacheKey, data);
            
            if (commit > config.cache_callgc) {
                
                commit = 0;
                
                try {
                    db.commit();
                } catch (CoreException ex) {
                    com.error(ex);
                }
                
            }
            
            //
            
            db.setChunkData(world, key, hash, engine_mode, inflatedBuffer);
        
        } catch (Exception ex) {
            com.error(ex, "Failed to write world buffer for key: " + key + ", world: " + world + ", hash: " + hash);
        } finally {
            commit++;
        }
        
    }
    
    private String cacheKey(String world, long key) {
        return key + "" + world;
    }
}
