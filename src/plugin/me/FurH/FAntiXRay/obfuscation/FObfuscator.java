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
import me.FurH.FAntiXRay.util.FReflectField;
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
    public static HashSet<Integer> hidden_blocks = new HashSet<>(Arrays.asList(new Integer[] { 14, 15, 16, 21, 56, 73, 74, 129 }));
    public static HashSet<String> disabled_worlds = new HashSet<>();

    public static int engine_mode = 0;
    public static boolean dark_enabled = false;
    public static boolean caves_enabled = false;
    public static int caves_intensity = 50;
    private static Random rnd = new Random(101);

    public static boolean chest_enabled = false;
    
    /* load the configurations */
    public static void load(Integer[] random_blocks, HashSet<Integer> hidden_blocks, HashSet<String> disabled_worlds, HashSet<Integer> dark_extra, int engine_mode, boolean dark_enabled, boolean caves_enabled, int caves_intensity, boolean chest_enabled) {        
        FObfuscator.random_blocks = random_blocks;
        FObfuscator.hidden_blocks = hidden_blocks;
        FObfuscator.disabled_worlds = disabled_worlds;
        hidden_blocks.addAll(dark_extra);

        FObfuscator.engine_mode = engine_mode;        
        FObfuscator.dark_enabled = dark_enabled;
        FObfuscator.caves_enabled = caves_enabled;
        FObfuscator.caves_intensity = caves_intensity;
        FObfuscator.chest_enabled = chest_enabled;
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
        
        int index = 0;
        byte[] obfuscated;

        ChunkSection[] sections = chunk.i();
        for (int j1 = 0; j1 < sections.length; ++j1) {
            if (sections[j1] != null && (!flag || !sections[j1].a()) && (i & 1 << j1) != 0) {
                obfuscated = obfuscate(sections[j1], chunk, j1);

                System.arraycopy(obfuscated, 0, buildBuffer, index, obfuscated.length);
                index += obfuscated.length;
            }
        }

        /* might be better for gc */
        obfuscated = null; sections = null; index = 0;

        return buildBuffer;
    }

    /* obfuscated the chunk */
    public static byte[] obfuscate(ChunkSection section, Chunk chunk, int i) {
        byte[] buffer = section.g().clone();

        int incrm = 5;
        int index = 0;
        
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {

                    int wx = (chunk.x << 4) + x;
                    int wy = (i << 4) + y;
                    int wz = (chunk.z << 4) + z;

                    int id = section.a(x, y, z);
                    boolean air = false;
                    
                    if (chest_enabled && id == 54) {
                        buffer[index] = 0;
                        index++;
                        continue;
                    }
                    
                    if (caves_enabled && id == 1) {
                        if (rnd.nextInt(1001) <= caves_intensity) {
                            incrm = rnd.nextInt(5);
                        }

                        if (incrm > 0) {
                            air = true;
                            incrm--;
                        }
                    }

                    if (air) {
                        if (isToObfuscate(chunk, wx, wy, wz)) {
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
                            if (isToObfuscate(chunk, wx, wy, wz)) {
                                buffer[index] = 1;
                            }
                        }
                    } else
                    if (engine_mode == 2) {
                        if (isObfuscable(id)) {
                            if (id == 1) {
                                if (!isBlocksTransparent(chunk, wx, wy, wz)) {
                                    buffer[index] = (byte) getRandom();
                                }
                            } else
                            if (isToObfuscate(chunk, wx, wy, wz)) {
                                buffer[index] = 1;
                            }
                        }
                    } else
                    if (engine_mode == 3) {
                        if (isObfuscable(id)) {
                            if (id == 1) {
                                if (rnd.nextInt(101) <= 20) {
                                    if (!isBlocksTransparent(chunk, wx, wy, wz)) {
                                        buffer[index] = (byte) getRandom();
                                    }
                                }
                            } else
                            if (isToObfuscate(chunk, wx, wy, wz)) {
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
    
    public static boolean isToObfuscate(Chunk chunk, int x, int y, int z) {
        if (dark_enabled) {
            return !isBlocksInLight(chunk, x, y, z);
        } else {
            return !isBlocksTransparent(chunk, x, y, z);
        }
    }

    /* return true if the block have light in one of its faces */
    private static boolean isBlocksInLight(Chunk chunk, int x, int y, int z) {
        return isBlocksInLight(chunk.world, x, y, z);
    }
    
    public static boolean isBlocksInLight(World world, int x, int y, int z) {
        if (world.getLightLevel(x + 1, y, z) > 0) {
            return true;
        } else
        if (world.getLightLevel(x - 1, y, z) > 0) {
            return true;
        } else
        if (world.getLightLevel(x, y + 1, z) > 0) {
            return true;
        } else
        if (world.getLightLevel(x, y - 1, z) > 0) {
            return true;
        } else
        if (world.getLightLevel(x, y, z + 1) > 0) {
            return true;
        } else
        if (world.getLightLevel(x, y, z - 1) > 0) {
            return true;
        }
        return false;
    }

    /* return true if the block have a transparent block in one of its faces */
    private static boolean isBlocksTransparent(Chunk chunk, int x, int y, int z) {
        if (isTransparent(chunk.world.getTypeId(x + 1, y, z))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(x - 1, y, z))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(x, y + 1, z))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(x, y - 1, z))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(x, y, z + 1))) {
            return true;
        } else
        if (isTransparent(chunk.world.getTypeId(x, y, z - 1))) {
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