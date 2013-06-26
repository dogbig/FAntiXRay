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

package me.FurH.FAntiXRay.obfuscation;

import java.util.Random;
import java.util.zip.Deflater;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.reflection.ReflectionUtils;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.cache.FCRC32;
import me.FurH.FAntiXRay.cache.FChunkCache;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.timings.FTimingsCore;
import net.minecraft.server.v1_5_R3.Block;
import net.minecraft.server.v1_5_R3.Chunk;
import net.minecraft.server.v1_5_R3.ChunkSection;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.Packet51MapChunk;
import net.minecraft.server.v1_5_R3.Packet56MapChunkBulk;
import net.minecraft.server.v1_5_R3.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FObfuscator {

    public static final FTimingsCore obfuscator = new FTimingsCore("FAntiXRay Obfuscation");
    private static FTimingsCore obfuscator_cached = new FTimingsCore("FAntiXRay: Cached Obfuscation", obfuscator);
    private static FTimingsCore obfuscator_uncached = new FTimingsCore("FAntiXRay: Uncached Obfuscation", obfuscator);
    
    private static Random rnd = new Random(101);
    
    public static Object obfuscate(Player player, Object object) {
        
        if (object instanceof Packet56MapChunkBulk) {
            try {
                return obfuscate(((CraftPlayer)player).getHandle(), (Packet56MapChunkBulk) object);
            } catch (CoreException ex) {
                ex.printStackTrace();
            }
        } else
        if (object instanceof Packet51MapChunk) {
            try {
                return obfuscate(((CraftPlayer)player).getHandle(), (Packet51MapChunk) object);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }
   
    /* initialize the Packet56MapChunkBulk */
    public static Packet56MapChunkBulk obfuscate(EntityPlayer player, Packet56MapChunkBulk packet) throws CoreException {
        FConfiguration config = FAntiXRay.getConfiguration();

        /* world is disabled, return */
        if (config.disabled_worlds.contains(player.world.getWorld().getName())) {
            return packet;
        }

        if (FAntiXRay.isExempt(player.name)) {
            return packet;
        }
        
        int[] c = (int[]) ReflectionUtils.getPrivateField(packet, "c"); //X
        int[] d = (int[]) ReflectionUtils.getPrivateField(packet, "d"); //Z

        byte[][] inflatedBuffers = (byte[][]) ReflectionUtils.getPrivateField(packet, "inflatedBuffers");
        byte[] buildBuffer = (byte[]) ReflectionUtils.getPrivateField(packet, "buildBuffer");
        byte[] obfuscated; // obfuscated data

        /* spigot compatibility */
        if (buildBuffer == null) {
            buildBuffer = new byte [ 196864 ];
        }

        int index = 0;
        for (int i = 0; i < packet.d(); i++) {
            Chunk chunk = player.world.getChunkAt(c[i], d[i]);

            obfuscated = obfuscate(chunk, inflatedBuffers[i], true, packet.a[i], false);

            if (obfuscated == null) {
                return packet;
            }

            /* spigot compatibility */
            if (obfuscated.length + index > buildBuffer.length) {
                buildBuffer = new byte [ obfuscated.length + index ];
            }

            System.arraycopy(obfuscated, 0, buildBuffer, index, inflatedBuffers[i].length);
            index += inflatedBuffers[i].length;
        }

        /* might be better for gc */
        inflatedBuffers = null; buildBuffer = null; obfuscated = null; 
        index = 0; //packet.chunks.clear(); packet.chunks = null;

        /* return the obfuscated packet */
        return packet;
    }

    /* initialize the Packet51MapChunk */
    public static Packet51MapChunk obfuscate(EntityPlayer player, Packet51MapChunk packet) throws CoreException {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        /* empty chunk?, return */
        if (packet.d == 0 && packet.c == 0) {
            return packet;
        }

        /* world is disabled, return */
        if (config.disabled_worlds.contains(player.world.getWorld().getName())) {
            return packet;
        }
        

        if (FAntiXRay.isExempt(player.name)) {
            return packet;
        }

        Chunk chunk = player.world.getChunkAt(packet.a, packet.b);
        byte[] inflatedBuffer = (byte[]) ReflectionUtils.getPrivateField(packet, "inflatedBuffer");
        byte[] buffer = (byte[]) ReflectionUtils.getPrivateField(packet, "buffer");
        byte[] obfuscated = obfuscate(chunk, inflatedBuffer, packet.e, packet.c, true);
        int size0 = (Integer) ReflectionUtils.getPrivateField(packet, "size");

        if (obfuscated == null) {
            return packet;
        }

        System.arraycopy(obfuscated, 0, inflatedBuffer, 0, inflatedBuffer.length);

        Deflater deflater = new Deflater(-1);

        try {

            deflater.setInput(inflatedBuffer, 0, inflatedBuffer.length);
            deflater.finish();

            buffer = new byte[ inflatedBuffer.length ];
            int size = deflater.deflate(buffer);

            ReflectionUtils.setPrivateField(packet, "size", size);
            ReflectionUtils.setPrivateField(packet, "buffer", buffer);

        } finally {
            deflater.end();
        }

        System.arraycopy(obfuscated, 0, inflatedBuffer, 0, inflatedBuffer.length);
        
        inflatedBuffer = null; buffer = null; obfuscated = null;

        return packet;
    }

    /* initialize the chunk */
    public static byte[] obfuscate(Chunk chunk, byte[] buildBuffer, boolean flag, int i, boolean p51) {

        FConfiguration config = FAntiXRay.getConfiguration();
        FChunkCache cache = FAntiXRay.getCache();

        boolean savecache = false;
        long hash = 0L;

        int index = 0;
        byte[] obfuscated = null;

        if (config.cache_enabled) {
            obfuscator_cached.start();

            hash = getHash(buildBuffer);
            byte[] cached = cache.read(chunk.world.worldData.getName(), cacheKey(chunk.x, chunk.z), hash, config.engine_mode);

            obfuscator_cached.stop();
            if (cached != null) {
                return cached;
            } else {
                savecache = true;
            }
        }
        
        obfuscator_uncached.start();

        boolean nether = chunk.world.getWorld().getEnvironment() == Environment.NETHER;

        ChunkSection[] sections = chunk.i();
        for (int j1 = 0; j1 < sections.length; ++j1) {
            if (sections[j1] != null && (!flag || !sections[j1].isEmpty()) && (i & 1 << j1) != 0) {
                obfuscated = obfuscate(sections[j1], chunk, j1, nether);

                System.arraycopy(obfuscated, 0, buildBuffer, index, obfuscated.length);
                index += obfuscated.length;
            }
        }

        if (savecache) {
            cache.write(chunk.world.worldData.getName(), cacheKey(chunk.x, chunk.z), buildBuffer, hash, config.engine_mode);
        }
        
        obfuscator_uncached.stop();

        return buildBuffer;
    }
    
    /* obfuscated the chunk */
    public static byte[] obfuscate(ChunkSection section, Chunk chunk, int l, boolean nether) {
        FConfiguration config = FAntiXRay.getConfiguration();

        byte[] buffer = section.getIdArray().clone();
        int index = 0;

        for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
                for (int i = 0; i < 16; i++) {

                    int x = (chunk.x << 4) + i;
                    int y = (l << 4) + j;
                    int z = (chunk.z << 4) + k;

                    int id = section.getTypeId(i, j, k);
                    boolean air = false;

                    if (config.cave_enabled && id == 1 && 
                            (y >= 50 && y < 53) || (y >= 40 && y < 43) || j == 15 || i == 1) {
                        if (rnd.nextInt(101) <= config.cave_intensity) {
                            air = true;
                        }
                    }

                    if (config.engine_chest && id == 54) {
                        buffer[index] = 0;
                    } else
                    if (air && !nether && !isBlocksTransparent(chunk, x, y, z)) {
                        buffer[index] = 0;
                    } else
                    if (config.engine_mode == 0) {
                        if (isHiddenBlock(id, nether)) {
                            buffer[index] = (byte) (nether ? 87 : 1);
                        }
                    } else
                    if (config.engine_mode == 1) {
                        if (isHiddenBlock(id, nether)) {
                            if (isToObfuscate(chunk, x, y, z)) {
                                buffer[index] = (byte) (nether ? 87 : 1);
                            }
                        }
                    } else
                    if (config.engine_mode == 2) {
                        if (isObfuscable(id, nether)) {
                            if (!isBlocksTransparent(chunk, x, y, z)) {
                                buffer[index] = (byte) getRandomId(nether);
                            }
                        }
                    } else
                    if (config.engine_mode == 3) {
                        if (isObfuscable(id, nether)) {
                            if (rnd.nextInt(101) <= 20) {
                                if (!isBlocksTransparent(chunk, x, y, z)) {
                                    buffer[index] = (byte) getRandomId(nether);
                                }
                            }
                        }
                    } else
                    if (config.engine_mode == 4) {
                        if (isObfuscable(id, nether)) {
                            if (rnd.nextInt(101) <= 20) {
                                if (!isBlocksTransparent(chunk, x, y, z)) {
                                    buffer[index] = (byte) getRandomId(nether);
                                }
                            }
                        } else if (isHiddenBlock(id, nether)) {
                            buffer[index] = (byte) (nether ? 87 : 1);
                        }
                    }
                    
                    index++;
                }
            }
        }
        return buffer;
    }
    
    public static boolean isHiddenBlock(int id, boolean nether) {
        FConfiguration config = FAntiXRay.getConfiguration();

        if (nether) {
            return config.hidden_nether.contains(id);
        }

        return config.hidden_world.contains(id);
    }
    
    private static int index_nether = -1;
    private static int index_world = -1;

    /* get random item */
    public static int getRandomId(boolean nether) {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        if (nether) {
            
            index_nether++;
            
            if (index_nether >= config.random_nether.length) {
                index_nether = 0;
            }
            
            return config.random_nether[ index_nether ];
        }
        
        index_world++;
        
        if (index_world >= config.random_world.length) {
            index_world = 0;
        }
        
        return config.random_world[ index_world ];
    }
    
    private static boolean isToObfuscate(Chunk chunk, int i, int j, int k) {
        return isToObfuscate(chunk.world, i, j, k);
    }
    
    public static boolean isToObfuscate(World world, int i, int j, int k) {
        FConfiguration config = FAntiXRay.getConfiguration();
        if (config.engine_dark) {
            return !isBlocksInLight(world, i, j, k);
        } else {
            return !isBlocksTransparent(world, i, j, k);
        }
    }

    /* return true if the block have light in one of its faces */
    public static boolean isBlocksInLight(World world, int i, int j, int k) {
        if (world.getLightLevel(i + 1, j, k) > 0) {
            return true;
        } else
        if (world.getLightLevel(i - 1, j, k) > 0) {
            return true;
        } else
        if (world.getLightLevel(i, j + 1, k) > 0) {
            return true;
        } else
        if (world.getLightLevel(i, j - 1, k) > 0) {
            return true;
        } else
        if (world.getLightLevel(i, j, k + 1) > 0) {
            return true;
        } else
        if (world.getLightLevel(i, j, k - 1) > 0) {
            return true;
        }
        return false;
    }

    /* return true if the block have a transparent block in one of its faces */
    private static boolean isBlocksTransparent(Chunk chunk, int i, int j, int k) {
        return isBlocksTransparent(chunk.world, i, j, k);
    }
    
    public static boolean isBlocksTransparent(World world, int i, int j, int k) {
        if (isTransparent(world.getTypeId(i + 1, j, k))) {
            return true;
        } else
        if (isTransparent(world.getTypeId(i - 1, j, k))) {
            return true;
        } else
        if (isTransparent(world.getTypeId(i, j + 1, k))) {
            return true;
        } else
        if (isTransparent(world.getTypeId(i, j - 1, k))) {
            return true;
        } else
        if (isTransparent(world.getTypeId(i, j, k + 1))) {
            return true;
        } else
        if (isTransparent(world.getTypeId(i, j, k - 1))) {
            return true;
        }
        return false;
    }

    /* return true if it is a obfuscable id */
    public static boolean isObfuscable(int id, boolean nether) {

        if (id == 1) {
            return true;
        }
        
        if (nether && id == 87) {
            return true;
        }
        
        return false;
    }
    
    /* return true if the id is a transparent block */
    public static boolean isTransparent(int id) {

        if (id == 0) {
            return true;
        }

        if (id == 1) {
            return false;
        }

        if (id == 12 || id == 13) {
            return true;
        }

        if (id == 8 || id == 9 || id == 10 || id == 11) {
            return true;
        }

        if (id == 87 || id == 112) {
            return false;
        }

        return !Block.l(id);
    }
    
    public static String toString(int x, int y, int z) {
        return (x) + "" + (y) + "" + (z);
    }

    private static long cacheKey(long x, long z) {
        return x & 4294967295L | (z & 4294967295L) << 32;
    }
    
    public static long getHash(byte[] buildBuffer) {
        return FCRC32.getHash(buildBuffer);
    }
}