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

package me.FurH.FAntiXRay.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.util.FCommunicator;
import me.FurH.FAntiXRay.util.FCommunicator.Type;
import me.FurH.FAntiXRay.util.FUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author FurmigaHumana
 */
public class FConfiguration {
    public int              engine_mode     = 1;
    public int              update_radius   = 1;
    
    /*public boolean          dark_enabled    = false;
    public boolean          dark_only       = true;
    public int              dark_radius     = 8;
    public HashSet<Integer> dark_blocks     = new HashSet<>();*/

    public boolean          block_place     = false;
    public boolean          block_explosion = false;
    public boolean          block_damage    = false;
    public boolean          block_piston    = false;
    public boolean          block_physics   = false;
    
    public int              call_gc         = 100;
    public boolean          enable_cache    = true;
    public int              size_limit      = 1024;
    public int              writes_sec      = 10;
    public int              compress_level  = 1;

    public boolean          ophasperm       = true;

    public boolean          updates         = true;

    public Integer[]        random_blocks   = new Integer[] { };
    public HashSet<Integer> hidden_blocks   = new HashSet<>();

    public HashSet<String>  disabled_worlds = new HashSet<>();

    public void load() {
        FCommunicator com    = FAntiXRay.getCommunicator();
        engine_mode     = getInteger("Options.EngineMode");
        if (engine_mode > 4) {
            com.log("[TAG] Engine Mode can't be higher than 4!");
            engine_mode = 4;
        }
        
        update_radius   = getInteger("Options.UpdateRadius");
        if (update_radius > 3) {
            com.log("[TAG] Update Radius can't be higher than 3!");
            update_radius = 3;
        }
        
        /*dark_enabled    = getBoolean("Darkness.Enabled");
        dark_only       = getBoolean("Darkness.BrightOnly");
        dark_radius     = getInteger("Darkness.BrightRadius");
        dark_blocks     = getIntegerHash("Darkness.UpdateOn");*/

        block_place     = getBoolean("UpdateEvents.onBlockPlace");
        block_explosion = getBoolean("UpdateEvents.onBlockExplosion");
        block_damage    = getBoolean("UpdateEvents.onBlockDamage");
        block_piston    = getBoolean("UpdateEvents.onBlockPiston");
        block_physics   = getBoolean("UpdateEvents.onBlockPhysics");

        call_gc         = getInteger("Cache.callGCat");
        enable_cache    = getBoolean("Cache.Enabled");
        size_limit      = getInteger("Cache.SizeLimit");
        writes_sec      = getInteger("Cache.WritesPerSec");
        compress_level  = getInteger("Cache.Compress.Level");
        if (compress_level > 9) {
            com.log("[TAG] The compression level can't be higher then 9!");
            compress_level = 9;
        }

        ophasperm       = getBoolean("Permissions.OpHasPerm");
        
        updates         = getBoolean("Updater.Enabled");
        
        random_blocks   = getIntegerList("Lists.RandomBlocks").toArray(new Integer[] {});
        Arrays.sort(random_blocks);
        
        hidden_blocks   = getIntegerHash("Lists.HiddenBlocks");
        if (hidden_blocks.contains(63)) { hidden_blocks.remove(63); }
        if (hidden_blocks.contains(68)) { hidden_blocks.remove(68); }
        
        disabled_worlds = getStringHash("Lists.DisabledWorlds");
    }
    
    /*
     * return a Boolean from the settings file
     */
    private boolean getBoolean(String node) {
        return Boolean.parseBoolean(getSetting(node));
    }
    
    /*
     * return a Integer from the settings file
     */
    private int getInteger(String node) {
        return Integer.parseInt(getSetting(node));
    }
    
    /*
     * return a List from the Settings file
     */
    private HashSet<String> getStringHash(String node) {
        return FUtil.toStringHashSet(Arrays.asList(getSetting(node).replaceAll(" ", "").split(",")));
    }
    
    private HashSet<Integer> getIntegerHash(String node) {
        return FUtil.toIntegerHashSet(FUtil.toIntegerList(getSetting(node).replaceAll(" ", ""), ","));
    }
    
    private List<Integer> getIntegerList(String node) {
        return FUtil.toIntegerList(getSetting(node).replaceAll(" ", ""), ",");
    }
        
    /*
     * return an Object from the Settings file
     */
    private String getSetting(String node) {
        FCommunicator com    = FAntiXRay.getCommunicator();
        FAntiXRay      plugin = FAntiXRay.getPlugin();
        
        File dir = new File(plugin.getDataFolder(), "settings.yml");
        if (!dir.exists()) { FUtil.ccFile(plugin.getResource("settings.yml"), dir); }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(dir);
            if (!config.contains(node)) {
                InputStream resource = plugin.getResource("settings.yml");
                YamlConfiguration rsconfig = new YamlConfiguration();
                rsconfig.load(resource);

                if (rsconfig.contains(node)) {
                    config.set(node, rsconfig.get(node));
                    com.log("[TAG] Settings file updated, check at: {0}", node);
                } else {
                    config.set(node, node);
                    com.log("[TAG] Can't get setting node: {0}, contact the developer.", Type.SEVERE, node);
                }

                try {
                    config.save(dir);
                } catch (IOException ex) {
                    com.error(getClass().getName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                            "[TAG] Can't update the settings file: {0}", ex.getMessage());            
                }
            }
        } catch (IOException ex) {
            com.error(getClass().getName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Can't load the settings file: {0}", ex.getMessage());  
        } catch (InvalidConfigurationException ex) {
            com.error(getClass().getName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Can't load the settings file: {0}", ex.getMessage());  
            com.log("[TAG] You have a broken node in your settings file at: {0}", node);
        }
        
        String value = config.getString(node);
        if (value == null) {
            com.log(FAntiXRay.tag + " You have a missing setting node at: {0}", Type.SEVERE, node);
            value = node;
        }
        
        return value;
    }
}
