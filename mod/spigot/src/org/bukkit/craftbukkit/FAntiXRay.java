package org.bukkit.craftbukkit;

import java.util.HashSet;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.Packet56MapChunkBulk;
import org.bukkit.block.BlockFace;

/**
 *
 * @author FurmigaHumana
 */
public class FAntiXRay {
    public static HashSet<String> exempts = new HashSet<String>();
    public static Integer[] random_blocks;
    public static HashSet<Integer> hidden_blocks;
    public static HashSet<String> disabled_worlds;
    public static HashSet<Integer> dark_blocks;

    public static int engine_mode;
    public static boolean dark_enabled;

    public static void load(Integer[] random_blocks, HashSet<Integer> hidden_blocks, HashSet<String> disabled_worlds, HashSet<Integer> dark_extra, int engine_mode, boolean dark_enabled) {        
        FAntiXRay.random_blocks = random_blocks;
        FAntiXRay.hidden_blocks = hidden_blocks;
        FAntiXRay.disabled_worlds = disabled_worlds;
        FAntiXRay.dark_blocks = new HashSet<Integer>(dark_extra);
        dark_blocks.addAll(hidden_blocks);

        FAntiXRay.engine_mode = engine_mode;        
        FAntiXRay.dark_enabled = dark_enabled;
    }

    public static void exempt(String player) {
        exempts.add(player);
    }

    public static void unexempt(String player) {
        exempts.remove(player);
    }

    public static boolean isExempt(String player) {
        return exempts.contains(player);
    }

    public static Packet56MapChunkBulk obfuscate(Packet56MapChunkBulk packet) {
        
        if (packet.player == null) {
            return packet;
        }

        int[] c = (int[]) FUtils.getPrivateField(packet, "c"); //X
        int[] d = (int[]) FUtils.getPrivateField(packet, "d"); //Z

        byte[][] inflatedBuffers = (byte[][]) FUtils.getPrivateField(packet, "inflatedBuffers");
        byte[] buildBuffer = (byte[]) FUtils.getPrivateField(packet, "buildBuffer");
        
        int index = 0;
        for (int i = 0; i < packet.d(); i++) {
            Chunk chunk = packet.player.world.getChunkAt(c[i], d[i]);

            byte[] obfuscated = obfuscate(chunk, inflatedBuffers[i], true, '\uffff');

            System.arraycopy(obfuscated, 0, buildBuffer, index, inflatedBuffers[i].length);
            index += inflatedBuffers[i].length;
        }
        
        packet.player = null;
        return packet;
    }
    
    public static byte[] obfuscate(ChunkMap chunkmap, Chunk chunk, byte[] buildBuffer, boolean flag, int i) {
        
        if (chunkmap.b == 0 && chunkmap.c == 0) {
            return buildBuffer;
        }
        
        return obfuscate(chunk, buildBuffer, flag, i);
    }
    
    public static byte[] obfuscate(Chunk chunk, byte[] buildBuffer, boolean flag, int i) {
        
        int index = 0;

        ChunkSection[] sections = chunk.i();
        for (int j1 = 0; j1 < sections.length; ++j1) {
            if (sections[j1] != null && (!flag || !sections[j1].a()) && (i & 1 << j1) != 0) {
                byte[] obfuscated = obfuscate(sections[j1], chunk, j1);

                System.arraycopy(obfuscated, 0, buildBuffer, index, obfuscated.length);
                index += obfuscated.length;
            }
        }

        return buildBuffer;
    }
    
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

                    if (engine_mode == 0) {
                        if (dark_enabled) {
                            if (dark_blocks.contains(id)) {
                                BlockFace face = isBlocksTransparent(chunk, wx, wy, wz);
                                if (face == null) {
                                    buffer[index] = 1;
                                } else {
                                    if (!isBlocksInLight(chunk, wx, wy, wz, face)) {
                                        buffer[index] = 1;
                                    }
                                }
                            }
                        } else {
                            if (hidden_blocks.contains(id)) {
                                buffer[index] = 1;
                            }
                        }
                    } else
                    if (engine_mode == 1) {
                        if (dark_enabled) {
                           if (dark_blocks.contains(id)) {
                                BlockFace face = isBlocksTransparent(chunk, wx, wy, wz);
                                if (face == null) {
                                    buffer[index] = 1;
                                } else {
                                    if (!isBlocksInLight(chunk, wx, wy, wz, face)) {
                                        buffer[index] = 1;
                                    }
                                }
                            }
                        } else {
                            if (hidden_blocks.contains(id)) {
                                if (isBlocksTransparent(chunk, wx, wy, wz) == null) {
                                    buffer[index] = 1;
                                }
                            }
                        }
                    } else
                    if (engine_mode == 2) {
                        if (isObfuscable(id)) {
                            if (isBlocksTransparent(chunk, wx, wy, wz) == null) {
                                buffer[index] = (byte) getRandom();
                            }
                        }
                    } else
                    if (engine_mode == 3) {
                        if (id == 1) {
                            if (isBlocksTransparent(chunk, wx, wy, wz) == null) {
                                buffer[index] = (byte) getRandom();
                            }
                        }
                    } else
                    if (engine_mode == 4) {
                        if (isBlocksTransparent(chunk, wx, wy, wz) == null) {
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

    public static int getRandom() {
        int random = ((int)(Math.random() * random_blocks.length));
        return random_blocks[ random ];
    }

    private static boolean isBlocksInLight(Chunk chunk, int x, int y, int z, BlockFace face) {
        if (face.equals(BlockFace.WEST)) {
            if (chunk.world.getWorld().getBlockAt(x + 1, y, z).getLightLevel() > 0) {
                return true;
            }
        } else
        if (face.equals(BlockFace.EAST)) {
            if (chunk.world.getWorld().getBlockAt(x - 1, y, z).getLightLevel() > 0) {
                return true;
            }
        } else
        if (face.equals(BlockFace.UP)) {
            if (chunk.world.getWorld().getBlockAt(x, y + 1, z).getLightLevel() > 0) {
                return true;
            }
        } else
        if (face.equals(BlockFace.DOWN)) {
            if (chunk.world.getWorld().getBlockAt(x, y - 1, z).getLightLevel() > 0) {
                return true;
            }
        } else
        if (face.equals(BlockFace.SOUTH)) {
            if (chunk.world.getWorld().getBlockAt(x, y, z + 1).getLightLevel() > 0) {
                return true;
            }
        } else
        if (face.equals(BlockFace.NORTH)) {
            if (chunk.world.getWorld().getBlockAt(x, y, z - 1).getLightLevel() > 0) {
                return true;
            }
        }

        return false;
    }

    private static BlockFace isBlocksTransparent(Chunk chunk, int x, int y, int z) {
        if (isBlockTransparent(chunk, x + 1, y, z)) {
            return BlockFace.WEST;
        } else
        if (isBlockTransparent(chunk, x - 1, y, z)) {
            return BlockFace.EAST;
        } else
        if (isBlockTransparent(chunk, x, y + 1, z)) {
            return BlockFace.UP;
        } else
        if (isBlockTransparent(chunk, x, y - 1, z)) {
            return BlockFace.DOWN;
        } else
        if (isBlockTransparent(chunk, x, y, z + 1)) {
            return BlockFace.SOUTH;
        } else
        if (isBlockTransparent(chunk, x, y, z - 1)) {
            return BlockFace.NORTH;
        }

        return null;
    }

    private static boolean isBlockTransparent(Chunk chunk, int x, int y, int z) {
        if (FUtils.isTransparent(chunk.world.getTypeId(x, y, z))) {
            return true;
        } else {
            return false;
        }
    }

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