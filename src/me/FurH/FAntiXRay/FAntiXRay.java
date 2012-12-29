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

package me.FurH.FAntiXRay;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import me.FurH.FAntiXRay.cache.FCacheQueue;
import me.FurH.FAntiXRay.cache.FChunkCache;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.configuration.FMessages;
import me.FurH.FAntiXRay.hook.FChunkRWork;
import me.FurH.FAntiXRay.listener.FBlockListener;
import me.FurH.FAntiXRay.listener.FEntityListener;
import me.FurH.FAntiXRay.listener.FPlayerListener;
import me.FurH.FAntiXRay.listener.FWorldListener;
import me.FurH.FAntiXRay.metrics.FMetrics;
import me.FurH.FAntiXRay.metrics.FMetrics.Graph;
import me.FurH.FAntiXRay.util.FCommunicator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author FurmigaHumana
 */
public class FAntiXRay extends JavaPlugin {
    public static final Logger log = Logger.getLogger("minecraft");
    public static String tag = "[FAntiXRay]: ";
    public boolean hasUpdate = false;
    public int currentVersion = 0;
    public int newVersion = 0;
    private int files = 0;
    private int size = 0;
    
    /* classes */
    private static FAntiXRay plugin;
    private static FCommunicator communicator;
    private static FMessages messages;
    private static FChunkCache cache;
    private static FConfiguration configuration;
    
    public static HashSet<String> exempt = new HashSet<>();

    @Override
    public void onEnable() {
        plugin = this;
        
        communicator = new FCommunicator();
        messages = new FMessages();
        cache = new FChunkCache();
        configuration = new FConfiguration();
        
        configuration.load();
        messages.load();

        PluginManager pm = getServer().getPluginManager();
        if (configuration.block_explosion) {
            pm.registerEvents(new FEntityListener(), this);
        }

        FBlockListener blockListener = new FBlockListener();
        pm.registerEvents(new FPlayerListener(), this);
        pm.registerEvents(new FWorldListener(), this);
        pm.registerEvents(blockListener, this);
        blockListener.loadListeners(this);

        if (configuration.enable_cache) {
            FCacheQueue.queue();
        }

        startMetrics();
        
        if (configuration.updates) {
            updateThread();
        }

        files = 0; size = 0;
        if (configuration.size_limit > 0) { 
            getCacheSizeTask();
        }

        getCacheSize();
        FChunkRWork.toReloadW();
        
        communicator.log("[TAG] Cache Size: {0} MB in {1} files", size, files);

        PluginDescriptionFile desc = getDescription();
        log.info("[FAntiXRay] FAntiXRay V"+desc.getVersion()+" Enabled");
    }

    @Override
    public void onDisable() {
        FCacheQueue.saveQueue();

        Bukkit.getScheduler().cancelTasks(this);
        
        PluginDescriptionFile desc = getDescription();
        log.info("[FAntiXRay] FAntiXRay V"+desc.getVersion()+" Disabled");
    }

