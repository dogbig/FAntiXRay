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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.zip.Deflater;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.cache.FChunkCache;
import me.FurH.FAntiXRay.util.FReflectField;
import me.FurH.FAntiXRay.util.FUtils;
import net.minecraft.server.v1_4_R1.Chunk;
import net.minecraft.server.v1_4_R1.ChunkSection;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.Packet51MapChunk;
import net.minecraft.server.v1_4_R1.Packet56MapChunkBulk;
import net.minecraft.server.v1_4_R1.World;

/**
 *
 * @author FurmigaHumana
 */
public class FObfuscator {
    public static Integer[] random_blocks = new Integer[] { 5, 15, 48, 56 };
    public static HashSet<Integer> hidden_blocks = new HashSet<Integer>(Arrays.asList(new Integer[] { 14, 15, 16, 21, 56, 73, 74, 129 }));
    public static HashSet<String> disabled_worlds = new HashSet<String>();

    public static int engine_mode = 0;
    public static boolean dark_enabled = false;
    public static boolean caves_enabled = false;
    public static int caves_intensity = 50;
    private static Random rnd = new Random(101);

    public static boolean chest_enabled = false;
    
    public static boolean cache_enabled = false;

    /* load the configurations */
    public static void load(Integer[] random_blocks, HashSet<Integer> hidden_blocks, HashSet<String> disabled_worlds, HashSet<Integer> dark_extra, int engine_mode, boolean dark_enabled, boolean caves_enabled, int caves_intensity, boolean chest_enabled, boolean cache_enabled) {        
        FObfuscator.random_blocks = random_blocks;
        FObfuscator.hidden_blocks = hidden_blocks;
        FObfuscator.disabled_worlds = disabled_worlds;
        hidden_blocks.addAll(dark_extra);

        FObfuscator.engine_mode = engine_mode;        
        FObfuscator.dark_enabled = dark_enabled;
        FObfuscator.caves_enabled = caves_enabled;
        FObfuscator.caves_intensity = caves_intensity;
        FObfuscator.chest_enabled = chest_enabled;
        FObfuscator.cache_enabled = cache_enabled;
    }

