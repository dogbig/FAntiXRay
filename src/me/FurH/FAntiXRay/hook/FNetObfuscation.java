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

package me.FurH.FAntiXRay.hook;

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.cache.FCacheQueue;
import me.FurH.FAntiXRay.cache.FChunkCache;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import me.FurH.FAntiXRay.listener.FWorldListener;
import me.FurH.FAntiXRay.util.FUtil;
import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.INetworkManager;
import net.minecraft.server.v1_4_6.MinecraftServer;
import net.minecraft.server.v1_4_6.Packet;
import net.minecraft.server.v1_4_6.Packet51MapChunk;
import net.minecraft.server.v1_4_6.Packet56MapChunkBulk;
import net.minecraft.server.v1_4_6.World;

/**
 *
 * @author FurmigaHumana
 */
public class FNetObfuscation extends FPlayerConnection {

    public FNetObfuscation(MinecraftServer minecraftserver, INetworkManager inetworkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, inetworkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet packet) {
        if (packet instanceof Packet56MapChunkBulk) {
            obfuscate((Packet56MapChunkBulk)packet);
        }
        
        if (packet instanceof Packet51MapChunk) {
            if (obfuscate((Packet51MapChunk)packet)) {
                return;
            }
        }

        super.sendPacket(packet);
    }
    
    public boolean obfuscate(Packet51MapChunk packet) {

        if (FAntiXRay.getConfiguration().disabled_worlds.contains(player.world.getWorld().getName())) {
            return false;
        }

        if (packet.c == 0 && packet.d == 0) {
            return false;
        }

        /*
         * If this chunk already was sent, there is no need to obfuscate and send it again.
         */
        if (FWorldListener.chunks.remove(packet.a + ":" +packet.b)) { 
            return true;
        }

        return false;
    }

    private void obfuscate(Packet56MapChunkBulk packet) {
        if (FUtil.getPrivateField(packet, "buffer") != null) { //Assuming the chunk is already being compressed
            return;
        }

        if (FAntiXRay.getConfiguration().disabled_worlds.contains(player.world.getWorld().getName())) {
            return;
        }

        FChunkCache cache = FAntiXRay.getCache();
        long hash = 0L;

        int engine_mode = FAntiXRay.getConfiguration().engine_mode;
        boolean usecache = FAntiXRay.getConfiguration().enable_cache;
        boolean savecache = false;

        int[] c = (int[]) FUtil.getPrivateField(packet, "c"); //X
        int[] d = (int[]) FUtil.getPrivateField(packet, "d"); //Z

        byte[][] inflatedBuffers = (byte[][]) FUtil.getPrivateField(packet, "inflatedBuffers");
        byte[] buildBuffer = (byte[]) FUtil.getPrivateField(packet, "buildBuffer");

        int index = 0;
        for (int i = 0; i < packet.d(); i++) {
            byte[] obfuscated = null;

            if (usecache) {
                hash = FUtil.getHash(inflatedBuffers[i]);
                obfuscated = cache.read(player.world, c[i], d[i], hash, engine_mode);
            }

            if (obfuscated == null) {
                obfuscated = obfuscate(inflatedBuffers[i], c[i], d[i], engine_mode);
                savecache = true;
            }

            if (inflatedBuffers[i].length > obfuscated.length) {
                obfuscated = new byte[ inflatedBuffers[i].length ];
            }

            System.arraycopy(obfuscated, 0, buildBuffer, index, inflatedBuffers[i].length);

            if (savecache && usecache) {
                FCacheQueue.onQueue(player.world, c[i], d[i], obfuscated, hash, engine_mode);
            }

            index += inflatedBuffers[i].length;
        }
    }

    public byte[] obfuscate(byte[] buffer, int cx, int cz, int engine_mode) {
        FConfiguration config = FAntiXRay.getConfiguration();
        
        for (int i = 0; i < 16; i++) {

            int increment = 0;

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {

                        int wx = (cx << 4) + x;
                        int wy = (i << 4) + y;
                        int wz = (cz << 4) + z;
                        int index = (i * 4096) + increment;

                        int id = player.world.getTypeId(wx, wy, wz);
                        boolean light = false;
                        
                        if (config.dark_enabled) {
                            light = isBlocksInLight(wx, wy, wz);
                        }
                        
                        if (engine_mode == 0) {
                            if (config.hidden_blocks.contains(id)) {
                                if (!light) {
                                    buffer[index] = 1;
                                }
                            } else if (!light) {
                                if (config.dark_extra.contains(id)) {
                                    buffer[index] = 1;
                                }
                            }
                        } else
                        if (engine_mode == 1) {
                            if (config.hidden_blocks.contains(id)) {
                                if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                    buffer[index] = 1;
                                } else if (!light) {
                                    buffer[index] = 1;
                                }
                            } else if (!light) {
                                if (config.dark_extra.contains(id)) {
                                    buffer[index] = 1;
                                }
                            }
                        } else
                        if (engine_mode == 2) {
                            if (id != 63 && id != 68 && id != 0) {
                                if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                    buffer[index] = (byte) FUtil.getRandom();
                                }
                            }
                        } else
                        if (engine_mode == 3) {
                            if (id == 1) {
                                if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                    buffer[index] = (byte) FUtil.getRandom();
                                }
                            }
                        } else
                        if (engine_mode == 4) {
                            if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                buffer[index] = (byte) FUtil.getRandom();
                            } else {
                                if (config.hidden_blocks.contains(id)) {
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
    
    private boolean isBlocksInLight(int x, int y, int z) {
        if (player.world.getWorld().getBlockAt(x, y + 1, z).getLightLevel() > 0) {
            return true;
        } else
        if (player.world.getWorld().getBlockAt(x, y - 1, z).getLightLevel() > 0) {
            return true;
        } else
        if (player.world.getWorld().getBlockAt(x + 1, y, z).getLightLevel() > 0) {
            return true;
        } else
        if (player.world.getWorld().getBlockAt(x - 1, y, z).getLightLevel() > 0) {
            return true;
        } else
        if (player.world.getWorld().getBlockAt(x, y, z + 1).getLightLevel() > 0) {
            return true;
        } else
        if (player.world.getWorld().getBlockAt(x, y, z - 1).getLightLevel() > 0) {
            return true;
        }
        return false;
    }
    
    private boolean isBlocksTransparent(World world, int x, int y, int z) {
        if (isBlockTransparent(world, x + 1, y, z)) {
            return true;
        } else
        if (isBlockTransparent(world, x - 1, y, z)) {
            return true;
        } else
        if (isBlockTransparent(world, x, y + 1, z)) {
            return true;
        } else
        if (isBlockTransparent(world, x, y - 1, z)) {
            return true;
        } else
        if (isBlockTransparent(world, x, y, z + 1)) {
            return true;
        } else
        if (isBlockTransparent(world, x, y, z - 1)) {
            return true;
        } else {
            return false;
        }
    }
    
    private static boolean isBlockTransparent(World world, int x, int y, int z) {
        if (!net.minecraft.server.v1_4_6.Block.i(world.getTypeId(x, y, z))) {
            return true;
        } else {
            return false;
        }
    }
}