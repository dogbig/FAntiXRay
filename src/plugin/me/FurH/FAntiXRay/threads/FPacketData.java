package me.FurH.FAntiXRay.threads;

import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.Packet;

/**
 *
 * @author FurmigaHumana
 */
public class FPacketData {
    public EntityPlayer player;
    public Packet packet;
    
    public FPacketData(EntityPlayer player, Packet packet) {
        this.player = player;
        this.packet = packet;
    }
}