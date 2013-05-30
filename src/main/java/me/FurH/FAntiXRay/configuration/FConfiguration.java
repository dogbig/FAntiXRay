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

package me.FurH.FAntiXRay.configuration;

import java.util.HashSet;
import me.FurH.Core.CorePlugin;
import me.FurH.Core.configuration.Configuration;

/**
 *
 * @author FurmigaHumana
 */
public class FConfiguration extends Configuration {
    
    public FConfiguration(CorePlugin plugin) {
        super(plugin);
    }
    
    public int              engine_mode         = 0;
    public int              engine_nether       = 0;
    public boolean          engine_dark         = false;
    public boolean          engine_chest        = false;
    
    public boolean          cave_enabled        = false;
    public int              cave_intensity      = 10;
    
    public boolean          cache_enabled       = false;
    public int              cache_memory        = 300;
    public int              cache_callgc        = 100;
    public long             cache_size          = 5120;
    public int              cache_compression   = 1;
    
    public int              update_radius       = 1;
    
    public boolean          light_enabled       = false;
    public int              light_radius        = 8;
    public HashSet<Integer> light_blocks        = new HashSet<Integer>();
    
    public boolean          update_explosion    = false;
    public boolean          update_piston       = false;
    public boolean          update_physics      = false;
    
    public boolean          proximity_enabled   = false;
    public int              proximity_radius    = 10;
    public int              proximity_distance  = 1;

    public boolean          permission_ophas    = false;
    
    public boolean          updater_enabled     = true;
    
    public Integer[]        random_world        = new Integer[0];
    public Integer[]        random_nether       = new Integer[0];
    
    public HashSet<Integer> hidden_world        = new HashSet<Integer>();
    public HashSet<Integer> hidden_nether       = new HashSet<Integer>();
    
    public HashSet<String>  disabled_worlds     = new HashSet<String>();
    
    public void load() {
        engine_mode         = getInteger("EngineOptions.EngineMode");
        engine_nether       = getInteger("EngineOptions.NetherEngine");
        engine_dark         = getBoolean("EngineOptions.HideOnDark");
        engine_chest        = getBoolean("EngineOptions.HideChests");
        
        cave_enabled        = getBoolean("EngineOptions.FakeCaves.Enabled");
        cave_intensity      = getInteger("EngineOptions.FakeCaves.Intensity");
    
        cache_enabled       = getBoolean("Cache.Enabled");
        cache_memory        = getInteger("Cache.MemoryCache");
        cache_callgc        = getInteger("Cache.FileCallGC");
        cache_size          = getLong("Cache.SizeLimit") * 1024 * 1024;
        cache_compression   = getInteger("Cache.Compression");

        update_radius       = getInteger("BlockUpdate.UpdateRadius");
    
        light_enabled       = getBoolean("BlockUpdate.LightUpdate.Enabled");
        light_radius        = getInteger("BlockUpdate.LightUpdate.LightRadius");
        light_blocks        = getStringAsIntegerSet("BlockUpdate.LightUpdate.LightSource");
    
        update_explosion    = getBoolean("BlockUpdate.UpdateOn.BlockExplosion");
        update_piston       = getBoolean("BlockUpdate.UpdateOn.BlockPiston");
        update_physics      = getBoolean("BlockUpdate.UpdateOn.BlockPhysics");
    
        proximity_enabled   = getBoolean("BlockUpdate.Proximity.Enabled");
        proximity_radius    = getInteger("BlockUpdate.Proximity.Radius");
        proximity_distance  = getInteger("BlockUpdate.Proximity.Distance");
    
        permission_ophas    = getBoolean("Permissions.OpHasPerm");
    
        updater_enabled     = getBoolean("Updater.Enabled");
    
        random_world        = getStringAsIntegerSet("RandomBlocks.World").toArray(new Integer[] { });
        random_nether       = getStringAsIntegerSet("RandomBlocks.Nether").toArray(new Integer[] { });
    
        hidden_world        = getStringAsIntegerSet("HiddenBlocks.World");
        hidden_nether       = getStringAsIntegerSet("HiddenBlocks.Nether");

        disabled_worlds     = new HashSet<String>(getStringList("Worlds.Disabled"));

        updateConfig();
    }
}