package me.FurH.server.FAntiXRay;

import java.util.Arrays;
import java.util.HashSet;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.Packet56MapChunkBulk;

/**
 *
 * @author FurmigaHumana
 */
public class FAntiXRay {
    public static Integer[] random_blocks = new Integer[] { 5, 15, 48, 56 };
    public static HashSet<Integer> hidden_blocks = new HashSet<Integer>(Arrays.asList(new Integer[] { 14, 15, 16, 21, 56, 73, 74, 129 }));
    public static HashSet<String> disabled_worlds = new HashSet<String>();
    public static HashSet<Integer> dark_blocks = new HashSet<Integer>();

    public static int engine_mode = 0;
    public static boolean dark_enabled = false;

    /* load the configurations */
    public static void load(Integer[] random_blocks, HashSet<Integer> hidden_blocks, HashSet<String> disabled_worlds, HashSet<Integer> dark_extra, int engine_mode, boolean dark_enabled) {        
        FAntiXRay.random_blocks = random_blocks;
        FAntiXRay.hidden_blocks = hidden_blocks;
        FAntiXRay.disabled_worlds = disabled_worlds;
        FAntiXRay.dark_blocks = new HashSet<Integer>(dark_extra);
        dark_blocks.addAll(hidden_blocks);

        FAntiXRay.engine_mode = engine_mode;        
        FAntiXRay.dark_enabled = dark_enabled;
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

            obfuscated = obfuscate(chunk, inflatedBuffers[i], true, '\uffff', false);

            System.arraycopy(obfuscated, 0, buildBuffer, index, inflatedBuffers[i].length);
            index += inflatedBuffers[i].length;
        }

        /* might be better for gc */
        inflatedBuffers = null; buildBuffer = null; obfuscated = null; 
        index = 0; packet.chunks.clear(); packet.chunks = null;

        /* return the obfuscated packet */
        return packet;
    }
    
    /* initialize the Packet51MapChunk */
    public static byte[] obfuscate(ChunkMap chunkmap, Chunk chunk, byte[] buildBuffer, boolean flag, int i) {

        /* empty chunk, return */
        if (chunkmap.b == 0 && chunkmap.c == 0) {
            return buildBuffer;
        }

        /* world is disabled, return */
        if (disabled_worlds.contains(chunk.world.getWorld().getName())) {
            return buildBuffer;
        }

        /* might be better for gc */
        chunkmap = null;

        /* return the obfuscated packet */
        return obfuscate(chunk, buildBuffer, flag, i, true);
    }
    
    /* initialize the chunk */
    public static byte[] obfuscate(Chunk chunk, byte[] buildBuffer, boolean flag, int i, boolean p51) {
        
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

        int index = 0;

        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {

                    int wx = (chunk.x << 4) + x;
                    int wy = (i << 4) + y;
                    int wz = (chunk.z << 4) + z;

                    int id = section.a(x, y, z);

                    if (dark_enabled) {
                        if (dark_blocks.contains(id)) {
                            int face = isBlocksTransparent(chunk, wx, wy, wz);
                            if (face == -1) {
                                buffer[index] = 1;
                            } else
                            if (!isBlocksInLight(chunk, wx, wy, wz, face)) {
                                buffer[index] = 1;
                            }
                        }
                    } else
                    if (engine_mode == 0) {
                        if (hidden_blocks.contains(id)) {
                            buffer[index] = 1;
                        }
                    } else
                    if (engine_mode == 1) {
                        if (hidden_blocks.contains(id)) {
                            if (isBlocksTransparent(chunk, wx, wy, wz) == -1) {
                                buffer[index] = 1;
                            }
                        }
                    } else
                    if (engine_mode == 2) {
                        if (isObfuscable(id)) {
                            if (isBlocksTransparent(chunk, wx, wy, wz) == -1) {
                                buffer[index] = (byte) getRandom();
                            }
                        }
                    } else
                    if (engine_mode == 3) {
                        if (id == 1) {
                            if (isBlocksTransparent(chunk, wx, wy, wz) == -1) {
                                buffer[index] = (byte) getRandom();
                            }
                        }
                    } else
                    if (engine_mode == 4) {
                        if (isBlocksTransparent(chunk, wx, wy, wz) == -1) {
                            buffer[index] = (byte) getRandom();
                        } else {
                            if (hidden_blocks.contains(id)) {
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

    /* return true if the block have light in one of its faces */
    private static boolean isBlocksInLight(Chunk chunk, int x, int y, int z, int face) {
        if (y > 60) { return true; }

        if (face == 0) {
            return (chunk.world.getLightLevel(x + 1, y, z) > 0);
        } else
        if (face == 1) {
            return (chunk.world.getLightLevel(x - 1, y, z) > 0);
        } else
        if (face == 2) {
            return (chunk.world.getLightLevel(x, y + 1, z) > 0);
        } else
        if (face == 3) {
            return (chunk.world.getLightLevel(x, y - 1, z) > 0);
        } else
        if (face == 4) {
            return (chunk.world.getLightLevel(x, y, z + 1) > 0);
        } else
        if (face == 5) {
            return (chunk.world.getLightLevel(x, y, z - 1) > 0);
        }

        return false;
    }

    /* return true if the block have a transparent block in one of its faces */
    private static int isBlocksTransparent(Chunk chunk, int x, int y, int z) {
        if (FUtils.isTransparent(chunk.world.getTypeId(x + 1, y, z))) {
            return 0;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x - 1, y, z))) {
            return 1;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x, y + 1, z))) {
            return 2;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x, y - 1, z))) {
            return 3;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x, y, z + 1))) {
            return 4;
        } else
        if (FUtils.isTransparent(chunk.world.getTypeId(x, y, z - 1))) {
            return 5;
        }
        return -1;
    }

    /* return true if it is a obfuscable id */
    public static boolean isObfuscable(int id) {
        switch (id) {
            case 1:
            case 3:
            case 13:
                return true;
            default:
                return false;
        }
    }
}
