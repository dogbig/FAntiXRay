package me.FurH.server.FAntiXRay.hooks;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;
import net.minecraft.server.PlayerConnection;

/**
 *
 * @author FurmigaHumana
 */
public class FPlayerConnection extends PlayerConnection {

    public FPlayerConnection(MinecraftServer minecraftserver, INetworkManager inetworkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, inetworkmanager, entityplayer);
    }
    
    @Override
    public void sendPacket(Packet packet) {

        if (packet instanceof Packet56MapChunkBulk) {
            Packet56MapChunkBulk p56 = (Packet56MapChunkBulk)packet;
            p56.obfuscate = true;
        } else
        if (packet instanceof Packet51MapChunk) {
            Packet51MapChunk p51 = (Packet51MapChunk)packet;
            p51.obfuscate = true;
        }
        
        super.sendPacket(packet);
    }
}
