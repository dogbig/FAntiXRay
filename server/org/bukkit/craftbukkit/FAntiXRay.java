package org.bukkit.craftbukkit;

import java.util.HashSet;
import net.minecraft.server.Block;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkMap;
import net.minecraft.server.ChunkSection;
import org.bukkit.block.BlockFace;

/**
 *
 * @author FurmigaHumana
 */
public class FAntiXRay {
    public static Integer[] random_blocks;
    public static HashSet<Integer> hidden_blocks;
    public static HashSet<String> disabled_worlds;
    public static HashSet<Integer> dark_blocks;

    public static int engine_mode;
    public static boolean dark_enabled;

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public static void load(Integer[] random_blocks, HashSet<Integer> hidden_blocks, HashSet<String> disabled_worlds, HashSet<Integer> dark_extra, int engine_mode, boolean dark_enabled) {        
        FAntiXRay.random_blocks = random_blocks;
        FAntiXRay.hidden_blocks = hidden_blocks;
        FAntiXRay.disabled_worlds = disabled_worlds;
        FAntiXRay.dark_blocks = new HashSet<Integer>(dark_extra);
        dark_blocks.addAll(hidden_blocks);

        FAntiXRay.engine_mode = engine_mode;        
        FAntiXRay.dark_enabled = dark_enabled;
    }
    
    public static byte[] obfuscate(ChunkMap chunkmap, Chunk chunk, byte[] buffer) {

        if (chunkmap.b == 0 && chunkmap.c == 0) {
            return buffer;
        }

        if (disabled_worlds.contains(chunk.world.getWorld().getName())) {
            return buffer;
        }

        for (int i = 0; i < 16; i++) {

            ChunkSection section = chunk.i()[i];
            if (section == null) {
                continue;
            }
            
            if (section.a()) {
                continue;
            }
                        
            int increment = 0;

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {

                        int wx = (chunk.x << 4) + x;
                        int wy = section.d() + y;
                        int wz = (chunk.z << 4) + z;

                        int index = (i * 4096) + increment;

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
                                    if (isBlocksTransparent(chunk, wx, wy, wz) != null) {
                                        buffer[index] = 1;
                                    }
                                }
                            }
                        } else
                        if (engine_mode == 2) {
                            if (id != 63 && id != 68 && id != 0) {
                                if (isBlocksTransparent(chunk, wx, wy, wz) != null) {
                                    buffer[index] = (byte) getRandom();
                                }
                            }
                        } else
                        if (engine_mode == 3) {
                            if (id == 1) {
                                if (isBlocksTransparent(chunk, wx, wy, wz) != null) {
                                    buffer[index] = (byte) getRandom();
                                }
                            }
                        } else
                        if (engine_mode == 4) {
                            if (isBlocksTransparent(chunk, wx, wy, wz) != null) {
                                buffer[index] = (byte) getRandom();
                            } else {
                                if (hidden_blocks.contains(id)) {
                                    buffer[index] = 1;
                                }
                            }
                        }
                        increment++;
                    }
                }
            }
        }
        return buffer;
    }
    
    /* TODO: OPTIMIZE! */
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
        } else {
            return null;
        }
    }
    
    private static boolean isBlockTransparent(Chunk chunk, int x, int y, int z) {
        if (!Block.i(chunk.world.getTypeId(x, y, z))) {
            return true;
        } else {
            return false;
        }
    }

    public static int getRandom() {
        int random = ((int)(Math.random() * random_blocks.length));
        return random_blocks[ random ];
    }
}
