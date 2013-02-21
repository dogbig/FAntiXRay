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

package me.FurH.FAntiXRay.metrics;

import java.io.IOException;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.metrics.FMetrics.Graph;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;

/**
 *
 * @author FurmigaHumana
 */
public class FMetricsModule {
    private FMetrics metrics;;
    
    public void setupMetrics(FAntiXRay plugin) {
        try {
            metrics = new FMetrics(plugin);
            
            /* setup the engine mode graph */
            setupEngineMode(); // -> 2
            
            /* setup the update radius graph */
            setupUpdateRadius(); // -> 3
            
            /* setup the cave generator enabled graph */
            setupFakeCavesGenerator(); // -> 4
            
            /* setup the cave generator intensity graph */
            setupFakeCavesIntensity(); // -> 5
            
            /* setup the cache system enabled graph */
            setupCacheSystem(); // -> 6
            
            /* setup the cache compression graph */
            setupCacheCompression(); // -> 7
            
            /* setup the darkness system enabled graph */
            setupDarkness(); // -> 8
            
            /* setup the block update settings graph */
            setupBlockUpdate(); // -> 9
            
            /* setup the chest hider enabled graph */
            setupChestHider(); // -> 10
            
            /* setup the chest interval graph */
            setupChestInterval(); // -> 11
            
            /* setup the chest radius graph */
            setupChestRadius(); // -> 12
            
            metrics.start();
        } catch (IOException e) {
        }
    }
    
    private void setupEngineMode() {
        Graph engine_mode = metrics.createGraph("Engine Mode");
        engine_mode.addPlotter(new FMetrics.Plotter("Engine Mode " + FObfuscator.engine_mode) {
            @Override
            public int getValue() {
                return 1;
            }
        });
    }
    
    private void setupUpdateRadius() {
        Graph update_radius = metrics.createGraph("Update Radius");
        update_radius.addPlotter(new FMetrics.Plotter("Update Radius " + FAntiXRay.getConfiguration().update_radius) {
            @Override
            public int getValue() {
                return 1;
            }
        });
    }
    
    private void setupFakeCavesGenerator() {
        Graph fake_caves = metrics.createGraph("Fake Caves");
        fake_caves.addPlotter(new FMetrics.Plotter("Enabled: " + FObfuscator.caves_enabled) {
            @Override
            public int getValue() {
                return 1;
            }
        });
    }
    
    private void setupFakeCavesIntensity() {
        Graph fake_caves = metrics.createGraph("Fake Caves Intensity");
        fake_caves.addPlotter(new FMetrics.Plotter("Intensity: " + FObfuscator.caves_intensity) {
            @Override
            public int getValue() {
                return 1;
            }
        });
    }
    
    private void setupCacheSystem() {
        Graph cache_system = metrics.createGraph("Cache System");
        cache_system.addPlotter(new FMetrics.Plotter("Enabled: " + FAntiXRay.getConfiguration().cache_enabled) {
            @Override
            public int getValue() {
                return 1;
            }
        });
    }
    
    private void setupCacheCompression() {
        Graph cache_compression = metrics.createGraph("Cache Compression");
        if (FAntiXRay.getConfiguration().compress_level > 0) {
            cache_compression.addPlotter(new FMetrics.Plotter("Compression: " + FAntiXRay.getConfiguration().compress_level) {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        } else {
            cache_compression.addPlotter(new FMetrics.Plotter("Uncompressed") {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }
    }
    
    private void setupDarkness() {
        Graph darkness = metrics.createGraph("Darkness");
        darkness.addPlotter(new FMetrics.Plotter("Enabled: " + FObfuscator.dark_enabled) {
            @Override
            public int getValue() {
                return 1;
            }
        });
    }
    
    private void setupBlockUpdate() {
        Graph block_update = metrics.createGraph("Block Update");
        if (FAntiXRay.getConfiguration().dark_update) {
            block_update.addPlotter(new FMetrics.Plotter("Dark Update") {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }
        if (FAntiXRay.getConfiguration().block_place) {
            block_update.addPlotter(new FMetrics.Plotter("Block Place") {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }
        if (FAntiXRay.getConfiguration().block_explosion) {
            block_update.addPlotter(new FMetrics.Plotter("Explosion") {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }
        if (FAntiXRay.getConfiguration().block_damage) {
            block_update.addPlotter(new FMetrics.Plotter("Block Damage") {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }
        if (FAntiXRay.getConfiguration().block_physics) {
            block_update.addPlotter(new FMetrics.Plotter("Block Physics") {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }
        if (FAntiXRay.getConfiguration().block_piston) {
            block_update.addPlotter(new FMetrics.Plotter("Block Piston") {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }
        if (FAntiXRay.getConfiguration().block_threaded) {
            block_update.addPlotter(new FMetrics.Plotter("Threaded Update") {
                @Override
                public int getValue() {
                    return 1;
                }
            });
        }
    }
    
    private void setupChestHider() {
        Graph darkness = metrics.createGraph("Chest Hider");
        darkness.addPlotter(new FMetrics.Plotter("Enabled: " + FObfuscator.chest_enabled) {
            @Override
            public int getValue() {
                return 1;
            }
        });
    }
    
    private void setupChestInterval() {
        Graph darkness = metrics.createGraph("Chest Hider Interval");
        darkness.addPlotter(new FMetrics.Plotter("Interval: " + FAntiXRay.getConfiguration().chest_interval) {
            @Override
            public int getValue() {
                return 1;
            }
        });
    }
    
    private void setupChestRadius() {
        Graph darkness = metrics.createGraph("Chest Hider Radius");
        darkness.addPlotter(new FMetrics.Plotter("Radius: " + FAntiXRay.getConfiguration().chest_radius) {
            @Override
            public int getValue() {
                return 1;
            }
        });
    }
}
