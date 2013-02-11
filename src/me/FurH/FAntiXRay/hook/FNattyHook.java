package me.FurH.FAntiXRay.hook;

import java.util.Queue;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.queue.FPacketQueue;
import me.FurH.FAntiXRay.util.FReflectField;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.Packet;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class FNattyHook extends FHookManager {

    @Override
    public void hook(Player p) {
        if (!FAntiXRay.isExempt(p.getName())) {
            final EntityPlayer player = ((CraftPlayer)p).getHandle();
            
            startTask(p, FAntiXRay.getConfiguration().chest_interval);

            io.netty.channel.Channel channel = (io.netty.channel.Channel) FReflectField.getPrivateField(player.playerConnection.networkManager, "channel");
            channel.pipeline().remove("encoder");
            channel.pipeline().addLast("encoder", new FPacketEncoder(player));

            Queue<Packet> syncPackets = (Queue<Packet>) FReflectField.getPrivateField(player.playerConnection.networkManager, "syncPackets");

            FPacketQueue newSyncPackets = new FPacketQueue(player);
            newSyncPackets.addAll(syncPackets);

            FReflectField.setFinalField(player.playerConnection.networkManager, "syncPackets", newSyncPackets);
        }
    }
}