    public void getCacheSizeTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                getCacheSize();
                if (size > configuration.size_limit) {
                    communicator.log("[TAG] The cache is too big, {0} MB of {1} MB allowed, cleaning up!", size, configuration.size_limit);
                    clearCache();
                }
            }
        }, 3600 * 20, 3600 * 20);
    }

    public int getCacheSize() {
        files = 0; size = 0;
        
        for (World w : getServer().getWorlds()) {
            File dir = new File(FAntiXRay.getPlugin().getDataFolder() + File.separator + w.getName());
            if (dir.exists()) {
                for (File filedir : dir.listFiles()) {
                    for (File file : filedir.listFiles()) {
                        size += file.length();
                        files++;
                    }
                }
            }
        }

        size = (int) Math.floor(size / 1024 / 1024);
        
        return size;
    }

    protected void clearCache() {
        for (World w : getServer().getWorlds()) {
            File dir = new File(FAntiXRay.getPlugin().getDataFolder() + File.separator + w.getName());
            if (dir.exists()) {
                for (File file : dir.listFiles()) {
                    file.delete();
                }
            }
        }
    }

    public static FChunkCache getCache() {
        return cache;
    }
    
    public static FConfiguration getConfiguration() {
        return configuration;
    }

    public static FCommunicator getCommunicator() {
        return communicator;
    }

    public static FMessages getMessages() {
        return messages;
    }

    public static FAntiXRay getPlugin() {
        return plugin;
    }

    public static void exempt(String player) {
        exempt.add(player);
    }

    public static void unexempt(String player) {
        exempt.remove(player);
    }

    public static boolean isExempt(String player) {
        return exempt.contains(player);
    }
    
    public boolean hasPerm(CommandSender sender, String perm) {
        if ((perm == null) || (perm.isEmpty())) {
            return true;
        } else {
            if (!(sender instanceof Player)) {
                return true;
            } else {
                Player p = (Player) sender;
                if (p.isOp()) {
                    if (configuration.ophasperm) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (p.hasPermission("FAntiXRay."+perm)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
    }
    
    private int engine0 = 0;
    private int engine1 = 0;
    private int engine2 = 0;
    
    private int up0 = 0;
    private int up1 = 0;
    private int up2 = 0;
    private int up3 = 0;
    
    private int blockplace = 0;
    private int explosion = 0;
    private int damage = 0;
    private int piston = 0;
    private int physics = 0;
    
    private int cached = 0;
    private int uncompressed = 0;
    private int compressed = 0;
    private void startMetrics() {
        try {
            FMetrics metrics = new FMetrics(this);

            Graph dbType = metrics.createGraph("Cache Enabled");
            dbType.addPlotter(new FMetrics.Plotter(Boolean.toString(configuration.enable_cache)) {
                @Override
                public int getValue() {
                    return 1;
                }
            });

            if (configuration.engine_mode == 0) {
                engine0++;
            }
            
            if (configuration.engine_mode == 1) {
                engine1++;
            }
            
            if (configuration.engine_mode == 2) {
                engine2++;
            }
            
            Graph extra = metrics.createGraph("Engine Mode");
            extra.addPlotter(new FMetrics.Plotter("Engine Mode 0") {
                @Override
                public int getValue() {
                    return engine0;
                }
            });
            
            extra.addPlotter(new FMetrics.Plotter("Engine Mode 1") {
                @Override
                public int getValue() {
                    return engine1;
                }
            });
            
            extra.addPlotter(new FMetrics.Plotter("Engine Mode 2") {
                @Override
                public int getValue() {
                    return engine2;
                }
            });
            
            if (configuration.update_radius == 0) {
                up3++;
            }
            
            if (configuration.update_radius == 1) {
                up0++;
            }
            
            if (configuration.update_radius == 2) {
                up1++;
            }
            
            if (configuration.update_radius == 3) {
                up2++;
            }
            
            Graph upa = metrics.createGraph("Update Radius");
            upa.addPlotter(new FMetrics.Plotter("Update Radius 1") {
                @Override
                public int getValue() {
                    return up0;
                }
            });
            
            upa.addPlotter(new FMetrics.Plotter("Update Radius 2") {
                @Override
                public int getValue() {
                    return up1;
                }
            });
            
            upa.addPlotter(new FMetrics.Plotter("Update Radius 3") {
                @Override
                public int getValue() {
                    return up2;
                }
            });
            
            upa.addPlotter(new FMetrics.Plotter("Update Radius 0") {
                @Override
                public int getValue() {
                    return up3;
                }
            });
            
            if (configuration.block_place) {
                blockplace++;
            }
            
            Graph update = metrics.createGraph("Update On");
            update.addPlotter(new FMetrics.Plotter("Block Place") {
                @Override
                public int getValue() {
                    return blockplace;
                }
            });
            
            if (configuration.block_explosion) {
                explosion++;
            }
            
            update.addPlotter(new FMetrics.Plotter("Explosion") {
                @Override
                public int getValue() {
                    return explosion;
                }
            });
            
            if (configuration.block_damage) {
                damage++;
            }
            
            update.addPlotter(new FMetrics.Plotter("Block Damage") {
                @Override
                public int getValue() {
                    return damage;
                }
            });
            
            if (configuration.block_piston) {
                piston++;
            }
            
            update.addPlotter(new FMetrics.Plotter("Block Piston") {
                @Override
                public int getValue() {
                    return piston;
                }
            });
            
            if (configuration.block_physics) {
                physics++;
            }
            
            update.addPlotter(new FMetrics.Plotter("Block Physics") {
                @Override
                public int getValue() {
                    return physics;
                }
            });
            
            if (configuration.enable_cache) {
                cached++;
            }
            
            Graph cachex = metrics.createGraph("Cache System");
            cachex.addPlotter(new FMetrics.Plotter("Cache Enabled") {
                @Override
                public int getValue() {
                    return cached;
                }
            });

            cachex.addPlotter(new FMetrics.Plotter("Cache Disabled") {
                @Override
                public int getValue() {
                    int uncached = 0;
                    
                    if (cached == 0) {
                        uncached++;
                    }

                    return uncached;
                }
            });
            
            if (configuration.compress_level > 0) {
                compressed++;
            }
            
            Graph cachexc = metrics.createGraph("Cache Compression");
            cachexc.addPlotter(new FMetrics.Plotter("Compressed") {
                @Override
                public int getValue() {
                    return compressed;
                }
            });
            
            if (configuration.compress_level < 0) {
                uncompressed++;
            }
            
            cachexc.addPlotter(new FMetrics.Plotter("Uncompressed") {
                @Override
                public int getValue() {
                    return uncompressed;
                }
            });
 
            metrics.start();
        } catch (IOException e) {
        }
    }
    
    public void updateThread() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                currentVersion = Integer.parseInt(getDescription().getVersion().replaceAll("[^0-9]", ""));
                newVersion = Integer.parseInt(getVersion(Integer.toString(currentVersion)).replaceAll("[^0-9]", ""));
                if (newVersion > currentVersion) {
                    communicator.log("New Version Found: {0} (You have: {1})", newVersion, currentVersion);
                    communicator.log("Visit: http://dev.bukkit.org/server-mods/antixray/");
                    hasUpdate = true;
                }
            }
        }, 100, 21600 * 20);
    }
    
    public String getVersion(String current) {
        try {	
            URL url = new URL("http://dev.bukkit.org/server-mods/antixray/files.rss");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("item");
            Node firstNode = nodes.item(0);
            if (firstNode != null) {
                if (firstNode.getNodeType() == 1) {
                    Element firstElement = (Element)firstNode;
                    NodeList firstElementTagName = firstElement.getElementsByTagName("title");
                    Element firstNameElement = (Element) firstElementTagName.item(0);
                    NodeList firstNodes = firstNameElement.getChildNodes();
                    return firstNodes.item(0).getNodeValue();
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException | DOMException e) {
            return current;
        }
        return current;
    }
}