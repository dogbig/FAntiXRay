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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import net.minecraft.server.v1_5_R2.World;

/**
 *
 * @author FurmigaHumana
 */
public class FChunkCache {
    public ConcurrentHashMap<String, FCacheData> cache = new ConcurrentHashMap<String, FCacheData>();
    public AtomicBoolean lock = new AtomicBoolean(false);
    private int callgc = 0;
    
    public void stop() {
        lock.set(true);

        while (!cache.isEmpty()) {
            try {
                FCacheData data = cache.remove(cache.keySet().iterator().next());
                if (data != null) {
                    write(data);
                }
            } catch (NoSuchElementException ex) { }
        }
    }
    
    public void overload() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                int run = 0;

                while (run < 100) {
                    try {
                        FCacheData data = cache.remove(cache.keySet().iterator().next());
                        if (data != null) {
                            write(data);
                        }
                    } catch (NoSuchElementException ex) {
                        interrupt();
                    }
                }
            }
        };
        thread.setName("FAntiXRay Overload Task");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void cacheWriteTask() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!lock.get()) {
                    try {
                        FCacheData data = cache.remove(cache.keySet().iterator().next());
                        if (data == null) {
                            sleep(10000);
                        } else {
                            write(data);
                            sleep(500);

                            if (cache.size() > 500) {
                                overload();
                            }
                        }
                    } catch (NoSuchElementException ex) {
                        try {
                            sleep(10000);
                        } catch (InterruptedException ex1) { }
                    } catch (InterruptedException ex) { }
                }
            }
        };
        thread.setName("FAntiXRay Write Task");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    
    public byte[] read(World world, int x1, int z1, long hash1, int engine1) {
        FConfiguration config = FAntiXRay.getConfiguration();

        if (callgc >= config.file_call_gc) {
            System.runFinalization();
            System.gc();
            callgc = 0;
        }
        
        boolean cached = cache.containsKey(toString(x1, z1));

        if (cached) {
            return cache.remove(toString(x1, z1)).obfuscated;
        }

        File dir = new File(FAntiXRay.getPlugin().getDataFolder() + File.separator + world.getWorld().getName() + File.separator + "r." + (x1 >> 5) + "." + (z1 >> 5));
        if (!dir.exists()) { return null; }

        File file = new File(dir, "r." + x1 + "." + z1 + ".udat");
        if (config.compress_level > 0) {
            file = new File(dir, "r." + x1 + "." + z1 + ".cdat");
        }

        byte[] data = null;

        FileInputStream fis = null;
        ZipInputStream zis = null;
        ObjectInputStream ois = null;

        try {
            if (!file.exists()) { return null; }

            fis = new FileInputStream(file);

            if (config.compress_level > 0) {
                zis = new ZipInputStream(fis);
                zis.getNextEntry();
                ois = new ObjectInputStream(zis);
            } else {
                ois = new ObjectInputStream(fis);
            }

            int engine = ois.readInt();
            if (engine != engine1) {
                return null;
            }

            int x = ois.readInt();
            int z = ois.readInt();

            if (x != x1 || z != z1) {
                return null;
            }

            long hash = ois.readLong();
            if (hash != hash1) {
                return null;
            }

            data = (byte[]) ois.readObject();

            callgc++;
        } catch (IOException ex) {
            return null;
        } catch (ClassNotFoundException ex) {
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
        return data;
    }
    
    public void write(World world, int x, int z, byte[] obfuscated, long hash, int engine) {
        cache.put(toString(x, z), new FCacheData(world, x, z, obfuscated, hash, engine));
    }

    public void write(FCacheData data) {
        FConfiguration config = FAntiXRay.getConfiguration();

        if (callgc >= config.file_call_gc) {
            System.runFinalization();
            System.gc();
            callgc = 0;
        }
        
        File dir = new File(FAntiXRay.getPlugin().getDataFolder() + File.separator + data.world.getWorld().getName() + File.separator + "r." + (data.x >> 5) + "." + (data.z >> 5));
        if (!dir.exists()) { dir.mkdirs(); }

        File file = new File(dir, "r." + data.x + "." + data.z + ".udat");

        if (config.compress_level > 0) {
            file = new File(dir, "r." + data.x + "." + data.z + ".cdat");
        }

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        ZipOutputStream zos = null;
        ZipEntry entry = null;

        try {
            if (!file.exists()) { file.delete(); }
            file.createNewFile(); 

            fos = new FileOutputStream(file);         

            if (config.compress_level > 0) {
                zos = new ZipOutputStream(fos);
                zos.setLevel(config.compress_level);
                entry = new ZipEntry("c." + data.x + "." + data.z + ".chunk");
                zos.putNextEntry(entry);
                oos = new ObjectOutputStream(zos);
            } else {
                oos = new ObjectOutputStream(fos);
            }

            oos.writeInt(data.engine);
            oos.writeInt(data.x);
            oos.writeInt(data.z);
            oos.writeLong(data.hash);
            oos.writeObject(data.obfuscated);

            callgc++;
        } catch (IOException ex) {
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

    private String toString(int x, int z) {
        return (x) + "" +(z);
    }

    public class FCacheData {
        public World world; 
        public int x; 
        public int z; 
        public byte[] obfuscated; 
        public long hash; 
        public int engine;
        
        public FCacheData(World world, int x, int z, byte[] obfuscated, long hash, int engine) {
            this.world = world;
            this.x = x;
            this.z = z;
            this.obfuscated = obfuscated;
            this.hash = hash;
            this.engine = engine;
        }
    }
}
