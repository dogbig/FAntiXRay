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
import me.FurH.FAntiXRay.FAntiXRay;
import net.minecraft.server.World;

/**
 *
 * @author FurmigaHumana
 */
public class FChunkCache {

    public byte[] read(World world, int x1, int z1, long hash1, int engine1) {        
        File dir = new File(FAntiXRay.getPlugin().getDataFolder() + File.separator + world.getWorld().getName());
        if (!dir.exists()) { return null; }
        
        File file = new File(dir, "r." + x1 + "." + z1 + ".fx");
        byte[] data = null;
        
        try {
            if (!file.exists()) { return null; }

            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

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

            fis.close();
            ois.close();
        } catch (IOException | ClassNotFoundException ex) {
            FAntiXRay.getCommunicator().error("[TAG] Error while reading the data file: {0}", ex, ex.getMessage());
        }
        return data;
    }
    
    public void write(World world, int x, int z, byte[] obfuscated, long hash, int engine) {        
        File dir = new File(FAntiXRay.getPlugin().getDataFolder() + File.separator + world.getWorld().getName());
        if (!dir.exists()) { dir.mkdirs(); }

        File file = new File(dir, "r." + x + "." + z + ".fx");

        try {
            if (!file.exists()) { 
                file.createNewFile(); 
            } else {
                file.delete();
                file.createNewFile(); 
            }

            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeInt(engine);
            oos.writeInt(x);
            oos.writeInt(z);
            oos.writeLong(hash);
            oos.writeObject(obfuscated);

            oos.close();
            fos.close();
        } catch (IOException ex) {
            FAntiXRay.getCommunicator().error("[TAG] Error while writing the data file: {0}", ex, ex.getMessage());
        }
    }
}