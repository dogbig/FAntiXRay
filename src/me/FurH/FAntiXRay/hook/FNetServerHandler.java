package me.FurH.FAntiXRay.hook;

import java.lang.reflect.Field;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.cache.FCacheQueue;
import me.FurH.FAntiXRay.cache.FChunkCache;
import me.FurH.FAntiXRay.util.FUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraft.server.World;

/**
 *
 * @author FurmigaHumana
 */
public class FNetServerHandler extends FNetServerProxy {
    
    public FNetServerHandler(MinecraftServer minecraftserver, NetServerHandler instance) {
        super(minecraftserver, instance);
    }
    
    @Override
    public void sendPacket(Packet packet) {
        if (packet instanceof Packet56MapChunkBulk) {
            obfuscate((Packet56MapChunkBulk)packet);
        } else {
            if (packet instanceof Packet51MapChunk) {
                Packet51MapChunk px = (Packet51MapChunk)packet;
                if (px.c != 0 && px.d != 0) {
                    FAntiXRay.log.severe("[FAntiXRay]: Packet51MapChunk was used! Alert the developer!");
                    return;
                }
            }
            super.sendPacket(packet);
        }
    }

    private void obfuscate(Packet56MapChunkBulk packet) {        
        NetServerHandler netServerHandler = player.netServerHandler;
        if (netServerHandler.disconnected) {
            return;
        }
        
        if (FAntiXRay.getConfiguration().disabled_worlds.contains(player.world.getWorld().getName())) {
            super.sendPacket(packet);
        }

        FChunkCache cache = FAntiXRay.getCache();
        long hash = 0L;

        int engine_mode = FAntiXRay.getConfiguration().engine_mode;
        boolean usecache = FAntiXRay.getConfiguration().enable_cache;
        boolean savecache = false;

        int[] a = packet.a;
        int[] c = (int[]) getPrivate(packet, "c"); //X
        int[] d = (int[]) getPrivate(packet, "d"); //Z

        byte[][] inflatedBuffers = (byte[][]) getPrivate(packet, "inflatedBuffers");
        byte[] buildBuffer = (byte[]) getPrivate(packet, "buildBuffer");

        int index = 0;
        for (int i = 0; i < packet.d(); i++) {

            hash = getHash(inflatedBuffers[i], inflatedBuffers[i].length);
            byte[] obfuscated = null;
            
            if (usecache) {
                obfuscated = cache.read(player.world, c[i], d[i], hash, engine_mode);
            }
            
            if (obfuscated == null) {
                obfuscated = obfuscate(inflatedBuffers[i], a[i], c[i], d[i], engine_mode);
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

        super.sendPacket(packet);
    }
    
    private byte[] obfuscate(byte[] buffer, int a, int cx, int cz, int engine_mode) {
        int c = 0;
        
        for (int i = 0; i < 16; i++) {
            if (  (a & 1 << i) > 0  ) {
                
                int index = 0;
                
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            
                            int wx = (cx << 4) + x;
                            int wy = (i << 4) + y;
                            int wz = (cz << 4) + z;
                            
                            int id = player.world.getTypeId(wx, wy, wz);

                            if (engine_mode == 0) {
                                if (FAntiXRay.getConfiguration().hidden_blocks.contains(id)) {
                                    if (id != 63 && id != 68) {
                                        buffer[(c * 4096) + index] = 1;
                                    }
                                }
                            } else
                            if (engine_mode == 1) {
                                if (FAntiXRay.getConfiguration().hidden_blocks.contains(id)) {
                                    if (id != 63 && id != 68) {
                                        if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                            buffer[(c * 4096) + index] = 1;
                                        }
                                    }
                                }
                            } else
                            if (engine_mode == 2) {
                                if (id != 63 && id != 68) {
                                    if (!isBlocksTransparent(player.world, wx, wy, wz)) {
                                        buffer[(c * 4096) + index] = (byte) FUtil.getRandom();
                                    }
                                }
                            }
                            index++;
                        }
                    }
                }
                c++;
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
        if (!net.minecraft.server.Block.i(world.getTypeId(x, y, z))) {
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