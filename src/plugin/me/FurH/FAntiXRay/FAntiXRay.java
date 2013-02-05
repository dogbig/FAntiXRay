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

package me.FurH.FAntiXRay;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import me.FurH.FAntiXRay.cache.FCacheManager;
import me.FurH.FAntiXRay.cache.FChunkCache;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.configuration.FMessages;
import me.FurH.FAntiXRay.hook.FHookManager;
import me.FurH.FAntiXRay.listener.FBlockListener;
import me.FurH.FAntiXRay.listener.FEntityListener;
import me.FurH.FAntiXRay.listener.FPlayerListener;
import me.FurH.FAntiXRay.metrics.FMetrics;
import me.FurH.FAntiXRay.metrics.FMetrics.Graph;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import me.FurH.FAntiXRay.util.FCommunicator;
import me.FurH.FAntiXRay.util.FUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
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
    private static HashSet<String> exempt = new HashSet<>();
    public static String tag = "[FAntiXRay]: ";

    public boolean hasUpdate = false;
    public int currentVersion = 0;
    public int newVersion = 0;

    /* classes */
    private static FAntiXRay plugin;
    private static FCommunicator communicator;
    private static FMessages messages;
    private static FConfiguration configuration;
    private static FHookManager hook;
    private static FChunkCache cache;

    @Override
    public void onEnable() {
        plugin = this;

        communicator = new FCommunicator();
        messages = new FMessages();
        configuration = new FConfiguration();
        cache = new FChunkCache();

        messages.load();
        configuration.load();
        hook = new FHookManager();

        PluginManager pm = getServer().getPluginManager();

        if (configuration.block_explosion) {
            pm.registerEvents(new FEntityListener(), this);
        }
        
        if (FObfuscator.chest_enabled) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                hook.hook(p);
            }
        }
        
        loadCache();

        FBlockListener blockListener = new FBlockListener();
        pm.registerEvents(new FPlayerListener(), this);
        pm.registerEvents(blockListener, this);
        blockListener.loadListeners(this);

        startMetrics();

        if (configuration.updates) {
            updateThread();
        }

        PluginDescriptionFile desc = getDescription();
        log.info("[FAntiXRay] FAntiXRay V"+desc.getVersion()+" Enabled");
    }
    
    public void onReload() {
        HandlerList.unregisterAll(this);

        Bukkit.getScheduler().cancelTasks(this);

        configuration.load();
        messages.load();

        PluginManager pm = getServer().getPluginManager();
        if (configuration.block_explosion) {
            pm.registerEvents(new FEntityListener(), this);
        }
        
        if (FObfuscator.chest_enabled) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                hook.hook(p);
            }
        }
        
        loadCache();

        FBlockListener blockListener = new FBlockListener();
        pm.registerEvents(new FPlayerListener(), this);
        pm.registerEvents(blockListener, this);
        blockListener.loadListeners(this);

        startMetrics();

        if (configuration.updates) {
            updateThread();
        }
    }
    
    public void loadCache() {
        if (configuration.cache_enabled) {
            if (configuration.size_limit > 0) {
                FCacheManager.getCacheSizeTask();
            }
        
            double size = FCacheManager.getCacheSize();
            double limit = configuration.size_limit;

            communicator.log("[TAG] Cache Size: {0} of {1} allowed in {2} files", FUtils.format(size), FUtils.format(limit), FCacheManager.files.size());
            
            if (size > limit) {
                FAntiXRay.getCommunicator().log("[TAG] The cache is too big, cleaning up!");
                FCacheManager.clearCache();
            }
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        PluginDescriptionFile desc = getDescription();
        log.info("[FAntiXRay] FAntiXRay V"+desc.getVersion()+" Disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("fantixray")) {
            if (args.length <= 0) {
                communicator.msg(sender, "&a/axr cache &8-&7 Shows the current cache size");
                communicator.msg(sender, "&a/axr cache clear &8-&7 Clear the cache");
                communicator.msg(sender, "&a/axr reload &8-&7 Reload the configuration");
                return true;
            } else if (args.length > 0) {
                if (args.length > 1) {
                    if (args.length > 2) {
                        communicator.msg(sender, "&a/axr cache clear &8-&7 Clear the cache");
                        return true;
                    } else {
                        if (args[1].equalsIgnoreCase("clear")) {
                            if (!sender.hasPermission("FAntiXRay.ClearCache")) {
                                communicator.msg(sender, "&4You don't have permission to use this command");
                                return true;
                            } else {
                                communicator.msg(sender, "&7Cleaning cache...");
                                int left = FCacheManager.clearCache();
                                if (left > 0) {
                                    communicator.msg(sender, "&a{0}&7 files could not be deleted!", left);
                                    return true;
                                } else {
                                    communicator.msg(sender, "&aCache cleared successfully!");
                                    return true;
                                }
                            }
                        } else {
                            communicator.msg(sender, "&a/axr cache clear &8-&7 Clear the cache");
                            return true;
                        }
                    }
                } else {
                    if (args[0].equalsIgnoreCase("cache")) {
                        if (!sender.hasPermission("FAntiXRay.SeeCache")) {
                            communicator.msg(sender, "&4You don't have permission to use this command");
                            return true;
                        } else {
                            double size = FCacheManager.getCacheSize();
                            double limit = configuration.size_limit;

                            communicator.msg(sender, "&7Current cache size: &a{0}&7 of &a{1}&7 allowed in &a{2}&7 files", FUtils.format(size), FUtils.format(limit), FCacheManager.files.size());
                            return true;
                        }
                    } else
                    if (args[0].equalsIgnoreCase("reload")) {
                        if (!sender.hasPermission("FAntiXRay.Reload")) {
                            communicator.msg(sender, "&4You don't have permission to use this command");
                            return true;
                        } else {
                            onReload();
                            communicator.msg(sender, "&aConfiguration reloaded successully");
                            return true;
                        }
                    } else {
                        communicator.msg(sender, "&a/axr cache &8-&7 Shows the current cache size");
                        communicator.msg(sender, "&a/axr cache clear &8-&7 Clear the cache");
                        communicator.msg(sender, "&a/axr reload &8-&7 Reload the configuration");
                        return true;
                    }
                }
            } else {
                communicator.msg(sender, "&a/axr cache &8-&7 Shows the current cache size");
                communicator.msg(sender, "&a/axr cache clear &8-&7 Clear the cache");
                communicator.msg(sender, "&a/axr reload &8-&7 Reload the configuration");
                return true;
            }
        }
        return true;
    }
    
    public static FHookManager getHookManager() {
        return hook;
    }

    public static FConfiguration getConfiguration() {
        return configuration;
    }

    public static FCommunicator getCommunicator() {
        return communicator;
    }
    
    public static FChunkCache getCache() {
        return cache;
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
        }
        
        if (!(sender instanceof Player)) {
            return true;
        }
        
        Player p = (Player) sender;
        
        if (p.isOp()) {
            if (!configuration.ophasperm) {
                return false;
            }
        }

        return (p.hasPermission("FAntiXRay."+perm));
    }
    
    private int engine0 = 0;
    private int engine1 = 0;
    private int engine2 = 0;
    
    private int up0 = 0;
    private int up1 = 0;
    private int up2 = 0;
    private int up3 = 0;
    
    private int darkness = 0;
    private int chesthider = 0;
    private int fakecaves = 0;
    
    private int blockplace = 0;
    private int explosion = 0;
    private int damage = 0;
    private int piston = 0;
    private int physics = 0;
    
    private void startMetrics() {
        try {
            FMetrics metrics = new FMetrics(this);

            if (FObfuscator.caves_enabled) {
                fakecaves++;
            }
            
            if (FObfuscator.dark_enabled) {
                darkness++;
            }
            
            if (FObfuscator.chest_enabled) {
                chesthider++;
            }
            
            if (FObfuscator.engine_mode == 0) {
                engine0++;
            }
            
            if (FObfuscator.engine_mode == 1) {
                engine1++;
            }
            
            if (FObfuscator.engine_mode == 2) {
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
            metrics.start();
        } catch (IOException e) {
        }
    }
    
    public void updateThread() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
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