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
import me.FurH.Core.CorePlugin;
import me.FurH.Core.updater.CoreUpdater;
import me.FurH.Core.util.Utils;
import me.FurH.FAntiXRay.cache.FCacheManager;
import me.FurH.FAntiXRay.cache.FChunkCache;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.hook.FBukkitHook;
import me.FurH.FAntiXRay.hook.FHookManager;
import me.FurH.FAntiXRay.hook.FNettyHook;
import me.FurH.FAntiXRay.listener.FBlockListener;
import me.FurH.FAntiXRay.listener.FPlayerListener;
import me.FurH.FAntiXRay.metrics.FMetricsModule;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author FurmigaHumana
 */
public class FAntiXRay extends CorePlugin {

    public FAntiXRay() {
        super("&8[&3FAntiXRay&8]&7:&f");
    }

    /* classes */
    private static FAntiXRay plugin;
    private static FConfiguration configuration;
    private static FHookManager hook;
    private static FChunkCache cache;
    public CoreUpdater updater;
    
    private static HashSet<String> exempt = new HashSet<String>();

    @Override
    public void onEnable() {
        plugin = this;

        updater = new CoreUpdater(this, "http://dev.bukkit.org/server-mods/antixray/");

        log("[TAG] Initializing configurations...");

        configuration = new FConfiguration(this);
        configuration.load();
        
        cache = new FChunkCache();
        
        boolean natty = true;

        try {
            Class.forName("org.spigotmc.netty.NettyNetworkManager");
        } catch (NoClassDefFoundError ex) {
            natty = false;
        } catch (ClassNotFoundException ex) {
            natty = false;
        }

        if (natty) {
            hook = new FNettyHook();
            log("[TAG] Netty support enabled!");
        } else {
            hook = new FBukkitHook();
        }

        PluginManager pm = getServer().getPluginManager();

        for (Player p : Bukkit.getOnlinePlayers()) {
            hook.unhook(p);
            hook.hook(p);
        }

        Plugin nolagg = Bukkit.getPluginManager().getPlugin("NoLagg");
        if (nolagg != null && nolagg.isEnabled()) {
            boolean buffered = nolagg.getConfig().getBoolean("chunks.bufferedLoader.enabled");
            if (buffered) {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                    @Override
                    public void run() {
                        log("[TAG] NoLagg bufferedLoader enabled! X-Ray will work!");
                    }
                }, 20 * 3, 20 * 3);
            }
        }

        loadCache();

        log("[TAG] Registring events...");
        FBlockListener blockListener = new FBlockListener();
        pm.registerEvents(new FPlayerListener(), this);

        blockListener.loadListeners(this);

        FMetricsModule metrics = new FMetricsModule();
        metrics.setupMetrics(this);

        if (configuration.updater_enabled) {
            updater.setup();
        }

        logEnable();
    }

    public void onReload() {
        configuration.load();
    }

    public void loadCache() {
        if (configuration.cache_enabled) {
            cache.setup();

            final long limit = configuration.cache_size;
            
            Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {

                @Override
                public void run() {
                    if (limit > 0) {
                        FCacheManager.getCacheSizeTask();
                    }

                    long size = FCacheManager.getCacheSize();

                    log("[TAG] Cache Size: {0} of {1} allowed in {2} files", Utils.getFormatedBytes(size), Utils.getFormatedBytes(limit), FCacheManager.files.size());

                    if (limit > 0 && size > limit) {
                        log("[TAG] The cache is too big, cleaning up!");
                        FCacheManager.clearCache();
                    }
                }
            });
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        cache.stop();

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
                    int left = FCacheManager.clearCache();
                    cache.cache.clear();

                    if (left > 0) {
                        msg(sender, "&a{0}&7 files could not be deleted!", left);
                        return true;
                    }

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

                    msg(sender, "&7Disk Cache: &a{0}&7 of &a{1}&7 allowed in &a{2}&7 files", Utils.getFormatedBytes(size),  Utils.getFormatedBytes(limit), FCacheManager.files.size());
                    msg(sender, "&7Memory Cache: &a{0}&7 of &a{1}&7 allowed in &a{2}&7 files", Utils.getFormatedBytes(m_size * each),  Utils.getFormatedBytes(m_limit * each), m_size);

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

    public static FHookManager getHookManager() {
        return hook;
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