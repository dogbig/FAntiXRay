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
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import me.FurH.Core.reflection.ReflectionUtils;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.cache.FChunkCache;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import net.minecraft.server.v1_5_R3.Block;
import net.minecraft.server.v1_5_R3.Chunk;
import net.minecraft.server.v1_5_R3.ChunkSection;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.Packet51MapChunk;
import net.minecraft.server.v1_5_R3.Packet56MapChunkBulk;
import net.minecraft.server.v1_5_R3.World;
import org.bukkit.World.Environment;

/**
 *
 * @author FurmigaHumana
 */
public class FObfuscator {

    private static Random rnd = new Random(101);
   
    /* initialize the Packet56MapChunkBulk */
    public static Packet56MapChunkBulk obfuscate(EntityPlayer player, Packet56MapChunkBulk packet) {
        FConfiguration config = FAntiXRay.getConfiguration();

        /* world is disabled, return */
        if (config.disabled_worlds.contains(player.world.getWorld().getName())) {
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
                System.out.println("Null Packet56MapChunk Obfuscation!");
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
    public static Packet51MapChunk obfuscate(EntityPlayer player, Packet51MapChunk packet) {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        /* empty chunk?, return */
        if (packet.d == 0 && packet.c == 0) {
            return packet;
        }

        /* world is disabled, return */
        if (config.disabled_worlds.contains(player.world.getWorld().getName())) {
            return packet;
        }

        Chunk chunk = player.world.getChunkAt(packet.a, packet.b);
        byte[] inflatedBuffer = (byte[]) ReflectionUtils.getPrivateField(packet, "inflatedBuffer");
        byte[] buffer = (byte[]) ReflectionUtils.getPrivateField(packet, "buffer");
        byte[] obfuscated = obfuscate(chunk, inflatedBuffer, packet.e, packet.c, true);

        if (obfuscated == null) {
            System.out.println("Null Packet51MapChunk Obfuscation!");
            return packet;
        }
        
        System.arraycopy(obfuscated, 0, inflatedBuffer, 0, inflatedBuffer.length);
        
        Deflater deflater = new Deflater(0);
        try {
            deflater.setInput(inflatedBuffer, 0, inflatedBuffer.length);
            deflater.finish();
            ReflectionUtils.setPrivateField(packet, "size", deflater.deflate(buffer));
        } finally {
            deflater.end();
        }

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
            hash = getHash(buildBuffer);

            byte[] cached = cache.read(chunk.world.worldData.getName(), chunk.x, chunk.z, hash, config.engine_mode);

            if (cached != null) {
                return cached;
            } else {
                savecache = true;
            }
        }

        boolean nether = chunk.world.getWorld().getEnvironment() == Environment.NETHER;

        ChunkSection[] sections = chunk.i();
        for (int j1 = 0; j1 < sections.length; ++j1) {
            if (sections[j1] != null && (!flag || !sections[j1].isEmpty()) && (i & 1 << j1) != 0) {
                obfuscated = obfuscate(sections[j1], chunk, j1, nether, p51);

                System.arraycopy(obfuscated, 0, buildBuffer, index, obfuscated.length);
                index += obfuscated.length;
            }
        }

        if (savecache) {
            cache.write(chunk.world.worldData.getName(), chunk.x, chunk.z, buildBuffer, hash, config.engine_mode);
        }

        /* might be better for gc */
        //obfuscated = null; sections = null; index = 0;

        return buildBuffer;
    }
    
    /* obfuscated the chunk */
    public static byte[] obfuscate(ChunkSection section, Chunk chunk, int l, boolean nether, boolean p51) {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        byte[] buffer = section.getIdArray().clone();
        
        int incrm = 5;
        int index = 0;
        
        for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
                for (int i = 0; i < 16; i++) {

                    int x = (chunk.x << 4) + i;
                    int y = (l << 4) + j;
                    int z = (chunk.z << 4) + k;

                    int id = section.getTypeId(i, j, k);
                    boolean air = false;

                    if (!p51 && config.cave_enabled && id == 1 && 
                            (y >= 50 && y < 53) || (y >= 40 && y < 43) || j == 15 || i == 1) {
                        
                        if (rnd.nextInt(101) <= config.cave_intensity) {
                            air = true;
                        }
                        
                    }

                    if (p51 && config.cave_enabled && id == 1) {

                        if (rnd.nextInt(101) <= config.cave_intensity * 2) {
                            incrm = rnd.nextInt(5);
                        }

                        if (incrm > 0) {
                            air = true;
                            incrm--;
                        }
                    }

                    if (!p51 && config.proximity_enabled && id == 54) {
                        buffer[index] = 0;
                    } else
                    if (air && !nether && !isBlocksTransparent(chunk, x, y, z)) {
                        buffer[index] = 0;
                    } else
                    if (config.engine_mode == 0) {
                        if (!p51 && isHiddenBlock(id, nether)) {
                            buffer[index] = (byte) (nether ? 87 : 1);
                        } else if (p51 && isToObfuscate(chunk, x, y, z)) {
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
                            if (p51 && !isBlocksTransparent(chunk, x, y, z)) {
                                buffer[index] = (byte) getRandomId(nether);
                            } else {
                                buffer[index] = (byte) (nether ? 87 : 1);
                            }
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

    /* get random item */
    public static int getRandomId(boolean nether) {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        if (nether) {
            return config.random_nether[ (int)(Math.random() * config.random_nether.length) ];
        }
        
        return config.random_world[ (int)(Math.random() * config.random_world.length) ];
    }
    
    public static boolean isToObfuscate(Chunk chunk, int i, int j, int k) {
        FConfiguration config = FAntiXRay.getConfiguration();
        if (config.engine_dark) {
            return !isBlocksInLight(chunk, i, j, k);
        } else {
            return !isBlocksTransparent(chunk, i, j, k);
        }
    }

    /* return true if the block have light in one of its faces */
    private static boolean isBlocksInLight(Chunk chunk, int i, int j, int k) {
        return isBlocksInLight(chunk.world, i, j, k);
    }
    
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
        if (isTransparent(chunk.world.getTypeId(i + 1, j, k))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(i - 1, j, k))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(i, j + 1, k))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(i, j - 1, k))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(i, j, k + 1))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(i, j, k - 1))) {
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
    
    public static long getHash(byte[] buildBuffer) {
        CRC32 checksum = new CRC32();
        checksum.reset();
        checksum.update(buildBuffer, 0, buildBuffer.length);
        return checksum.getValue();
    }
}