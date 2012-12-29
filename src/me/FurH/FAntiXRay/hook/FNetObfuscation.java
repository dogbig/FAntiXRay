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

import java.lang.reflect.Field;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
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
            super.sendPacket(packet);
        } else
        if (packet instanceof Packet51MapChunk) {
            Packet51MapChunk p51 = (Packet51MapChunk)packet;
            byte[] inflatedBuffer = (byte[]) getPrivate(p51, "inflatedBuffer");
            if (inflatedBuffer.length == 256) {
                super.sendPacket(p51);
            }
        } else {
            super.sendPacket(packet);
        }
    }

    private void obfuscate(Packet56MapChunkBulk packet) {

        if (getPrivate(packet, "buffer") != null) { //Assuming the chunk is already being compressed
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

        int[] c = (int[]) getPrivate(packet, "c"); //X
        int[] d = (int[]) getPrivate(packet, "d"); //Z

        byte[][] inflatedBuffers = (byte[][]) getPrivate(packet, "inflatedBuffers");
        byte[] buildBuffer = (byte[]) getPrivate(packet, "buildBuffer");

        int index = 0;
        for (int i = 0; i < packet.d(); i++) {
            byte[] obfuscated = null;

            /*if (FWorldListener.chunks.remove(c[i] + ":" + d[i])) {
                //FChunkRWork.queue.add(new FChunkData(packet, this.player));
                //sendpacket = false;
                //continue;
                System.out.println("IS HERE: " + c[i] + ":" + d[i]);
            }*/

            System.arraycopy(inflatedBuffers[i], 0, buildBuffer, index, inflatedBuffers[i].length);
            
            if (usecache) {
                hash = getHash(inflatedBuffers[i], inflatedBuffers[i].length);
                obfuscated = cache.read(player.world, c[i], d[i], hash, engine_mode);
            }

            if (obfuscated == null) {
                obfuscated = obfuscate(inflatedBuffers[i], c[i], d[i], engine_mode);
                savecache = true;
            }

            if (inflatedBuffers[i].length > obfuscated.length) {
                obfuscated = new byte[ inflatedBuffers[i].length ];
            }

            try {
                System.arraycopy(obfuscated, 0, buildBuffer, index, inflatedBuffers[i].length);
            } catch (Exception ex) {
                FAntiXRay.log.severe("[FAntiXRay]: Error on chunk processing! Please, send this error to the developer!");
                FAntiXRay.log.severe("[FAntiXRay]: inflatedBuffer: " + inflatedBuffers[i].length + ", buildBuffer: " + buildBuffer.length + ", obfuscated: " + obfuscated.length);
            }
            
            if (savecache && usecache) {
                FCacheQueue.onQueue(player.world, c[i], d[i], obfuscated, hash, engine_mode);
            }

            index += inflatedBuffers[i].length;
        }
    }

    private byte[] obfuscate(byte[] buffer, int cx, int cz, int engine_mode) {
        for (int i = 0; i < 16; i++) {

            int index = 0;

            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {

                        int wx = (cx << 4) + x;
                        int wy = (i << 4) + y;
                        int wz = (cz << 4) + z;
                        int dindex = (i * 4096) + index;

                        int id = player.world.getTypeId(wx, wy, wz);

                        FConfiguration config = FAntiXRay.getConfiguration();
                        /* 
                         * TODO: FIND A WAY TO GET THE LIGHT LEVEL, getLightLevel ALWAYS RETURN 0
                         */
                        
                        if (engine_mode == 0) {
                            if (config.hidden_blocks.contains(id)) {
                                buffer[dindex] = 1;
                            }
                        } else
                        if (engine_mode == 1) {
                            if (config.hidden_blocks.contains(id)) {
                                if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                    buffer[dindex] = 1;
                                }
                            }
                        } else
                        if (engine_mode == 2) {
                            if (id != 63 && id != 68 && id != 0) {
                                if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                    buffer[dindex] = (byte) FUtil.getRandom();
                                }
                            }
                        } else
                        if (engine_mode == 3) {
                            if (id == 1) {
                                if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                    buffer[dindex] = (byte) FUtil.getRandom();
                                }
                            }
                        } else
                        if (engine_mode == 4) {
                            if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                buffer[dindex] = (byte) FUtil.getRandom();
                            } else {
                                if (config.hidden_blocks.contains(id)) {
                                    buffer[dindex] = 1;
                                }
                            }
                        }
                        index++;
                    }
                }
            }
        }
        return buffer;
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

    private Object getPrivate(Object object, String x) {
        try {
            Field field = object.getClass().getDeclaredField(x);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            FAntiXRay.log.severe("[FAntiXRay]: Failed to get private field: " + x + ", " + ex.getMessage());
        }
        return null;
    }
    
    private long getHash(byte[] data, int size) {
        Checksum checksum = new CRC32();
        checksum.reset();
        checksum.update(data, 0, size);
        return checksum.getValue();
    }
}