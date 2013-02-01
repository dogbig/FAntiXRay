package me.FurH.FAntiXRay.hook;

import me.FurH.FAntiXRay.FAntiXRay;

/**
 *
 * @author FurmigaHumana
 */
public class FProtocolLib {
    
    public static void setupProtocolLib(FAntiXRay plugin) {
        /*ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        
        manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, new Integer[] { Packets.Server.MAP_CHUNK, Packets.Server.MAP_CHUNK_BULK }) {
            @Override
            public void onPacketSending(PacketEvent e) {
                Player p = e.getPlayer();
                if (e.getPacketID() == Packets.Server.MAP_CHUNK) {
                    if (!FAntiXRay.isExempt(p.getName())) {
                        Packet51MapChunk packet = (Packet51MapChunk) e.getPacket().getHandle();
                        FObfuscator.obfuscate(((CraftPlayer)e.getPlayer()).getHandle(), packet, true);
                    }
                } else
                if (e.getPacketID() == Packets.Server.MAP_CHUNK_BULK) {
                    if (!FAntiXRay.isExempt(p.getName())) {
                        Packet56MapChunkBulk packet = (Packet56MapChunkBulk) e.getPacket().getHandle();
                        FObfuscator.obfuscate(((CraftPlayer)e.getPlayer()).getHandle(), packet, true);
                    }
                }
            }
        });*/
    }
}