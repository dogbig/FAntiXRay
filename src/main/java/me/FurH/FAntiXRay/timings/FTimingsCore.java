package me.FurH.FAntiXRay.timings;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class FTimingsCore {
    
    //private FTimingsCoreSpigot handler;
    
    public FTimingsCore(String name, FTimingsCore parent) {
        
        if (name == null) {
            return;
        }
        
        //if (FAntiXRay.spigot) { this.handler = new FTimingsCoreSpigot(name, parent); }
    }
    
    public FTimingsCore(String name) {
        
        if (name == null) {
            return;
        }
        
        //if (FAntiXRay.spigot) { this.handler = new FTimingsCoreSpigot(name); }
    }
    
    public void start() {
        //if (this.handler != null) { this.handler.start(); }
    }
    
    public void stop() {
        //if (this.handler != null) { this.handler.stop(); }
    }

    /*public class FTimingsCoreSpigot extends FTimingsCore {
        
        private org.bukkit.CustomTimingsHandler handler;

        public FTimingsCoreSpigot(String name, FTimingsCore parent) {
            super(null, null);
            
            handler = new org.bukkit.CustomTimingsHandler(name, parent.handler.handler);
        }

        public FTimingsCoreSpigot(String name) {
            super(null);
            
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
    }*/
}
