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

package me.FurH.FAntiXRay.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import net.minecraft.server.v1_4_6.World;

/**
 *
 * @author FurmigaHumana
 */
public class FChunkCache {
    private int files = 0;

    public byte[] read(World world, int x1, int z1, long hash1, int engine1) {
        files++;

        if (files >= 100) {
            System.gc();
            files = 0;
        }

        File dir = new File(FAntiXRay.getPlugin().getDataFolder() + File.separator + world.getWorld().getName() + File.separator + "r." + (x1 >> 5) + "." + (z1 >> 5));
        FConfiguration config = FAntiXRay.getConfiguration();
        if (!dir.exists()) { return null; }

        File file = new File(dir, "r." + x1 + "." + z1 + ".udat");

        if (config.compress_level > 0) {
            file = new File(dir, "r." + x1 + "." + z1 + ".cdat");
        }

        byte[] data = null;

        try {
            if (!file.exists()) { return null; }
            
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = null;
            ObjectInputStream ois = null;
        
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

            if (zis != null) {
                zis.closeEntry();
                zis.close();
            }

            ois.close();
            fis.close();
            
            ois = null;
            zis = null;
            fis = null;
        } catch (IOException | ClassNotFoundException ex) {
            FAntiXRay.getCommunicator().error("[TAG] Error while reading the data file: {0}", ex, ex.getMessage());
        }
        return data;
    }
    
    public void write(World world, int x, int z, byte[] obfuscated, long hash, int engine) {
        files++;

        if (files >= 100) {
            System.gc();
            files = 0;
        }
        
        File dir = new File(FAntiXRay.getPlugin().getDataFolder() + File.separator + world.getWorld().getName() + File.separator + "r." + (x >> 5) + "." + (z >> 5));
        FConfiguration config = FAntiXRay.getConfiguration();
        if (!dir.exists()) { dir.mkdirs(); }

        File file = new File(dir, "r." + x + "." + z + ".udat");

        if (config.compress_level > 0) {
            file = new File(dir, "r." + x + "." + z + ".cdat");
        }        

        try {
            if (!file.createNewFile()) { 
                file.delete();
                file.createNewFile(); 
            }

            FileOutputStream fos = new FileOutputStream(file);         
            ObjectOutputStream oos = null;
            ZipOutputStream zos = null;
        
            if (config.compress_level > 0) {
                zos = new ZipOutputStream(fos);
                zos.setLevel(config.compress_level);
                zos.putNextEntry(new ZipEntry("c." + x + "." + z + ".chunk"));
                oos = new ObjectOutputStream(zos);
            } else {
                oos = new ObjectOutputStream(fos);
            }

            oos.writeInt(engine);
            oos.writeInt(x);
            oos.writeInt(z);
            oos.writeLong(hash);
            oos.writeObject(obfuscated);

            if (zos != null) { 
                zos.closeEntry();
                zos.close(); 
            }
            
            oos.close();
            fos.close();
            
            oos = null;
            zos = null;
            fos = null;
        } catch (IOException ex) {
            FAntiXRay.getCommunicator().error("[TAG] Error while writing the data file: {0}", ex, ex.getMessage());
        }
    }
}