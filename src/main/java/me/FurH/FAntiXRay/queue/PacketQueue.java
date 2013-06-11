package me.FurH.FAntiXRay.queue;

import me.FurH.Core.CorePlugin;
import me.FurH.Core.packets.IPacketQueue;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class PacketQueue extends IPacketQueue {
    
    public PacketQueue(CorePlugin plugin) {
        super(plugin);
    }

    @Override
    public Object handleAsyncMapChunkBulk(Player player, Object o) {
        return FObfuscator.obfuscate(player, o);
    }

    @Override
    public Object handleAsyncMapChunk(Player player, Object o) {
        return FObfuscator.obfuscate(player, o);
    }

    @Override
    public boolean handleAsyncCustomPayload(Player player, String string, int i, byte[] bytes) { return true; }

    @Override
    public Object handleAndSetAsyncCustomPayload(Player player, Object o) { return o; }

    @Override
    public boolean handleAsyncClientSettings(Player player) { return true; }

    @Override
    public Object handleSyncMapChunkBulk(Player player, Object o) {
        return o;
    }

    @Override
    public Object handleSyncMapChunk(Player player, Object o) {
        return o;
    }
}
