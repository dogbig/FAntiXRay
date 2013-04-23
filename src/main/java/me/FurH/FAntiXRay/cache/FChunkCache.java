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

package me.FurH.FAntiXRay.cache;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import me.FurH.Core.cache.CoreSafeCache;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;

/**
 *
 * @author FurmigaHumana
 */
public class FChunkCache {

    private ConcurrentLinkedQueue<FCacheData> queue = new ConcurrentLinkedQueue<FCacheData>();
    public CoreSafeCache<String, FCacheData> cache = new CoreSafeCache<String, FCacheData>(3600);

    private int run_files = 0;
    private File main_dir;
    
    public void setup() {
        main_dir = FAntiXRay.getPlugin().getDataFolder();

        FConfiguration config = FAntiXRay.getConfiguration();
        cache = new CoreSafeCache<String, FCacheData>(config.cache_memory);

        if (FAntiXRay.netty) {
            return;
        }
        
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        FCacheData data = queue.poll();
                        if (data == null) {
                            sleep(10000);
                        } else {
                            
                            write(data);
                            sleep(500);

                            if (cache.size() > 500) {
                                overload();
                            }
                        }
                    } catch (Exception ex) {
                        try {
                            sleep(10000);
                        } catch (InterruptedException ex1) { }
                    }
                }
            }
        };
        thread.setName("FAntiXRay Write Task");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    
    public void stop() {
        while (!queue.isEmpty()) {
            try {
                FCacheData data = queue.poll();
                if (data != null) {
                    write(data);
                }
            } catch (Exception ex) { }
        }
    }
    
    public void overload() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                int run = 0;

                while (run < 100) {
                    try {
                        FCacheData data = queue.poll();
                        if (data != null) {
                            write(data);
                        } else {
                            break;
                        }
                    } catch (Exception ex) {
                        interrupt();
                    }
                }
            }
        };
        thread.setName("FAntiXRay Overload Task");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private byte[] read(FCacheData data, String world, int x, int z, long hash, int engine_mode) {
        
        if (data == null) {
            return null;
        }
        
        if (data.x != x || data.z != z || !data.world.equals(world)) {
            return null;
        }
        
        if (data.engine != engine_mode) {
            return null;
        }
        
        if (data.hash != hash) {
            return null;
        }
        
        return data.obfuscated;
    }
    
    public byte[] read(String world, int x, int z, long hash, int engine_mode) {
        FConfiguration config = FAntiXRay.getConfiguration();

        boolean cached = cache.containsKey(toString(x, z, world));
        if (cached) {
            return read(cache.get(toString(x, z, world)), world, x, z, hash, engine_mode);
        }

        if (FAntiXRay.netty) {
            return null;
        }

        if (run_files >= config.cache_callgc) {
            System.runFinalization();
            System.gc();
            run_files = 0;
        }
        
        File dir = new File(main_dir + File.separator + world + File.separator + "r." + (x >> 5) + "." + (z >> 5));
        if (!dir.exists()) { return null; }

        File file = new File(dir, "r." + x + "." + z + ".cdat");
        FCacheData data = null;

        FileInputStream fis = null;
        ZipInputStream zis = null;
        ObjectInputStream ois = null;

        try {
            if (!file.exists()) { return null; }

            fis = new FileInputStream(file);
            zis = new ZipInputStream(fis);
            
            zis.getNextEntry();
            
            ois = new ObjectInputStream(zis);

            data = (FCacheData) ois.readObject();

            run_files++;
        } catch (EOFException ex) {
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (zis != null) {
                try {
                    zis.closeEntry();
                    zis.close();
                } catch (Exception ex) { }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (Exception ex) { }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ex) { }
            }
            ois = null; zis = null; fis = null;
        }

        cache.put(toString(x, z, world), data);
        return read(data, world, x, z, hash, engine_mode);
    }

    public void write(String world, int x, int z, byte[] obufscated, long hash, int engine_mode) {
        FCacheData data = new FCacheData(world, x, z, obufscated, hash, engine_mode);

        if (data.obfuscated == null) {
            return;
        }
        
        if (data.obfuscated.length == 0) {
            return;
        }
        
        cache.put(toString(x, z, world), data);
        
        if (FAntiXRay.netty) {
            return;
        }
        
        if (queue.contains(data)) {
            queue.remove(data);
        }

        queue.add(data);
    }
    
    public void write(FCacheData data) {
        FConfiguration config = FAntiXRay.getConfiguration();

        if (FAntiXRay.netty) {
            return;
        }
        
        if (run_files >= config.cache_callgc) {
            System.runFinalization();
            System.gc();
            run_files = 0;
        }

        File dir = new File(main_dir + File.separator + data.world + File.separator + "r." + (data.x >> 5) + "." + (data.z >> 5));
        if (!dir.exists()) { dir.mkdirs(); }

        File file = new File(dir, "r." + data.x + "." + data.z + ".cdat");

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        ZipOutputStream zos = null;
        ZipEntry entry = null;

        try {
            
            if (file.exists()) {
                file.delete();
            }
            
            file.createNewFile(); 
            fos = new FileOutputStream(file);         

            int compression = config.cache_compression;
            zos = new ZipOutputStream(fos);

            if (compression > 9) {
                config.cache_compression = 9;
            }

            zos.setLevel(config.cache_compression);
            entry = new ZipEntry("c." + data.x + "." + data.z + ".chunk");

            zos.putNextEntry(entry);
            oos = new ObjectOutputStream(zos);

            oos.writeObject(data);

            oos.flush();
            zos.flush();
            fos.flush();

            run_files++;
        } catch (EOFException ex) {
            file.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (zos != null) {
                try {
                    zos.closeEntry();
                    zos.close();
                } catch (Exception ex) { }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (Exception ex) { }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex) { }
            }
            zos = null; oos = null; fos = null; entry = null;
        }
    }

    private String toString(int x, int z, String world) {
        return (x) + "" + (z) + "" + world;
    }

    public void clear() {
        queue.clear();
        cache.clear();
    }
}
