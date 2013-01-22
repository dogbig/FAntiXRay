package me.FurH.server.FAntiXRay;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

/**
 *
 * @author FurmigaHumana
 */
public class FAntiXRay {
    public static Integer[] random_blocks = new Integer[] { 5, 15, 48, 56 };
    public static HashSet<Integer> hidden_blocks = new HashSet<Integer>(Arrays.asList(new Integer[] { 14, 15, 16, 21, 56, 73, 74, 129 }));
    public static HashSet<String> disabled_worlds = new HashSet<String>();

    public static int engine_mode = 0;
    public static boolean dark_enabled = false;
    public static boolean caves_enabled = false;
    public static int caves_intensity = 50;
    private static Random rnd = new Random(101);

    /* load the configurations */
    public static void load(Integer[] random_blocks, HashSet<Integer> hidden_blocks, HashSet<String> disabled_worlds, HashSet<Integer> dark_extra, int engine_mode, boolean dark_enabled, boolean caves_enabled, int caves_intensity) {        
        FAntiXRay.random_blocks = random_blocks;
        FAntiXRay.hidden_blocks = hidden_blocks;
        FAntiXRay.disabled_worlds = disabled_worlds;
        hidden_blocks.addAll(dark_extra);

        FAntiXRay.engine_mode = engine_mode;        
        FAntiXRay.dark_enabled = dark_enabled;
        FAntiXRay.caves_enabled = caves_enabled;
        FAntiXRay.caves_intensity = caves_intensity;
    }

    /* initialize the Packet56MapChunkBulk */
    public static Packet56MapChunkBulk obfuscate(Packet56MapChunkBulk packet) {

        /* the packet will not be obfuscated, return */
        if (!packet.obfuscate) {
            return packet;
        }

        /* who sent this packet?, return */
        if (packet.chunks == null) {
            return packet;
        }

        /* no chunks to obfuscate, return */
        if (packet.chunks.isEmpty()) {
            return packet;
        }

        /* world is disabled, return */
        if (disabled_worlds.contains(((Chunk)packet.chunks.get(0)).world.getWorld().getName())) {
            return packet;
        }

        byte[][] inflatedBuffers = (byte[][]) FUtils.getPrivateField(packet, "inflatedBuffers");
        byte[] buildBuffer = (byte[]) FUtils.getPrivateField(packet, "buildBuffer");
        byte[] obfuscated; // obfuscated data

        int index = 0;
        for (int i = 0; i < packet.chunks.size(); i++) {
            Chunk chunk = (Chunk)packet.chunks.get(i);

            obfuscated = obfuscate(chunk, inflatedBuffers[i], true, '\uffff');

            System.arraycopy(obfuscated, 0, buildBuffer, index, inflatedBuffers[i].length);
            index += inflatedBuffers[i].length;
        }

        /* might be better for gc */
        inflatedBuffers = null; buildBuffer = null; obfuscated = null; 
        index = 0; //packet.chunks.clear(); packet.chunks = null;

        /* return the obfuscated packet */
        return packet;
    }
    
    public static Packet51MapChunk obfuscate(Packet51MapChunk packet) {

        /* empty chunk?, return */
        if (packet.d == 0 && packet.c == 0) {
            return packet;
        }
        
        /* who sent this packet?, return */
        if (packet.chunk == null) {
            return packet;
        }

        /* world is disabled, return */
        if (disabled_worlds.contains(packet.chunk.world.getWorld().getName())) {
            return packet;
        }

        byte[] inflatedBuffer = (byte[]) FUtils.getPrivateField(packet, "inflatedBuffer");
        byte[] obfuscated = obfuscate(packet.chunk, inflatedBuffer, packet.e, packet.i);

        System.arraycopy(obfuscated, 0, inflatedBuffer, 0, inflatedBuffer.length);
        packet.compress();

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
                    int wy = (section.d()) + y;
                    int wz = (chunk.z << 4) + z;

                    int id = section.a(x, y, z);
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
                            if (isToObfuscate(chunk, wx, wy, wz)) {
                                if (id == 1) {
                                    buffer[index] = (byte) getRandom();
                                } else {
                                    buffer[index] = 1;
                                }
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
    
    private static boolean isToObfuscate(Chunk chunk, int x, int y, int z) {
        if (dark_enabled) {
            return !isBlocksInLight(chunk, x, y, z);
        } else {
            return !isBlocksTransparent(chunk, x, y, z);
        }
    }

    /* return true if the block have light in one of its faces */
    private static boolean isBlocksInLight(Chunk chunk, int x, int y, int z) {
        if (chunk.world.getLightLevel(x + 1, y, z) > 0) {
            return true;
        } else
        if (chunk.world.getLightLevel(x - 1, y, z) > 0) {
            return true;
        } else
        if (chunk.world.getLightLevel(x, y + 1, z) > 0) {
            return true;
        } else
        if (chunk.world.getLightLevel(x, y - 1, z) > 0) {
            return true;
        } else
        if (chunk.world.getLightLevel(x, y, z + 1) > 0) {
            return true;
        } else
        if (chunk.world.getLightLevel(x, y, z - 1) > 0) {
            return true;
        }
        return false;
    }

    /* return true if the block have a transparent block in one of its faces */
    private static boolean isBlocksTransparent(Chunk chunk, int x, int y, int z) {
        if (FUtils.isTransparent(chunk.world.getTypeId(x + 1, y, z))) {
            return true;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x - 1, y, z))) {
            return true;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x, y + 1, z))) {
            return true;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x, y - 1, z))) {
            return true;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x, y, z + 1))) {
            return true;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x, y, z - 1))) {
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
}