package me.FurH.FAntiXRay.timings;

import me.FurH.FAntiXRay.FAntiXRay;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class FTimingsCore {
    
    private FTimingsCoreSpigot handler;
    
    public FTimingsCore(String name, FTimingsCore parent) {
        if (FAntiXRay.spigot) { this.handler = new FTimingsCoreSpigot(name, parent); }
    }
    
    public FTimingsCore(String name) {
        if (FAntiXRay.spigot) { this.handler = new FTimingsCoreSpigot(name); }
    }
    
    public void start() {
        if (this.handler != null) { this.handler.start(); }
    }
    
    public void stop() {
        if (this.handler != null) { this.handler.stop(); }
    }

    public class FTimingsCoreSpigot extends FTimingsCore {
        
        private org.bukkit.CustomTimingsHandler handler;

        public FTimingsCoreSpigot(String name, FTimingsCore parent) {
            super(name, parent);
            
            handler = new org.bukkit.CustomTimingsHandler(name, ((FTimingsCoreSpigot)parent).handler);
        }

        public FTimingsCoreSpigot(String name) {
            super(name);
            
            handler = new org.bukkit.CustomTimingsHandler(name);
        }

        @Override
        public void start() {
            handler.startTiming();
        }

        @Override
        public void stop() {
            handler.stopTiming();
        }
    }
}
