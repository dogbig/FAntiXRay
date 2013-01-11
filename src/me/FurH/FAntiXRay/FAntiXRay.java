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

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.configuration.FMessages;
import me.FurH.FAntiXRay.listener.FBlockListener;
import me.FurH.FAntiXRay.listener.FEntityListener;
import me.FurH.FAntiXRay.listener.FPlayerListener;
import me.FurH.FAntiXRay.metrics.FMetrics;
import me.FurH.FAntiXRay.metrics.FMetrics.Graph;
import me.FurH.FAntiXRay.util.FCommunicator;
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
    public static String tag = "[FAntiXRay]: ";
    public boolean hasUpdate = false;
    public int currentVersion = 0;
    public int newVersion = 0;
    
    /* classes */
    private static FAntiXRay plugin;
    private static FCommunicator communicator;
    private static FMessages messages;
    private static FConfiguration configuration;

    @Override
    public void onEnable() {
        plugin = this;
        
        communicator = new FCommunicator();
        messages = new FMessages();
        configuration = new FConfiguration();
        
        messages.load();
        configuration.load();

        PluginManager pm = getServer().getPluginManager();
        if (configuration.block_explosion) {
            pm.registerEvents(new FEntityListener(), this);
        }
        
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
        
        FBlockListener blockListener = new FBlockListener();
        pm.registerEvents(new FPlayerListener(), this);
        pm.registerEvents(blockListener, this);
        blockListener.loadListeners(this);

        startMetrics();
        
        if (configuration.updates) {
            updateThread();
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        
        PluginDescriptionFile desc = getDescription();
        log.info("[FAntiXRay] FAntiXRay V"+desc.getVersion()+" Disabled");
    }
    
    public void onUnload() {
        this.setEnabled(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("fantixray")) {
            if (args.length <= 0) {
                communicator.msg(sender, "&a/axr reload &8-&7 Reload the configuration");
                return true;
            } else if (args.length > 0) {
                if (args.length > 1) {
                    communicator.msg(sender, "&a/axr reload &8-&7 Reload the configuration");
                    return true;
                } else {
                    if (args[0].equalsIgnoreCase("reload")) {
                        if (!sender.hasPermission("FAntiXRay.ReloadConfig")) {
                            communicator.msg(sender, "&4You don't have permission to use this command");
                            return true;
                        } else {
                            onReload();
                            communicator.msg(sender, "&aConfiguration reloaded successully");
                            return true;
                        }
                    } else {
                        communicator.msg(sender, "&a/axr reload &8-&7 Reload the configuration");
                        return true;
                    }
                }
            } else {
                communicator.msg(sender, "&a/axr reload &8-&7 Reload the configuration");
                return true;
            }
        }
        return true;
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
        org.bukkit.craftbukkit.v1_4_6.FAntiXRay.exempt(player);
    }

    public static void unexempt(String player) {
        org.bukkit.craftbukkit.v1_4_6.FAntiXRay.unexempt(player);
    }

    public static boolean isExempt(String player) {
        return org.bukkit.craftbukkit.v1_4_6.FAntiXRay.isExempt(player);
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
    private int engine3 = 0;
    private int engine4 = 0;
    
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

            if (configuration.engine_mode == 0) {
                engine0++;
            }
            
            if (configuration.engine_mode == 1) {
                engine1++;
            }
            
            if (configuration.engine_mode == 2) {
                engine2++;
            }
            
            if (configuration.engine_mode == 3) {
                engine3++;
            }
            
            if (configuration.engine_mode == 4) {
                engine4++;
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
            
            extra.addPlotter(new FMetrics.Plotter("Engine Mode 3") {
                @Override
                public int getValue() {
                    return engine3;
                }
            });
            
            extra.addPlotter(new FMetrics.Plotter("Engine Mode 4") {
                @Override
                public int getValue() {
                    return engine4;
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