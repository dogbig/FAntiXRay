package me.FurH.FAntiXRay.hook;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.FurH.FAntiXRay.FAntiXRay;
import net.minecraft.server.v1_4_R1.Packet51MapChunk;
import net.minecraft.server.v1_4_R1.Packet56MapChunkBulk;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FProtocolLib {
    
    public static void setupProtocolLib(FAntiXRay plugin) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        
        manager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, new Integer[] { Packets.Server.MAP_CHUNK, Packets.Server.MAP_CHUNK_BULK }) {
            @Override
            public void onPacketSending(PacketEvent e) {
                Player p = e.getPlayer();
                if (e.getPacketID() == Packets.Server.MAP_CHUNK) {
                    Packet51MapChunk packet = (Packet51MapChunk) e.getPacket().getHandle();
                    if (!FAntiXRay.isExempt(p.getName())) {
                        packet.obfuscate = true;
                    }
                } else
                if (e.getPacketID() == Packets.Server.MAP_CHUNK_BULK) {
                    Packet56MapChunkBulk packet = (Packet56MapChunkBulk) e.getPacket().getHandle();
                    if (!FAntiXRay.isExempt(p.getName())) {
                        packet.obfuscate = true;
                    }
                }
            }
        });
    }
}