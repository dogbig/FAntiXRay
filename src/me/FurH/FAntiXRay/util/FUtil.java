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
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import me.FurH.FAntiXRay.FAntiXRay;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author FurmigaHumana
 */
public class FUtil {
    
    public static void setPrivateField(Object obj, String x, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(x);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            FAntiXRay.getCommunicator().error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to set private field: {0} to {1}, {2}", x, value, ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public static Object getPrivateField(Object obj, String x) {
        try {
            Field f = obj.getClass().getDeclaredField(x);
            f.setAccessible(true);
            return f.get(obj);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            FAntiXRay.getCommunicator().error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to get private field: {0} , {1}", x, ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
    
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
        FCommunicator com    = FAntiXRay.getCommunicator();
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
                        com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                                "[TAG] {0} is not a valid number!, {1}", str, ex.getMessage());
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
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to parse string to list: {0}, split: {1}, {2}", string, split, ex.getMessage());
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
    public static String stack(String className, int line, String method, Throwable ex, String message) {
        FAntiXRay      plugin = FAntiXRay.getPlugin();
        String format1 = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(System.currentTimeMillis());
        File data = new File(plugin.getDataFolder() + File.separator + "error", "");
        if (!data.exists()) { data.mkdirs(); }
        
        FCommunicator com    = FAntiXRay.getCommunicator();
        data = new File(data.getAbsolutePath(), "error-"+format1+".txt");
        if (!data.exists()) {
            try {
                data.createNewFile();
            } catch (IOException e) {
                
                com.log("Failed to create new log file, {0} .", e.getMessage());
            }
        }
        
        try {
            StackTraceElement[] st = ex.getStackTrace();
            String l = System.getProperty("line.separator");

            String format2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(System.currentTimeMillis());
            FileWriter fw = new FileWriter(data, true);
            BufferedWriter bw = new BufferedWriter(fw);
            Runtime runtime = Runtime.getRuntime();
            
            File root = new File("/");

            bw.write(format2 +l);
            bw.write("	=============================[ ERROR INFORMATION ]============================="+l);
            bw.write("	- Plugin: " + plugin.getDescription().getFullName() + " (Latest: " + plugin.getVersion("1.0") + ")" +l);
            bw.write("	- Error Message: " + ex.getMessage() +l);
            bw.write("	- Location: " + className + ", Line: " + line + ", Method: " + method +l);
            bw.write("	- Comment: " + message +l);
            bw.write("	=============================[ HARDWARE SETTINGS ]============================="+l);
            bw.write("		Java: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("java.vendor.url") +l);
            bw.write("		System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") +l);
            bw.write("		Processors: " + runtime.availableProcessors() +l);
            bw.write("		Memory: "+l);
            bw.write("			Free: " + runtime.freeMemory() +l);
            bw.write("			Total: " + runtime.totalMemory() +l);
            bw.write("			Max: " + runtime.maxMemory() +l);
            bw.write("		Storage: "+l);
            bw.write("			Total: " + root.getTotalSpace() +l);
            bw.write("			Free: " + root.getTotalSpace() +l);
            bw.write("	=============================[ INSTALLED PLUGINS ]============================="+l);
            bw.write("	Plugins:"+l);
            for (Plugin x : plugin.getServer().getPluginManager().getPlugins()) {
                bw.write("		- " + x.getDescription().getFullName() +l);
            }
            bw.write("	=============================[  LOADED  WORLDS  ]============================="+l);
            bw.write("	Worlds:"+l);
            for (World w : plugin.getServer().getWorlds()) {
                bw.write("		" + w.getName() + ":" +l);
                bw.write("			Envioronment: " + w.getEnvironment().toString() +l);
                bw.write("			Player Count: " + w.getPlayers().size() +l);
                bw.write("			Entity Count: " + w.getEntities().size() +l);
                bw.write("			Loaded Chunks: " + w.getLoadedChunks().length +l);
            }
            bw.write("	=============================[ ERROR  STACKTRACE ]============================="+l);
            for (StackTraceElement element : st) {
                bw.write("		- " + element.toString()+l);
            }
            bw.write("	=============================[ END OF STACKTRACE ]============================="+l);
            bw.write(format2);
            bw.close();
            fw.close();
        } catch (IOException e) {
            com.log("Failed to write in the log file, {0}", e.getMessage());
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
            com.error(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getLineNumber(), Thread.currentThread().getStackTrace()[1].getMethodName(), ex, 
                    "[TAG] Failed to copy the file {0}, {1}", file.getName(), ex.getMessage());
        }
    }
}
