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

import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.FurH.Core.CorePlugin;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.packets.IPacketQueue;
import me.FurH.Core.packets.PacketManager;
import me.FurH.Core.updater.CoreUpdater;
import me.FurH.Core.util.Utils;
import me.FurH.FAntiXRay.cache.FCacheManager;
import me.FurH.FAntiXRay.cache.FChunkCache;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.database.FSQLDatabase;
import me.FurH.FAntiXRay.listener.FPlayerListener;
import me.FurH.FAntiXRay.listener.FUpdateListener;
import me.FurH.FAntiXRay.metrics.FMetricsModule;
import me.FurH.FAntiXRay.queue.PacketQueue;
import me.FurH.FAntiXRay.threads.ObfuscationThreads;
import me.FurH.FAntiXRay.threads.UpdateThreads;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author FurmigaHumana
 */
public class FAntiXRay extends CorePlugin {

    private static HashSet<String> exempt = new HashSet<String>();
    private static FConfiguration configuration;

    public FAntiXRay() {
        super("&8[&3FAntiXRay&8]&7:&f", false, true);
    }

    /* classes */
    private static FAntiXRay plugin;
    private static FChunkCache cache;
    private static FSQLDatabase sql;

    public CoreUpdater updater;
    private IPacketQueue packet;
    
    public static boolean spigot = false;
    
    @Override
    public void onEnable() {
        plugin = this;

        updater = new CoreUpdater(this, "http://dev.bukkit.org/server-mods/antixray/");

        log("[TAG] Initializing configurations...");

        configuration = new FConfiguration(this);
        configuration.load();

        cache = new FChunkCache();
        sql = new FSQLDatabase(this);
        
        try {
            sql.setAutoCommit(false);
        } catch (CoreException ex) {
            error(ex);
        }
        
        sql.setupQueue(0.5, 3);

        try {
            sql.connect();
        } catch (CoreException ex) {
            error(ex);
        }

        sql.load();

        try {
            sql.commit();
        } catch (CoreException ex) {
            error(ex);
        }
        
        sql.setAllowMainThread(false);

        try {
            Class.forName("org.spigotmc.netty.NettyNetworkManager");
            configuration.cache_enabled = false;
            log("[TAG] Netty support enabled!");
            spigot = true;
        } catch (Exception ex) { }

        packet = new PacketQueue(this);
        
        UpdateThreads.setup();
        ObfuscationThreads.setup();

        PluginManager pm = getServer().getPluginManager();

        Plugin nolagg = Bukkit.getPluginManager().getPlugin("NoLagg");
        if (nolagg != null && nolagg.isEnabled()) {
            boolean buffered = nolagg.getConfig().getBoolean("chunks.bufferedLoader.enabled");
            if (buffered) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                    @Override
                    public void run() {
                        log("[TAG] NoLagg bufferedLoader enabled! \n[TAG] Your server is not protected against XRAY!");
                    }
                }, 20 * 3, 20 * 3);
            }
        }

        loadCache();

        log("[TAG] Registring events...");        
        pm.registerEvents(new FPlayerListener(), this);
        pm.registerEvents(new FUpdateListener(), this);

        FMetricsModule metrics = new FMetricsModule();
        metrics.setupMetrics(this);

        if (configuration.updater_enabled) {
            updater.setup();
        }

        PacketManager.register(packet, 51);
        PacketManager.register(packet, 56);

        /*Random rnd = new Random();
        int done = 0;
        
        for (int j1 = 0; j1 < 1000000; j1++) {
            
            byte[] chunk = new byte[ 65535 ];
            rnd.nextBytes(chunk);
            
            try {
                sql.setChunkData("world", rnd.nextLong(), rnd.nextLong(), 4, chunk);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            done++;
            if (done > 1000) {
                System.out.println("DONE: " + j1 + ", OF " + 1000000); done = 0;
            }
            
        }*/
        
        logEnable();
    }

    public void onReload() {
        configuration.load();
    }

    public void loadCache() {
        if (configuration.cache_enabled) {

            final long limit = configuration.cache_size;

            if (limit > 0) {
                FCacheManager.getCacheSizeTask();
            }
        }
    }

    @Override
    public void onDisable() {
        
        Bukkit.getScheduler().cancelTasks(this);
        
        PacketManager.unregister(packet, 51);
        PacketManager.unregister(packet, 56);
        
        try {
            sql.disconnect(false);
        } catch (CoreException ex) {
            error(ex);
        }

        logDisable();
    }

    /*
     * //axr cache[0]
     * //axr cache[0] clear[1]
     * //axr reload[0]
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("fantixray")) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("clear")) {

                    if (!sender.hasPermission("FAntiXRay.ClearCache")) {
                        msg(sender, "&4You don't have permission to use this command");
                        return true;
                    }

                    msg(sender, "&7Cleaning cache...");
                    FCacheManager.clearCache();
                    cache.cache.clear();

                    msg(sender, "&aCache cleared successfully!");
                    return true;
                }
            } else
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("cache")) {

                    if (!sender.hasPermission("FAntiXRay.SeeCache")) {
                        msg(sender, "&4You don't have permission to use this command");
                        return true;
                    }
                    
                    double size = FCacheManager.getCacheSize();
                    double limit = configuration.cache_size;
                    
                    int each = 68;

                    Random rnd = new Random(5);
                    if (rnd.nextBoolean()) {
                        each += rnd.nextInt(5);
                    } else {
                        each -= rnd.nextInt(5);
                    }
                    
                    double m_size = cache.cache.size();
                    double m_limit = configuration.cache_memory;

                    msg(sender, "&7Disk Cache: &a{0}&7 of &a{1}&7 allowed", Utils.getFormatedBytes(size),  Utils.getFormatedBytes(limit));
                    msg(sender, "&7Memory Cache: &a{0}&7 of &a{1}&7 allowed in &a{2}&7 files", Utils.getFormatedBytes(m_size * each * 1024),  Utils.getFormatedBytes(m_limit * each  * 1024), m_size);

                    return true;
                } else
                if (args[0].equalsIgnoreCase("reload")) {

                    if (!sender.hasPermission("FAntiXRay.Reload")) {
                        msg(sender, "&4You don't have permission to use this command");
                        return true;
                    }

                    onReload();

                    msg(sender, "&aConfiguration reloaded successully");
                    return true;
                }
            }

            msg(sender, "&a/axr cache &8-&7 Shows the current cache size");
            msg(sender, "&a/axr cache clear &8-&7 Clear the cache");
            msg(sender, "&a/axr reload &8-&7 Reload the configuration");
            return true;
        }
        
        return true;
    }
    
    public static FSQLDatabase getSQLDatbase() {
        return sql;
    }
    
    public static FConfiguration getConfiguration() {
        return configuration;
    }
    
    public static FChunkCache getCache() {
        return cache;
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
}