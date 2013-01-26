package me.FurH.FAntiXRay.hook;

import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import me.FurH.server.FAntiXRay.FObfuscationRequest;
import net.minecraft.server.v1_4_R1.Packet;
import net.minecraft.server.v1_4_R1.Packet51MapChunk;
import net.minecraft.server.v1_4_R1.Packet56MapChunkBulk;

/**
 *
 * @author FurmigaHumana
 */
public class FObfuscatorHook extends FObfuscationRequest {
    
    @Override
    public Packet getObfuscated(Packet packet) {

        if (packet instanceof Packet56MapChunkBulk) {
            Packet56MapChunkBulk p56 = (Packet56MapChunkBulk)packet;
            packet = FObfuscator.obfuscate56(p56);
        } else
        if (packet instanceof Packet51MapChunk) {
            Packet51MapChunk p51 = (Packet51MapChunk)packet;
            packet = FObfuscator.obfuscate51(p51);
        }

        return packet;
    }
}
