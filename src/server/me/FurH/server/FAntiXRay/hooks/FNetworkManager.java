package me.FurH.server.FAntiXRay.hooks;

import java.io.IOException;
import java.net.Socket;
import java.security.PrivateKey;
import me.FurH.server.FAntiXRay.FAntiXRay;
import net.minecraft.server.Connection;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet56MapChunkBulk;

/**
 *
 * @author FurmigaHumana
 */
public class FNetworkManager extends NetworkManager {

    public FNetworkManager(Socket socket, String s, Connection connection, PrivateKey privatekey) throws IOException { // CraftBukkit - throws IOException
        super(socket, s, connection, privatekey);
    }
    
    @Override
    public Packet a(boolean flag) {
        Packet packet = super.a(flag);
        
        if (packet != null) {
            if (packet instanceof Packet56MapChunkBulk) {
                packet = FAntiXRay.obfuscate((Packet56MapChunkBulk)packet);
            }
        }
        
        return packet;
    }
}
