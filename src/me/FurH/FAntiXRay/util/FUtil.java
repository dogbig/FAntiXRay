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

package me.FurH.FAntiXRay.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author FurmigaHumana
 */
public class FUtil {
    
    /*
     * return a HashSet of the List contends
     */
    public static HashSet<String> toStringHashSet(List<String> list) {
        HashSet<String> set = new HashSet<>();
        set.addAll(list);
        return set;
    }
    
    /*
     * return a HashSet of the List contends
     */
    public static HashSet<Integer> toIntegerHashSet(List<Integer> list) {
        HashSet<Integer> set = new HashSet<>();
        set.addAll(list);
        return set;
    }
    
    /*
     * convert and string to a list
     */
    public static List<Integer> toIntegerList(String string, String split) {
        try {
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            if (string.contains(split) && !"[]".equals(string)) {
                List<Integer> ints = new ArrayList<>();
                String[] splits = string.split(split);

                for (String str : splits) {
                    try {
                        int i = Integer.parseInt(str);
                        ints.add(i);
                    } catch (Exception ex) {
                        FCommunicator com    = FAntiXRay.getCommunicator();
                        com.error("[TAG] {0} is not a valid number!, {1}", ex, str, ex.getMessage());
                    }
                }
                
                return ints;
            } else {
                if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                    return Arrays.asList(new Integer[] { Integer.parseInt(string) });
                } else {
                    return new ArrayList<>();
                }
            }
        } catch (Exception ex) {
            FCommunicator com    = FAntiXRay.getCommunicator();
            com.error("[TAG] Failed to parse string to list: {0}, split: {1}, {2}", ex, string, split, ex.getMessage());
            return new ArrayList<>();
        }
    }
    
    public static int getRandom() {
        int random = ((int)(Math.random() * FAntiXRay.getConfiguration().random_blocks.length));
        return FAntiXRay.getConfiguration().random_blocks[ random ];
    }
    
    /*
     * Dump the stack to a file
     */
    public static String stack(Throwable ex) {
        FAntiXRay      plugin = FAntiXRay.getPlugin();
        String format1 = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(System.currentTimeMillis());
        File data = new File(plugin.getDataFolder() + File.separator + "error", "");
        if (!data.exists()) { data.mkdirs(); }
        
        data = new File(data.getAbsolutePath(), "error-"+format1+".txt");
        if (!data.exists()) {
            try {
                data.createNewFile();
            } catch (IOException e) {
                FCommunicator com    = FAntiXRay.getCommunicator();
                com.error("Failed to create new log file, {0} .", e, e.getMessage());
            }
        }
        
        StackTraceElement[] st = ex.getStackTrace();
        FileWriter Writer;
        try {
            String format2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(System.currentTimeMillis());
            Writer = new FileWriter(data, true);
            BufferedWriter Out = new BufferedWriter(Writer);
            Out.write(format2 + " - " + "Error Message: " + ex.getMessage() + System.getProperty("line.separator"));
            
            List<String> pls = new ArrayList<>();
            Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();
            for (Plugin pl1 : plugins) {
                pls.add(pl1.getDescription().getFullName());
            }

            Out.write(format2 + " - " + "Plugins ("+pls.size()+"): " + pls.toString() + System.getProperty("line.separator"));
            Out.write(format2 + " - " + "=============================[ ERROR  STACKTRACE ]=============================" + System.getProperty("line.separator"));
            for (int i = 0; i < st.length; i++) {
                Out.write(format2 + " - " + st[i].toString() + System.getProperty("line.separator"));
            }

            Out.write(format2 + " - " + "=============================[ END OF STACKTRACE ]=============================" + System.getProperty("line.separator"));
            Out.close();
        } catch (IOException e) {
            FCommunicator com    = FAntiXRay.getCommunicator();
            com.error("Failed to write in the log file, {0}", e, e.getMessage());
        }
        
        return format1;
    }

    /*
     * Copy a file from a location
     */
    public static void ccFile(InputStream in, File file) {
        try {
            if ((file.getParentFile() != null) && (!file.getParentFile().exists())) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[512];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.close();
        } catch (IOException ex) {
            FCommunicator com    = FAntiXRay.getCommunicator();
            com.error("Failed to copy the file {0}, {1}", ex, file.getName(), ex.getMessage());
        }
    }
}
