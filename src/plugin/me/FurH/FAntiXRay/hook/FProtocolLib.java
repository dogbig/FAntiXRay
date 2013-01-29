package me.FurH.FAntiXRay.hook;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import me.FurH.FAntiXRay.threads.FPacketData;
import net.minecraft.server.v1_4_R1.Packet;
import net.minecraft.server.v1_4_R1.Packet51MapChunk;
import net.minecraft.server.v1_4_R1.Packet56MapChunkBulk;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
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
                    if (!FAntiXRay.isExempt(p.getName())) {

                        if (FAntiXRay.getConfiguration().thread_enabled || FAntiXRay.getConfiguration().thread_player) {
                            FAntiXRay.getThreadManager().add(new FPacketData(((CraftPlayer)e.getPlayer()).getHandle(), (Packet) e.getPacket().getHandle()));
                            return;
                        }
                    
                        Packet51MapChunk packet = (Packet51MapChunk) e.getPacket().getHandle();
                        FObfuscator.obfuscate(((CraftPlayer)e.getPlayer()).getHandle(), packet, true);
                    }
                } else
                if (e.getPacketID() == Packets.Server.MAP_CHUNK_BULK) {
                    if (!FAntiXRay.isExempt(p.getName())) {
                        
                        if (FAntiXRay.getConfiguration().thread_enabled || FAntiXRay.getConfiguration().thread_player) {
                            FAntiXRay.getThreadManager().add(new FPacketData(((CraftPlayer)e.getPlayer()).getHandle(), (Packet) e.getPacket().getHandle()));
                            return;
                        }

                        Packet56MapChunkBulk packet = (Packet56MapChunkBulk) e.getPacket().getHandle();
                        FObfuscator.obfuscate(((CraftPlayer)e.getPlayer()).getHandle(), packet, true);
                    }
                }
            }
        });
    }
}