    /* initialize the Packet56MapChunkBulk */
    public static Packet56MapChunkBulk obfuscate(EntityPlayer player, Packet56MapChunkBulk packet) {
        
        /* world is disabled, return */
        if (disabled_worlds.contains(player.world.getWorld().getName())) {
            return packet;
        }

        int[] c = (int[]) FReflectField.getPrivateField(packet, "c"); //X
        int[] d = (int[]) FReflectField.getPrivateField(packet, "d"); //Z

        byte[][] inflatedBuffers = (byte[][]) FReflectField.getPrivateField(packet, "inflatedBuffers");
        byte[] buildBuffer = (byte[]) FReflectField.getPrivateField(packet, "buildBuffer");
        byte[] obfuscated; // obfuscated data
        
        /* spigot compatibility */
        if (buildBuffer == null) {
            buildBuffer = new byte [ 196864 ];
        }

        int index = 0;
        for (int i = 0; i < packet.d(); i++) {
            Chunk chunk = player.world.getChunkAt(c[i], d[i]);

            obfuscated = obfuscate(chunk, inflatedBuffers[i], true, packet.a[i]);

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
        
        /* empty chunk?, return */
        if (packet.d == 0 && packet.c == 0) {
            return packet;
        }

        /* world is disabled, return */
        if (disabled_worlds.contains(player.world.getWorld().getName())) {
            return packet;
        }

        Chunk chunk = player.world.getChunkAt(packet.a, packet.b);
        byte[] inflatedBuffer = (byte[]) FReflectField.getPrivateField(packet, "inflatedBuffer");
        byte[] buffer = (byte[]) FReflectField.getPrivateField(packet, "buffer");
        byte[] obfuscated = obfuscate(chunk, inflatedBuffer, packet.e, packet.c);

        System.arraycopy(obfuscated, 0, inflatedBuffer, 0, inflatedBuffer.length);
        
        Deflater deflater = new Deflater(-1);
        try {
            deflater.setInput(inflatedBuffer, 0, inflatedBuffer.length);
            deflater.finish();
            FReflectField.setPrivateField(packet, "size", deflater.deflate(buffer));
        } finally {
            deflater.end();
        }
        
        inflatedBuffer = null; buffer = null; obfuscated = null;

        return packet;
    }

    /* initialize the chunk */
    public static byte[] obfuscate(Chunk chunk, byte[] buildBuffer, boolean flag, int i) {
        
        FChunkCache cache = FAntiXRay.getCache();
        boolean savecache = false;
        long hash = 0L;

        int index = 0;
        byte[] obfuscated = null;
        
        if (cache_enabled) {
            hash = FUtils.getHash(buildBuffer);

            byte[] cached = cache.read(chunk.world, chunk.x, chunk.z, hash, engine_mode);
            if (cached != null) {
                return cached;
            } else {
                savecache = true;
            }
        }

        ChunkSection[] sections = chunk.i();
        for (int j1 = 0; j1 < sections.length; ++j1) {
            if (sections[j1] != null && (!flag || !sections[j1].a()) && (i & 1 << j1) != 0) {
                obfuscated = obfuscate(sections[j1], chunk, j1);

                System.arraycopy(obfuscated, 0, buildBuffer, index, obfuscated.length);
                index += obfuscated.length;
            }
        }

        if (savecache) {
            cache.write(chunk.world, chunk.x, chunk.z, buildBuffer, hash, engine_mode);
        }

        /* might be better for gc */
        obfuscated = null; sections = null; index = 0;

        return buildBuffer;
    }
    
    /* obfuscated the chunk */
    public static byte[] obfuscate(ChunkSection section, Chunk chunk, int l) {
        byte[] buffer = section.g().clone();

        int incrm = 5;
        int index = 0;
        
        for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
                for (int i = 0; i < 16; i++) {

                    int x = (chunk.x << 4) + i;
                    int y = (l << 4) + j;
                    int z = (chunk.z << 4) + k;

                    int id = section.a(i, j, k);
                    boolean air = false;

                    if (caves_enabled && id == 1) {
                        if (rnd.nextInt(1001) <= caves_intensity) {
                            incrm = rnd.nextInt(5);
                        }

                        if (incrm > 0) {
                            air = true;
                            incrm--;
                        }
                    }

                    if (chest_enabled && id == 54) {
                        buffer[index] = 0;
                    } else
                    if (air) {
                        if (isToObfuscate(chunk, x, y, z)) {
                            buffer[index] = 0;
                        }
                    } else
                    if (engine_mode == 0) {
                        if (hidden_blocks.contains(id)) {
                            buffer[index] = 1;
                        }
                    } else
                    if (engine_mode == 1) {
                        if (hidden_blocks.contains(id)) {
                            if (isToObfuscate(chunk, x, y, z)) {
                                buffer[index] = 1;
                            }
                        }
                    } else
                    if (engine_mode == 2) {
                        if (isObfuscable(id)) {
                            if (id == 1) {
                                if (!isBlocksTransparent(chunk, x, y, z)) {
                                    buffer[index] = (byte) getRandom();
                                }
                            } else
                            if (isToObfuscate(chunk, x, y, z)) {
                                buffer[index] = 1;
                            }
                        }
                    } else
                    if (engine_mode == 3) {
                        if (isObfuscable(id)) {
                            if (id == 1) {
                                if (rnd.nextInt(101) <= 20) {
                                    if (!isBlocksTransparent(chunk, x, y, z)) {
                                        buffer[index] = (byte) getRandom();
                                    }
                                }
                            } else
                            if (isToObfuscate(chunk, x, y, z)) {
                                buffer[index] = 1;
                            }
                        }
                    }
                    index++;
                }
            }
        }
        return buffer;
    }

    /* get random item */
    public static int getRandom() {
        int random = ((int)(Math.random() * random_blocks.length));
        return random_blocks[ random ];
    }
    
    public static boolean isToObfuscate(Chunk chunk, int i, int j, int k) {
        if (dark_enabled) {
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
    public static boolean isObfuscable(int id) {
        if (id == 1) {
            return true;
        }

        return hidden_blocks.contains(id);
    }
    
    /* return true if the id is a transparent block */
    public static boolean isTransparent(int id) {
        if (id == 0) {
            return true;
        }
        
        if (id == 1) {
            return false;
        }
        
        return !net.minecraft.server.v1_4_R1.Block.i(id);
    }
    
    public static String toString(int x, int y, int z) {
        return (x) + "" + (y) + "" + (z);
    }
}