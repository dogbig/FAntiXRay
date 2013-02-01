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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.util.FCommunicator;
import me.FurH.FAntiXRay.util.FCommunicator.Type;
import me.FurH.FAntiXRay.util.FUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author FurmigaHumana
 */
public class FMessages {
    public String prefix_tag    = "&4[FAntiXRay]:&7";
    
    public String deobfuscated  = "OnLogin.Deobfuscated";
    
    public String update1       = "Updater.new";
    public String update2       = "Updater.visit";

    public void load() {
        deobfuscated  = getMessage("OnLogin.Deobfuscated");
        update1       = getMessage("Updater.new");
        update2       = getMessage("Updater.visit");
    }
    
    private String getMessage(String node) {
        FCommunicator com    = FAntiXRay.getCommunicator();
        FAntiXRay      plugin = FAntiXRay.getPlugin();
        
        File dir = new File(plugin.getDataFolder(), "messages.yml");
        if (!dir.exists()) { FUtils.ccFile(plugin.getResource("messages.yml"), dir); }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(dir);
            if (!config.contains(node)) {
                InputStream resource = plugin.getResource("messages.yml");
                YamlConfiguration rsconfig = new YamlConfiguration();
                rsconfig.load(resource);

                if (rsconfig.contains(node)) {
                    config.set(node, rsconfig.getString(node));
                    com.log(FAntiXRay.tag + "Messages file updated, check at: {0}", node);
                } else {
                    config.set(node, node);
                    com.log(FAntiXRay.tag + "Can't get the message node {0}, contact the developer.", Type.SEVERE, node);
                }

                try {
                    config.save(dir);
                } catch (IOException ex) {
                    com.error(getClass().getName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                        FAntiXRay.tag + "[TAG] Can't update the messages file: {0}", ex.getMessage());  
                }
            }
        } catch (IOException ex) {
            com.error(getClass().getName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                FAntiXRay.tag + "[TAG] Can't load the messages file: {0}", ex.getMessage()); 
        } catch (InvalidConfigurationException ex) {
            com.error(getClass().getName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                FAntiXRay.tag + "[TAG] Can't load the messages file: {0}", ex.getMessage()); 
            com.log(FAntiXRay.tag + " You have a broken message node at: {0}", node);
        }
        
        String value = config.getString(node);
        if (value == null) {
            com.log(FAntiXRay.tag + " You have a missing message node at: {0}", Type.SEVERE, node);
            value = node;
        }
        return value;
    }
}
